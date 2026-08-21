package com.codeit.monew.article.mapper;

import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.article.dto.response.ArticleViewResult;
import com.codeit.monew.article.entity.ArticleView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleViewMapper {

    @Mapping(source = "articleView.id", target = "id")
    @Mapping(source = "articleView.user.id", target = "viewedBy")
    @Mapping(source = "articleView.createdAt", target = "createdAt")
    @Mapping(source = "articleView.article.id", target = "articleId")
    @Mapping(source = "articleView.article.source", target = "source")
    @Mapping(source = "articleView.article.sourceUrl", target = "sourceUrl")
    @Mapping(source = "articleView.article.title", target = "articleTitle")
    @Mapping(source = "articleView.article.publishDate", target = "articlePublishedDate")
    @Mapping(source = "articleView.article.summary", target = "articleSummary")
    @Mapping(source = "commentCount", target = "articleCommentCount")
    @Mapping(source = "viewCount", target = "articleViewCount")
    ArticleViewDto toDto(ArticleViewResult result);

}
