package br.ufrn.imd.feed.cache;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.service.FeedService;

@Component
public class PostsCacheLocal {
    @Autowired
    private FeedService feedService;
    private RLocalCachedMap<String, List<PostDto>> map;
    private final Duration timeToLive = Duration.ofSeconds(30);

    public PostsCacheLocal(RedissonClient redissonClient) {
        Codec codec = new TypedJsonJacksonCodec(String.class, List.class);
        LocalCachedMapOptions<String, List<PostDto>> options = LocalCachedMapOptions.<String, List<PostDto>>defaults()
                // .timeToLive(30, TimeUnit.SECONDS)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);

        this.map = redissonClient.getLocalCachedMap("/posts-local/", codec, options);
    }

    public List<PostDto> get(String username, int offset, int limit) {
        String key = getKey(username, offset, limit);
        System.out.println("Obtendo chave " + key + "...");

        List<PostDto> posts = getFromCache(key);
        if (posts == null || posts.isEmpty()) {
            posts = generateFeed(username, offset, limit);
        }
        return posts;
    }

    private List<PostDto> generateFeed(String username, int offset, int limit) {
        String key = getKey(username, offset, limit);
        System.out.println("Gerando novo feed para chave " + key + "...");
        List<PostDto> posts = feedService.findFeedPosts(username, offset, limit);
        updateCache(key, posts);
        return posts;
    }

    private List<PostDto> getFromCache(String key) {    
        System.out.println("Buscando chave " + key + " no cache (local)...");
        return map.get(key);
    }

    private boolean updateCache(String key, List<PostDto> posts) {
        System.out.println("Atualizando chave " + key + " no cache (local)");
        var put = map.fastPut(key, posts);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(timeToLive.toMillis());
                map.fastRemove(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return put;
    }

    private String getKey(String username, int offset, int limit) {
        return String.format("%s-offset:%s_limit:%s", username, offset, limit);
    }
}
