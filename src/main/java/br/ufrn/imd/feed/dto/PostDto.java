package br.ufrn.imd.feed.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostDto {
    private String id;
    private Date createdAt;
    private String username;
    private String songId;
    private SongDto song;
    private String text;
    private int likes;
    private int comments;
}
