package br.ufrn.imd.feed.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import br.ufrn.imd.feed.dto.SongDto;
import reactor.core.publisher.Mono;

@HttpExchange("songs")
public interface SongsClient {
    @GetExchange(value = "/songs/count")
    public Mono<Long> count();

    @GetExchange(value = "/songs/{id}")
    public Mono<SongDto> findById(@PathVariable String id);

    // @GetExchange(value = "/songs/search")
    // public Mono<List<SongDto>> findAllById(@RequestBody List<String> ids);
}
