package com.codeit.monew.article.mapper;

import com.codeit.monew.article.dto.response.ArticleDto;
import com.codeit.monew.article.dto.response.ArticleSearchResult;
import com.codeit.monew.article.entity.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    @Mapping(source = "article.id", target = "id")
    @Mapping(source = "article.source", target = "source")
    @Mapping(source = "article.sourceUrl", target = "sourceUrl")
    @Mapping(source = "article.title", target = "title")
    @Mapping(source = "article.publishDate", target = "publishDate")
    @Mapping(source = "article.summary", target = "summary")
    @Mapping(source = "commentCount", target = "commentCount")
    @Mapping(source = "viewCount", target = "viewCount")
    ArticleDto toDto(ArticleSearchResult result);

    @Mapping(source = "article.id", target = "id")
    @Mapping(source = "article.source", target = "source")
    @Mapping(source = "article.sourceUrl", target = "sourceUrl")
    @Mapping(source = "article.title", target = "title")
    @Mapping(source = "article.publishDate", target = "publishDate")
    @Mapping(source = "article.summary", target = "summary")
    @Mapping(source = "viewedByMe", target = "viewedByMe")
    ArticleDto toDto(Article article, Long commentCount, Long viewCount, Boolean viewedByMe);

    List<ArticleDto> toDtoList(List<ArticleSearchResult> articles);

}
