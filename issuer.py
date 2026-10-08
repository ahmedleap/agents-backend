
from flask import Flask, jsonify, abort
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization
import jwt
import uuid
import datetime
import base64
import os

app = Flask(__name__)

ISSUER = "agents-of-leap"
AUDIENCE = "trading-middleware"
KEY_ID = "local-dev-key-1"

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


# Public JWKS endpoint
@app.route("/.well-known/jwks.json", methods=["GET"])
def jwks():
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


# Generate development tokens only
@app.route("/dev/token", methods=["GET"])
def generate_token():

    # Only permit local requests
    if os.environ.get("JWT_DEV_MODE") != "true":
        abort(404)

    role = "CLIENT"

    now = datetime.datetime.now(
        datetime.timezone.utc
    )

    payload = {
        "iss": ISSUER,
        "aud": AUDIENCE,
        "sub": str(uuid.UUID(
            "11111111-1111-4111-8111-111111111111"
        )),
        "roles": [role],
        "iat": now,
        "nbf": now,
        "exp": now + datetime.timedelta(minutes=30)
    }

    token = jwt.encode(
        payload,
        private_key,
        algorithm="RS256",
        headers={"kid": KEY_ID}
    )

    return jsonify({
        "access_token": token,
        "token_type": "Bearer",
        "expires_in": 1800
    })


if __name__ == "__main__":
    app.run(
        host="127.0.0.1",
        port=3000,
        debug=False
    )
