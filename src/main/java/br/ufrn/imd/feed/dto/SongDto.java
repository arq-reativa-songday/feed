package br.ufrn.imd.feed.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongDto {
    private String id;
    private String name;
    private String artist;
    private String genre;
}
