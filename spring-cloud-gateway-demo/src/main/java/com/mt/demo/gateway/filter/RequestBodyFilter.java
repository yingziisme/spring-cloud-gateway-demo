package com.mt.demo.gateway.filter;

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
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * RequestBodyFilter
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
//@Component
public class RequestBodyFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (HttpMethod.POST.equals(exchange.getRequest().getMethod()) && null != exchange.getRequest().getHeaders().getContentType()
                && exchange.getRequest().getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)
                && !exchange.getRequest().getHeaders().getContentType().includes(MediaType.MULTIPART_FORM_DATA)) {

            String requestbody = resolveBodyFromRequest(exchange.getRequest());
            log.info("requestbody: \n {}", requestbody);
            return chain.filter(exchange.mutate().request(generateNewRequest(exchange.getRequest(), requestbody)).build());
        }
        return chain.filter(exchange);
    }

    private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        StringBuilder sb = new StringBuilder();

        body.subscribe(buffer -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            String bodyString = new String(bytes, StandardCharsets.UTF_8);
            sb.append(bodyString);
            log.info("======\n{}", bodyString);
            DataBufferUtils.release(buffer);
        });
        return sb.toString();
    }

    private ServerHttpRequest generateNewRequest(ServerHttpRequest request, String requestBody) {
        URI ex = UriComponentsBuilder.fromUri(request.getURI()).build(true).toUri();
        ServerHttpRequest newRequest = request.mutate().uri(ex).build();
        DataBuffer dataBuffer = stringBuffer(requestBody);
        Flux<DataBuffer> flux = Flux.just(dataBuffer);
        newRequest = new ServerHttpRequestDecorator(newRequest) {
            @Override
            public Flux<DataBuffer> getBody() {
                return flux;
            }
        };
        return newRequest;
    }

    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
