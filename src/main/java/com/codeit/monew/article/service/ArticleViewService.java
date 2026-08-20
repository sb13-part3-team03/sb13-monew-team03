package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.command.ArticleViewCreateCommand;
import com.codeit.monew.article.dto.response.ArticleViewDto;
import com.codeit.monew.article.dto.response.ArticleViewResult;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleView;
import com.codeit.monew.article.exception.ArticleNotFoundException;
import com.codeit.monew.article.mapper.ArticleViewMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleViewService {

    private final ArticleViewRepository articleViewRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleViewMapper articleViewMapper;
    private final ArticleViewSaveService articleViewSaveService;

    @Transactional
    public ArticleViewDto save(ArticleViewCreateCommand command) {
        UUID articleId = command.articleId();
        UUID userId = command.userId();

        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        ArticleView articleView =
                articleViewRepository.findByArticleIdAndUserId(articleId, userId)
                        .orElseGet(() -> articleViewSaveService.save(article, user));

        Long commentCount =
                commentRepository.countAllByDeletedAtIsNullAndArticleId(articleId);

        Long viewCount =
                articleViewRepository.countByArticleId(articleId);

        ArticleViewResult result =
                new ArticleViewResult(articleView, commentCount, viewCount);

        return articleViewMapper.toDto(result);
    }

}
