package br.ufrn.imd.feed.cache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.service.FeedService;

@Component
public class PostsCacheRemoto {
    @Autowired
    private FeedService feedService;
    private RMapCache<String, List<PostDto>> map;

    public PostsCacheRemoto(RedissonClient redissonClient) {
        Codec codec = new TypedJsonJacksonCodec(String.class, List.class);
        this.map = redissonClient.getMapCache("/posts-remoto/", codec);
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
        System.out.println("Buscando chave " + key + " no cache (remoto)...");
        return map.get(key);
    }

    private boolean updateCache(String key, List<PostDto> posts) {
        System.out.println("Atualizando chave " + key + " no cache (remoto)");
        return map.fastPut(key, posts, 30, TimeUnit.SECONDS);
    }

    private String getKey(String username, int offset, int limit) {
        return String.format("%s-offset:%s_limit:%s", username, offset, limit);
    }
}
