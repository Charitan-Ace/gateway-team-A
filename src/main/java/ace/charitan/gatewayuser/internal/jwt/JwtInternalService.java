package ace.charitan.gatewayuser.internal.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.OctetPrivateJwk;
import io.jsonwebtoken.security.RsaPrivateJwk;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface JwtInternalService {

    Claims parseJweClaims(String jwe);
    String parseJweContent(String jwe);

    PrivateKey getKey();
}
