package br.ufrn.imd.feed.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.feed.client.SongDayClient;
import br.ufrn.imd.feed.client.SongsClient;
import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.SearchPostsCountDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import br.ufrn.imd.feed.exception.ServicesCommunicationException;
import br.ufrn.imd.feed.model.Feed;
import reactor.core.publisher.Mono;

@Service
public class FeedService {
    @Autowired
    private SongDayClient songDayClient;

    @Autowired
    private SongsClient songsClient;

    public Feed generateFeed(String username, Date lastFeedDate, int offset, int limit) {
        // buscar pessoas que o usuário segue
        Set<String> followees = this.findFollowees(username);

        Date updatedAt = new Date();
        // buscar posts para o feed
        List<PostDto> posts = this.findPosts(new SearchPostsDto(offset, limit, followees));

        if (posts.size() > 0) {
            Date mostRecentPostDate = posts.get(0).getCreatedAt();
            updatedAt = new Date(mostRecentPostDate.getTime() + 1);
        }

        Long newsPosts = null;
        if (lastFeedDate != null) {
            newsPosts = findPostsCount(new SearchPostsCountDto(lastFeedDate, updatedAt, followees));
        }

        Long songsCount = this.countSongs();

        // montar feed
        return Feed.builder()
                .username(username)
                .updatedAt(updatedAt)
                .posts(posts)
                .offset(offset)
                .limit(limit)
                .size(posts.size())
                .newsPosts(newsPosts)
                .totalSongs(songsCount)
                .build();
    }

    private List<PostDto> findPosts(SearchPostsDto searchPostsDto) {
        return songDayClient.getAll(searchPostsDto)
                .onErrorResume(throwable -> {
                    if (throwable.getLocalizedMessage().contains("404 Not Found")) {
                        return Mono.empty();
                    }
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com SongDay para recuperar as publicações: "
                                    + throwable.getLocalizedMessage()));
                })
                .collectList().block();
    }

    private Long findPostsCount(SearchPostsCountDto searchPostsCountDto) {
        return songDayClient.searchPostsCount(searchPostsCountDto)
                .onErrorResume(throwable -> {
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com SongDay para recuperar a quantidade de novas publicações: "
                                    + throwable.getLocalizedMessage()));
                })
                .block();
    }

    private Set<String> findFollowees(String username) {
        return songDayClient.getFolloweesByUsername(username)
                .onErrorResume(throwable -> {
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com SongDay para recuperar os usuários seguidos: "
                                    + throwable.getLocalizedMessage()));
                })
                .block();
    }

    private Long countSongs() {
        return songsClient.count()
                .onErrorResume(throwable -> {
                    return Mono.just(0L);
                }).block();
    }
}
