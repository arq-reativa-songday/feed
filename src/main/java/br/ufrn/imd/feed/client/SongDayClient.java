package br.ufrn.imd.feed.client;

import java.util.Set;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.SearchPostsCountDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@HttpExchange("songday")
public interface SongDayClient {
    @GetExchange(value = "/users/username/{username}/followees")
    public Mono<Set<String>> getFolloweesByUsername(@PathVariable String username);

    @PostExchange(value = "/posts/search")
    public Flux<PostDto> getAll(@Valid @RequestBody SearchPostsDto search);

    @PostExchange(value = "/posts/search/count")
    public Mono<Long> searchPostsCount(@Valid @RequestBody SearchPostsCountDto search);
}
