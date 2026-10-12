package com.agentsbackend.services;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility for parsing RSA keys from PEM or base64 format.
 * Handles base64-encoded strings that may contain:
 * - PEM formatted keys with headers/footers
 * - Raw base64 with newlines
 * - Base64 with embedded line breaks (encoded as \n)
 */
public class RsaKeys {

    /**
     * Parses a public key from base64-encoded PKCS#8 format or PEM format.
     * First tries to decode as base64 (handling embedded newlines).
     * If result is PEM, extracts the base64 content.
     * Then generates the PublicKey from DER bytes.
     */
    public static PublicKey parsePublicKey(String keyValue) {
        try {
            if (keyValue == null || keyValue.trim().isEmpty()) {
                throw new IllegalArgumentException("Public key value is empty");
            }

            // First, decode the base64 to see what we have
            byte[] decodedBytes = Base64.getDecoder().decode(keyValue.replaceAll("\\s", ""));
            
            // Convert to string to check if it's PEM formatted
            String decodedString = new String(decodedBytes);
            
            byte[] derBytes;
            if (decodedString.contains("-----BEGIN")) {
                // It's PEM formatted, extract the base64 part
                derBytes = extractDerFromPem(decodedString);
            } else {
                // It's already DER, use as-is
                derBytes = decodedBytes;
            }

            // Generate the public key from DER bytes
            X509EncodedKeySpec spec = new X509EncodedKeySpec(derBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse public key: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts DER bytes from PEM formatted key string.
     */
    private static byte[] extractDerFromPem(String pem) {
        // Remove all PEM headers, footers, and whitespace
        String base64Content = pem
                .replaceAll("-----BEGIN.*?-----", "")
                .replaceAll("-----END.*?-----", "")
                .replaceAll("\\s", "");
        
        // Decode to get DER bytes
        return Base64.getDecoder().decode(base64Content);
    }
}
