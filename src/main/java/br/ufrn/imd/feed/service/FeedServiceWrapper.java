package br.ufrn.imd.feed.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.ufrn.imd.feed.cache.PostsCacheLocal;
import br.ufrn.imd.feed.cache.PostsCacheRemoto;
import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.model.Feed;

@Service
public class FeedServiceWrapper {
    @Autowired
    private FeedService service;

    @Autowired
    private PostsCacheRemoto cacheRemoto;

    @Autowired
    private PostsCacheLocal cacheLocal;

    @Value("${api.cached}")
    private Boolean cacheActive;

    @Value("${api.cache.local}")
    private Boolean cacheLocalActive;

    public Feed generateFeed(String username, Date lastFeedDate, int offset, int limit) {
        return service.generateFeed(username, lastFeedDate, offset, limit);
    }

    public List<PostDto> findFeedPosts(String username, int offset, int limit) {
        if (!cacheActive) {
            return service.findFeedPosts(username, offset, limit);
        }

        return cacheLocalActive ? cacheLocal.get(username, offset, limit)
                : cacheRemoto.get(username, offset, limit);
    }
}
