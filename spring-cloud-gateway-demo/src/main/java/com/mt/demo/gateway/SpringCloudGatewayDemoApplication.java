package com.mt.demo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;


@EnableDiscoveryClient
@SpringBootApplication
public class SpringCloudGatewayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayDemoApplication.class, args);
    }


    /**
     * 自定义限流标志的key
     * exchange对象中获取请求信息，用户信息等
     *
     * @return key
     */
    @Bean
    KeyResolver myKeyResolver() {
//        return exchange -> Mono.just(System.nanoTime() + "");
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }
}
