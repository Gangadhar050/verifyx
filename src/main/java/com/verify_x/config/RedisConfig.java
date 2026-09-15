//package com.verify_x.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//import org.springframework.cache.CacheManager;
//
//import java.time.Duration;
//
//@Configuration
//public class RedisConfig {
//
//    @Value("${spring.redis.host:localhost}")
//    private String redisHost;
//
//    @Value("${spring.redis.port:6379}")
//    private int redisPort;
//
//    @Value("${cache.default-ttl-seconds:300}")
//    private long defaultTtlSeconds;
//
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        return new LettuceConnectionFactory(redisHost, redisPort);
//    }
//
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
//        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofSeconds(defaultTtlSeconds))
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
//
//        return RedisCacheManager.builder(connectionFactory)
//                .cacheDefaults(config)
//                .build();
//    }
//}
