package br.ufrn.imd.feed.client;

import java.util.List;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.SearchPostsCountDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@FeignClient(name = "songday")
public interface SongDayClient {
    @GetMapping(value = "/users/username/{username}/followees")
    ResponseEntity<Set<String>> findFollowees(@PathVariable String username);

    @PostMapping(value = "/posts/search")
    ResponseEntity<List<PostDto>> findPosts(@RequestBody SearchPostsDto search);

    @PostMapping(value = "/posts/search/count")
    ResponseEntity<Integer> findPostsCount(@RequestBody SearchPostsCountDto search);
}
