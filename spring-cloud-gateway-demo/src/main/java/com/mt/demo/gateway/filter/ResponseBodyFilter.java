package com.mt.demo.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * ResponseBodyFilter
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
//@Component
public class ResponseBodyFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return chain.filter(exchange.mutate().response(responseDecorator(exchange)).build());
    }

    private ServerHttpResponseDecorator responseDecorator(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = serverHttpResponse.bufferFactory();

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                MediaType mediaType = exchange.getResponse().getHeaders().getContentType();
                if (null == mediaType || (!mediaType.includes(MediaType.APPLICATION_JSON) && !mediaType.includes(MediaType.APPLICATION_JSON_UTF8))) {
                    log.info("===not support for the mediaType : {}", mediaType);
                    return super.writeWith(body);
                }

                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> flux = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(flux.map(databuffer -> {
                        byte[] bytes = new byte[databuffer.readableByteCount()];
                        databuffer.read(bytes);
                        DataBufferUtils.release(databuffer);
                        String responseBody = new String(bytes, StandardCharsets.UTF_8);
                        log.info("responseBody: \n{}", responseBody);
                        return bufferFactory.wrap(bytes);
                    }));
                }

                return super.writeWith(body);
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }
        };
    }

    @Override
    public int getOrder() {
        return -4;
    }
}
