package br.ufrn.imd.feed.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import reactor.core.publisher.Mono;

@HttpExchange("songs")
public interface SongsClient {
    @GetExchange(value = "/songs/count")
    public Mono<Long> count();
}
