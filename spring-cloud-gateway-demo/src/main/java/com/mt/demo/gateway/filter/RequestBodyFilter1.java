package com.mt.demo.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * RequestBodyFilter
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
//@Component
public class RequestBodyFilter1 implements GlobalFilter, Ordered {

    private static final List<HttpMessageReader<?>> MESSAGE_READERS = HandlerStrategies.withDefaults().messageReaders();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (HttpMethod.POST.equals(exchange.getRequest().getMethod()) && null != exchange.getRequest().getHeaders().getContentType()
                && exchange.getRequest().getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)
                && !exchange.getRequest().getHeaders().getContentType().includes(MediaType.MULTIPART_FORM_DATA)) {

            return readBody(exchange, chain);
        }
        return chain.filter(exchange);
    }

    private Mono<Void> readBody(ServerWebExchange exchange, GatewayFilterChain chain) {
        return DataBufferUtils.join(exchange.getRequest().getBody()).flatMap(dataBuffer -> {
            DataBufferUtils.retain(dataBuffer);
            Flux<DataBuffer> cachedFlux = Flux.defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));

            ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return cachedFlux;
                }
            };
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            return ServerRequest.create(mutatedExchange, MESSAGE_READERS).bodyToMono(String.class)
                    .doOnNext(objectValue -> {
                        log.info("----request body: \n{}\n", objectValue);
                    })
                    .then(chain.filter(mutatedExchange));
        });
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
