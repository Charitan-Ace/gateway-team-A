package ace.charitan.gatewayuser.internal.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

@Service
public class JwtServiceImpl implements JwtInternalService {

    private PrivateKey encPrivateKey;

    public void setEncPrivateKey(PrivateKey encPrivateKey) {
        this.encPrivateKey = encPrivateKey;
    }

    @Override
    public Claims parseJweClaims(String jwe) {
        return Jwts.parser()
                .decryptWith(encPrivateKey)
                .build()
                .parseEncryptedClaims(jwe)
                .getPayload();
    }

    @Override
    public String parseJweContent(String jwe) {
        byte[] payload = Jwts.parser()
                .decryptWith(encPrivateKey)
                .build()
                .parseEncryptedContent(jwe)
                .getPayload();

        return new String(payload, StandardCharsets.UTF_8);
    }

    @Override
    public PrivateKey getKey() {
        return encPrivateKey;
    }

}
