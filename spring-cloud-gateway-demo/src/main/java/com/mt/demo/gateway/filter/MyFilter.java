package com.mt.demo.gateway.filter;

import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * MyFilter
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
@Component
public class MyFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("this is a pre filter");

        return exchange.getSession().flatMap(webSession -> {
            log.info("websession: {}", webSession.getId());
            webSession.getAttributes().put(webSession.getId(), "aaaa");

            return chain.filter(exchange);
        }).then(Mono.fromRunnable(() -> {
            log.info("this is a post filter");
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
