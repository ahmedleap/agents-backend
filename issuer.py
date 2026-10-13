
"""Loopback-only development JWT issuer for testing the Spring resource server.

The RSA private key is generated in memory for each process and is never written to disk.
This utility is not for production and refuses to run with JWT_DEV_MODE enabled under a
production APP_ENV.
"""

from flask import Flask, jsonify, abort, request
from cryptography.hazmat.primitives.asymmetric import rsa
import jwt
import uuid
import datetime
import base64
import os
import hmac
import hashlib
import secrets
import threading

app = Flask(__name__)

ISSUER = "agents-middleware"
AUDIENCE = "agents-backend"
KEY_ID = "local-dev-key-1"
TEST_SUBJECT = "11111111-1111-4111-8111-111111111111"
ALLOWED_ROLES = {"CLIENT", "ADMIN", "ANALYST"}
ALLOWED_ENVIRONMENTS = {"development", "dev", "test", "local"}
TEST_USERS = {
    "alex@example.com": {
        "email": "alex@example.com",
        "password": "ClientTest123!",
        "role": "CLIENT",
        "sub": TEST_SUBJECT,
        "clientId": TEST_SUBJECT,
        "firstName": "Alex",
        "lastName": "Client",
    },
    "admin@example.test": {
        "email": "admin@example.test",
        "password": "AdminTest123!",
        "role": "ADMIN",
        "sub": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
        "clientId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
        "firstName": "Admin",
        "lastName": "User",
    },
    "analyst@example.test": {
        "email": "analyst@example.test",
        "password": "AnalystTest123!",
        "role": "ANALYST",
        "sub": "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb",
        "clientId": "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb",
        "firstName": "Analyst",
        "lastName": "User",
    },
}
ACCESS_TOKEN_SECONDS = 300
REFRESH_TOKEN_SECONDS = 600
REFRESH_SESSIONS = {}
REFRESH_SESSIONS_LOCK = threading.Lock()

# Development-only key, generated fresh each time
# the test server starts.
private_key = rsa.generate_private_key(
    public_exponent=65537,
    key_size=2048
)

public_key = private_key.public_key()
public_numbers = public_key.public_numbers()


def base64url(number):
    raw = number.to_bytes(
        (number.bit_length() + 7) // 8,
        "big"
    )
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode()


def dev_mode_enabled():
    return (os.environ.get("JWT_DEV_MODE", "").lower() == "true"
            and os.environ.get("APP_ENV", "development").lower() in ALLOWED_ENVIRONMENTS)


def require_local_dev_mode():
    # It is deliberately bound to loopback; the request check is defense in depth.
    if not dev_mode_enabled() or request.remote_addr not in {"127.0.0.1", "::1"}:
        abort(404)


# Public JWKS endpoint is only exposed while explicit dev mode is active.
@app.route("/.well-known/jwks.json", methods=["GET"])
def jwks():
    require_local_dev_mode()
    return jsonify({
        "keys": [{
            "kty": "RSA",
            "use": "sig",
            "alg": "RS256",
            "kid": KEY_ID,
            "n": base64url(public_numbers.n),
            "e": base64url(public_numbers.e)
        }]
    })


# Generate development tokens only. POST keeps tokens out of request URLs/history.
@app.route("/dev/token", methods=["POST"])
def generate_token():
    require_local_dev_mode()
    body = request.get_json(silent=True) or {}
    role = body.get("role", "CLIENT")
    if role not in ALLOWED_ROLES:
        return jsonify({"error": "role must be CLIENT, ADMIN, or ANALYST"}), 400
    case = body.get("case", "valid")
    if case not in {"valid", "wrong-issuer", "wrong-audience", "expired", "invalid-subject", "wrong-signature"}:
        return jsonify({"error": "unsupported test case"}), 400

    token = issue_token(role, TEST_SUBJECT, case)

    return jsonify({
        "access_token": token,
        "token_type": "Bearer",
        "expires_in": ACCESS_TOKEN_SECONDS
    })


