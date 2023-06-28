package br.ufrn.imd.feed.model;

import java.util.Date;
import java.util.List;

import br.ufrn.imd.feed.dto.PostDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Feed {
    private String username;
    private Date updatedAt;
    private int offset;
    private int limit;
    private int size;
    private Long newsPosts;
    private List<PostDto> posts;
    private Long totalSongs;
}
