package com.kakaopay.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory();
	}
	
	@Bean
	public <T> RedisTemplate<T, T> redisTemplate(){
		RedisTemplate<T, T> rt = new RedisTemplate<T, T>();
		rt.setConnectionFactory(redisConnectionFactory());
		rt.setKeySerializer(new StringRedisSerializer());
		rt.setValueSerializer(new StringRedisSerializer());
		
		return rt;
	}
}
