package br.ufrn.imd.feed.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "songs")
public interface SongsClient {
    @GetMapping(value = "/songs/count")
    ResponseEntity<Long> count();
}
