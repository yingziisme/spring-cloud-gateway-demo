package com.mt.demo.gateway.filter;

import com.mt.demo.gateway.configuration.Constants;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * RequestBodyFilter
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
@Component
public class RequestBodyFilter2 implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (HttpMethod.POST.equals(exchange.getRequest().getMethod()) && null != exchange.getRequest().getHeaders().getContentType()
                && exchange.getRequest().getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)
                && !exchange.getRequest().getHeaders().getContentType().includes(MediaType.MULTIPART_FORM_DATA)) {

            return DataBufferUtils.join(exchange.getRequest().getBody()).map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                return bytes;
            }).flatMap(bodyBytes -> {
                String msg = new String(bodyBytes, StandardCharsets.UTF_8);
                log.info("requestBody: \n {}", msg);
                exchange.getAttributes().put(Constants.CACHE_REQUEST_BODY, msg);
                return chain.filter(exchange.mutate().request(generateNewRequest(exchange.getRequest(), bodyBytes)).build());
            });
        }
        return chain.filter(exchange);
    }

    private ServerHttpRequest generateNewRequest(ServerHttpRequest request, byte[] bytes) {
        URI ex = UriComponentsBuilder.fromUri(request.getURI()).build(true).toUri();
        ServerHttpRequest newRequest = request.mutate().uri(ex).build();
        DataBuffer dataBuffer = stringBuffer(bytes);
        Flux<DataBuffer> flux = Flux.just(dataBuffer);
        newRequest = new ServerHttpRequestDecorator(newRequest) {
            @Override
            public Flux<DataBuffer> getBody() {
                return flux;
            }
        };
        return newRequest;
    }

    private DataBuffer stringBuffer(byte[] bytes) {
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        return nettyDataBufferFactory.wrap(bytes);
    }


    @Override
    public int getOrder() {
        return -5;
    }
}
