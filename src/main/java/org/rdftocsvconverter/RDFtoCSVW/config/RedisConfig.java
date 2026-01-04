package org.rdftocsvconverter.RDFtoCSVW.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;

/**
 * Redis configuration using JDK serialization to avoid Jackson version conflicts.
 * This approach uses Java's native serialization instead of JSON.
 * Supports both REDIS_URL (production) and individual properties (local development).
 */
@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.url:}")
    private String redisUrl;

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config;

        logger.info("=== Redis Configuration Debug ===");
        logger.info("REDIS_URL from env: {}", redisUrl != null && !redisUrl.isEmpty() ? "SET (length: " + redisUrl.length() + ")" : "NOT SET");
        logger.info("Fallback host: {}", redisHost);
        logger.info("Fallback port: {}", redisPort);

        // If REDIS_URL is provided (Render.com), parse and use it
        if (redisUrl != null && !redisUrl.isEmpty()) {
            try {
                logger.info("Parsing REDIS_URL...");
                URI uri = URI.create(redisUrl);
                config = new RedisStandaloneConfiguration();
                config.setHostName(uri.getHost());
                config.setPort(uri.getPort() > 0 ? uri.getPort() : 6379);
                
                logger.info("Parsed Redis host: {}", uri.getHost());
                logger.info("Parsed Redis port: {}", uri.getPort() > 0 ? uri.getPort() : 6379);
                
                // Extract password from URL if present (format: redis://user:password@host:port)
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String password = userInfo.split(":", 2)[1];
                    config.setPassword(password);
                    logger.info("Using password from REDIS_URL");
                } else if (redisPassword != null && !redisPassword.isEmpty()) {
                    config.setPassword(redisPassword);
                    logger.info("Using password from SPRING_REDIS_PASSWORD");
                } else {
                    logger.info("No password configured");
                }
            } catch (Exception e) {
                // Fallback to individual properties if URL parsing fails
                logger.error("Failed to parse REDIS_URL, falling back to individual properties", e);
                config = new RedisStandaloneConfiguration(redisHost, redisPort);
                if (redisPassword != null && !redisPassword.isEmpty()) {
                    config.setPassword(redisPassword);
                }
                logger.info("Using fallback - host: {}, port: {}", redisHost, redisPort);
            }
        } else {
            // Use individual properties (local development)
            logger.info("REDIS_URL not set, using individual properties");
            config = new RedisStandaloneConfiguration(redisHost, redisPort);
            if (redisPassword != null && !redisPassword.isEmpty()) {
                config.setPassword(redisPassword);
            }
            logger.info("Using host: {}, port: {}", redisHost, redisPort);
        }

        logger.info("Creating LettuceConnectionFactory with host: {}, port: {}", config.getHostName(), config.getPort());
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        logger.info("=== End Redis Configuration ===");
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use JDK serialization to avoid Jackson version conflicts
        JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer();
        template.setValueSerializer(jdkSerializer);
        template.setHashValueSerializer(jdkSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
