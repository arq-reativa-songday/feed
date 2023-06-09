package br.ufrn.imd.feed.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchPostsDto {
    private int offset;
    private int limit;
    private Set<String> followees;
}
