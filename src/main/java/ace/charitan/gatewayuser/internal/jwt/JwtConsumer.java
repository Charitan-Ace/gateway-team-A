package ace.charitan.gatewayuser.internal.jwt;

import io.jsonwebtoken.security.Jwks;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AbstractConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.PrivateKey;
import java.util.Map;

@Component
public class JwtConsumer extends AbstractConsumerSeekAware {

    private static final Logger logger = LoggerFactory.getLogger(JwtConsumer.class);

    private final JwtServiceImpl jwtService;

    public JwtConsumer(JwtServiceImpl jwtService) {
        this.jwtService = jwtService;
    }

    @KafkaListener(topics = {"key.encryption.private.change"}, groupId = "user-gateway-service")
    public void encPrivateKeyConsumer(String jwkString) {
        try {
            Key jwk = Jwks.parser()
                    .build()
                    .parse(jwkString)
                    .toKey();

            if (jwk instanceof PrivateKey) {
                jwtService.setEncPrivateKey((PrivateKey) jwk);
                logger.info("Encryption {} private key updated", jwk.getFormat());
            }
        } catch (Exception e) {
            logger.error("Error parsing JWK for private key update: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        assignments.keySet().forEach(topicPartition -> {
            if ("key.encryption.private.change".equals(topicPartition.topic())) {
                // Jump back one offset from the current position
                callback.seekRelative(topicPartition.topic(), topicPartition.partition(), -1, false);
            }
        });
    }
}
