package com.mt.demo.gateway.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

/**
 * WebSessionConfiguration
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
@Configuration
@EnableRedisWebSession(redisNamespace = "MySpringSession")
public class WebSessionConfiguration {

}
