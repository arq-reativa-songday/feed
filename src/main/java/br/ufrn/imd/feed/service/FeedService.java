package br.ufrn.imd.feed.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import br.ufrn.imd.feed.client.SongDayClient;
import br.ufrn.imd.feed.client.SongsClient;
import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.SearchPostsCountDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import br.ufrn.imd.feed.exception.NotFoundException;
import br.ufrn.imd.feed.exception.ServicesCommunicationException;
import br.ufrn.imd.feed.model.Feed;
import feign.FeignException;

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

        Integer newsPosts = null;
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
        try {
            ResponseEntity<List<PostDto>> response = songDayClient.findPosts(searchPostsDto);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return Collections.emptyList();
            }
            return response.getBody();
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException(e.getLocalizedMessage());
            } else {
                e.printStackTrace();
                throw new ServicesCommunicationException(
                        "Erro durante a comunicação com SongDay para recuperar publicações");
            }
        }
    }

    private Integer findPostsCount(SearchPostsCountDto searchPostsCountDto) {
        try {
            ResponseEntity<Integer> response = songDayClient.findPostsCount(searchPostsCountDto);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return 0;
            }
            return response.getBody();
        } catch (FeignException e) {
            e.printStackTrace();
            throw new ServicesCommunicationException(
                    "Erro durante a comunicação com SongDay a quantidade de novas publicações");
        }
    }

    private Set<String> findFollowees(String username) {
        try {
            ResponseEntity<Set<String>> response = songDayClient.findFollowees(username);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return Collections.emptySet();
            }
            return response.getBody();
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException(e.getLocalizedMessage());
            } else {
                e.printStackTrace();
                throw new ServicesCommunicationException(
                        "Erro durante a comunicação com SongDay para recuperar usuários seguidos");
            }
        }
    }

    private Long countSongs() {
        try {
            ResponseEntity<Long> response = songsClient.count();
            if (!response.getStatusCode().is2xxSuccessful()) {
                return 0L;
            }
            return response.getBody();
        } catch (FeignException e) {
            return 0L;
        }
    }
}