@app.route("/auth/login", methods=["POST"])
def development_login():
    """Fake local credentials for getting a Bruno access token; not production auth."""
    require_local_dev_mode()
    body = request.get_json(silent=True) or {}
    email = body.get("email")
    password = body.get("password")
    if not isinstance(email, str) or not isinstance(password, str):
        return jsonify({"error": "email and password are required"}), 400

    normalized_email = email.strip().lower()
    user = TEST_USERS.get(normalized_email)
    expected_password = user["password"] if user else "development-dummy-password"
    if user is None or not hmac.compare_digest(password.encode(), expected_password.encode()):
        return jsonify({"error": "Invalid email or password"}), 401

    return token_response(user)


@app.route("/auth/refresh", methods=["POST"])
def development_refresh():
    """Rotate a local refresh token and return a fresh token pair."""
    require_local_dev_mode()
    body = request.get_json(silent=True) or {}
    refresh_token = body.get("refreshToken")
    if not isinstance(refresh_token, str) or not refresh_token:
        return jsonify({"error": "refreshToken is required"}), 400

    token_hash = hashlib.sha256(refresh_token.encode("utf-8")).hexdigest()
    now = datetime.datetime.now(datetime.timezone.utc).timestamp()
    with REFRESH_SESSIONS_LOCK:
        session = REFRESH_SESSIONS.pop(token_hash, None)
        if session is None or session["expires_at"] <= now:
            return jsonify({"error": "Invalid, expired, or already-used refresh token"}), 401
        user = session["user"]

    return token_response(user)


def token_response(user):
    access_token = issue_token(user["role"], user["sub"], email=user["email"])
    refresh_token = secrets.token_urlsafe(48)
    refresh_hash = hashlib.sha256(refresh_token.encode("utf-8")).hexdigest()
    expires_at = datetime.datetime.now(datetime.timezone.utc).timestamp() + REFRESH_TOKEN_SECONDS
    with REFRESH_SESSIONS_LOCK:
        REFRESH_SESSIONS[refresh_hash] = {"user": user, "expires_at": expires_at}

    return jsonify({
        "accessToken": access_token,
        "refreshToken": refresh_token,
        "expiresIn": ACCESS_TOKEN_SECONDS,
        "user": {
            "clientId": user["clientId"],
            "email": user["email"],
            "firstName": user["firstName"],
            "lastName": user["lastName"],
            "role": user["role"],
        },
    })


def issue_token(role, subject, case="valid", email="client@example.test"):
    now = datetime.datetime.now(datetime.timezone.utc)
    payload = {
        "iss": "wrong-issuer" if case == "wrong-issuer" else ISSUER,
        "aud": "wrong-audience" if case == "wrong-audience" else AUDIENCE,
        "sub": "not-a-uuid" if case == "invalid-subject" else str(uuid.UUID(subject)),
        "email": email,
        "jti": str(uuid.uuid4()),
        "roles": [role],
        "iat": now,
        "nbf": now,
        "exp": now - datetime.timedelta(seconds=1) if case == "expired"
            else now + datetime.timedelta(seconds=ACCESS_TOKEN_SECONDS),
    }
    signing_key = private_key
    key_id = KEY_ID
    if case == "wrong-signature":
        # Sign with a key deliberately absent from the JWKS.
        signing_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
        key_id = "unknown-test-key"
    return jwt.encode(payload, signing_key, algorithm="RS256", headers={"kid": key_id})


if __name__ == "__main__":
    app_env = os.environ.get("APP_ENV", "development").lower()
    if os.environ.get("JWT_DEV_MODE", "").lower() != "true":
        raise RuntimeError("Set JWT_DEV_MODE=true to start the local development issuer")
    if app_env not in ALLOWED_ENVIRONMENTS:
        raise RuntimeError("The local development issuer cannot run when APP_ENV is production")
    app.run(
        host="127.0.0.1",
        port=3000,
        debug=False
    )
