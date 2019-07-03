package com.mt.demo.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * ResponseBodyFilter
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
@Component
public class ResponseBodyFilter2 implements GlobalFilter, Ordered {
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
                return super.writeWith(DataBufferUtils.join(Flux.from(body))
                        .map(dataBuffer -> {

                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);
                            return content;

                        }).flatMap(bytes -> {

                            MediaType mediaType = serverHttpResponse.getHeaders().getContentType();
                            if (null == mediaType || (!mediaType.includes(MediaType.APPLICATION_JSON) && !mediaType.includes(MediaType.APPLICATION_JSON_UTF8))) {

                            } else {
                                String bodyString = "";
                                int length = bytes.length;

                                if (!ObjectUtils.isEmpty(exchange.getResponse().getHeaders().get(HttpHeaders.CONTENT_ENCODING))
                                        && exchange.getResponse().getHeaders().get(HttpHeaders.CONTENT_ENCODING).contains("gzip")) {
                                    GZIPInputStream gzipInputStream = null;
                                    try {
                                        gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes), length);
                                        StringWriter writer = new StringWriter();
                                        IOUtils.copy(gzipInputStream, writer, StandardCharsets.UTF_8);
                                        bodyString = writer.toString();

                                    } catch (IOException e) {
                                        log.error("====Gzip IO error", e);
                                    } finally {
                                        if (gzipInputStream != null) {
                                            try {
                                                gzipInputStream.close();
                                            } catch (IOException e) {
                                                log.error("===Gzip IO close error", e);
                                            }
                                        }
                                    }
                                } else {
                                    bodyString = new String(bytes, StandardCharsets.UTF_8);
                                }

                                log.info("bodyString: {}", bodyString);
                            }


                            return Mono.just(bufferFactory.wrap(bytes));
                        }));
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