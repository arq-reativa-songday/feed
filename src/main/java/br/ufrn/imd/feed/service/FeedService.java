package br.ufrn.imd.feed.service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.feed.client.SongDayClient;
import br.ufrn.imd.feed.client.SongsClient;
import br.ufrn.imd.feed.dto.PostDto;
import br.ufrn.imd.feed.dto.SearchPostsCountDto;
import br.ufrn.imd.feed.dto.SearchPostsDto;
import br.ufrn.imd.feed.dto.SongDto;
import br.ufrn.imd.feed.exception.NotFoundException;
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
                    if (throwable.getLocalizedMessage().contains("404 Not Found")) {
                        return Mono.error(new NotFoundException("Usuário não encontrado"));
                    }
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

    private SongDto findSongById(String id) {
        return songsClient.findById(id)
                .onErrorResume(throwable -> {
                    if (throwable.getLocalizedMessage().contains("404 Not Found")) {
                        return Mono.empty();
                    }
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com Songs para recuperar a música: "
                                    + throwable.getLocalizedMessage()));
                }).block();
    }

    public List<PostDto> findFeedPosts(String username, int offset, int limit) {
        // buscar pessoas que o usuário segue
        Set<String> followees = this.findFollowees(username);

        // buscar posts para o feed
        List<PostDto> posts = this.findPosts(new SearchPostsDto(offset, limit, followees));

        return posts.stream().map(post -> {
            // para cada post, buscar os dados da música
            SongDto song = this.findSongById(post.getSongId());
            if (song != null)
                post.setSong(song);
            return post;
        }).collect(Collectors.toList());
    }

    // public List<PostDto> findFeedPosts(String username, int offset, int limit) {
    //     // buscar pessoas que o usuário segue
    //     Set<String> followees = this.findFollowees(username);

    //     // buscar posts para o feed
    //     List<PostDto> posts = this.findPosts(new SearchPostsDto(offset, limit, followees));

    //     // buscar músicas
    //     List<String> idsSongs = posts.stream().map(p -> p.getSongId()).collect(Collectors.toList());
    //     List<SongDto> songs = songsClient.findAllById(idsSongs).block();

    //     return posts.stream().map(post -> {
    //         Optional<SongDto> song = songs.stream()
    //                 .filter(s -> s.getId().equals(post.getSongId()))
    //                 .findFirst();

    //         if (song.isPresent()) {
    //             post.setSong(song.get());
    //         }

    //         return post;
    //     }).collect(Collectors.toList());
    // }
}
