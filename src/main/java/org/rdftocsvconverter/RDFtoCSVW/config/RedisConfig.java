package org.rdftocsvconverter.RDFtoCSVW.config;

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

        // If REDIS_URL is provided (Render.com), parse and use it
        if (redisUrl != null && !redisUrl.isEmpty()) {
            try {
                URI uri = URI.create(redisUrl);
                config = new RedisStandaloneConfiguration();
                config.setHostName(uri.getHost());
                config.setPort(uri.getPort() > 0 ? uri.getPort() : 6379);
                
                // Extract password from URL if present (format: redis://user:password@host:port)
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String password = userInfo.split(":", 2)[1];
                    config.setPassword(password);
                } else if (redisPassword != null && !redisPassword.isEmpty()) {
                    config.setPassword(redisPassword);
                }
            } catch (Exception e) {
                // Fallback to individual properties if URL parsing fails
                System.err.println("Failed to parse REDIS_URL, falling back to individual properties: " + e.getMessage());
                config = new RedisStandaloneConfiguration(redisHost, redisPort);
                if (redisPassword != null && !redisPassword.isEmpty()) {
                    config.setPassword(redisPassword);
                }
            }
        } else {
            // Use individual properties (local development)
            config = new RedisStandaloneConfiguration(redisHost, redisPort);
            if (redisPassword != null && !redisPassword.isEmpty()) {
                config.setPassword(redisPassword);
            }
        }

        return new LettuceConnectionFactory(config);
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
