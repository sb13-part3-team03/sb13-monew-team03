package com.codeit.monew.comment.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.dto.command.CommentCreateCommand;
import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.exception.CommentException;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.global.exception.ErrorCode;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto registry(CommentCreateCommand command){
        Article article = getArticleOrExcept(command.articleId());
        User user = getUserOrExcept(command.userId());

        Comment comment = commentRepository.save(
                new Comment(
                        article,
                        user,
                        command.content()
                )
        );

        return getCommentDtoFromComment(comment);
    }

    private Article getArticleOrExcept(UUID articleId){
        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty()) throw new CommentException(ErrorCode.COMMENT_ARTICLE_NOT_FOUND);
        return article.get();
    }

    private User getUserOrExcept(UUID userId){
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new CommentException(ErrorCode.COMMENT_USER_NOT_FOUND);
        return user.get();
    }

    private CommentDto getCommentDtoFromComment(Comment comment){
        CommentDtoCreateCommand command = commentRepository.getDtoCommandById(comment.getId())
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND));

        return commentMapper.toDto(command);
    }
}
