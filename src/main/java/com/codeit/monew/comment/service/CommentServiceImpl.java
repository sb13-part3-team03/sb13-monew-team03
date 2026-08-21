package com.codeit.monew.comment.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.dto.command.*;
import com.codeit.monew.comment.dto.command.comment.CommentCreateCommand;
import com.codeit.monew.comment.dto.command.comment.CommentQueryCommand;
import com.codeit.monew.comment.dto.command.comment.CommentUpdateCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;
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
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final String SERVICE_NAME = "Comment";

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto registry(CommentCreateCommand command){

        log.info("{} - Comment Registry requested By User : {}",SERVICE_NAME,command.userId());

        Article article = getArticleOrExcept(command.articleId());
        User user = getUserOrExcept(command.userId());

        log.debug(
                "{} - Comment Registry - articleId : {}, userId : {}",
                SERVICE_NAME,
                article.getId(),
                user.getId()
        );

        Comment comment = commentRepository.save(
                new Comment(
                        article,
                        user,
                        command.content()
                )
        );

        return getCommentDtoFromComment(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorContainerDto<CommentDto> query(CommentQueryCommand command){

        log.info("{} - Comment Queried by ID : {}",SERVICE_NAME,command.requestUserId());

        Slice<CommentDtoCreateCommand> createDtoCommands = commentRepository.getAllCommentsWithCursor(command);

        log.debug("{} - repository return objects : size - {}", SERVICE_NAME, createDtoCommands.getSize());

        return commentMapper.toDto(
                getCursorContainerCommand(
                        createDtoCommands.getContent(),
                        createDtoCommands.getSize(),
                        getCommentsCountConditionedByArticle(command.articleId()),
                        createDtoCommands.hasNext(),
                        "likeCount".equals(command.orderBy())   // order attribute check - change enum to after
                )
        );
    }

    @Override
    @Transactional
    public CommentDto update(CommentUpdateCommand command){

        Comment comment = getCommentById(command.commentId());

        authCheck(comment, command.userId());

        // value checked request dto
        comment.update(command.content());

        Comment result = commentRepository.save(comment);

        return getCommentDtoFromComment(result);
    }

    // todo - if all header has monew login user id, check comment owner is same user
    // now the header dose not send to comment delete.
    @Override
    @Transactional
    public void mask(UUID commentId){
        // logical delete.

        log.info("{} - Comment Marked like Deleted : id - {}", SERVICE_NAME, commentId);

        Comment comment = getCommentById(commentId);

        // add value in deletedAt field
        comment.delete();

        // not delete instance in now.
        commentRepository.save(comment);
    }

    // todo - if all header has monew login user id, check comment owner is same user
    // now the header dose not send to comment delete.
    @Override
    @Transactional
    public void delete(UUID commentId){
        // delete comment like info associate comment

        log.info("{} - Comment Deleted : id - {}", SERVICE_NAME, commentId);

        Comment comment = getCommentById(commentId);

        // delete associate like
        commentLikeRepository.deleteAllByComment(comment);

        commentRepository.delete(comment);

    }

    private void authCheck(Comment comment,  UUID userId){
        if (!comment.getUser().getId().equals(userId))
            throw new CommentException(ErrorCode.COMMENT_FORBIDDEN);
    }

    private Long getCommentsCountConditionedByArticle(UUID articleId){
        // comment count is determined by article id.
        // therefore, article id is taken required condition.
        if (articleId == null) return commentRepository.countByDeletedAtIsNull();
        return commentRepository.countAllByDeletedAtIsNullAndArticleId(articleId);
    }


    // get CreateCursorDtoCommand
    private CursorContainerCreateCommand<CommentDto> getCursorContainerCommand(
            List<CommentDtoCreateCommand> contents,
            int size,
            Long totalElement,
            Boolean hasNext,
            Boolean orderByLikeCount
    ){
        // default values fot nextCursor and nextAfter
        String next = "";
        String after = "";

        log.debug("got contents - {}",contents);

        // cursor and next after
        List<CommentDto> comments = contents.stream().map(commentMapper::toDto).toList();

        log.debug("commentDto list: {}",comments);

        // protect null point exception when comment query is 0.
        if (!comments.isEmpty()){
            CommentDto last = comments.get(comments.size() - 1);

            log.debug("last comment info is - {}",last);

            next = orderByLikeCount
                    ? last.likeCount().toString()
                    : last.createdAt().toString();

            after = last.createdAt().toString();
        }

        return new CursorContainerCreateCommand<>(
                comments,
                next,
                after,
                (long) size,
                totalElement,
                hasNext
        );
    }

    private Comment getCommentById(UUID commentId){
        return commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private Article getArticleOrExcept(UUID articleId){
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_ARTICLE_NOT_FOUND));
    }

    private User getUserOrExcept(UUID userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_USER_NOT_FOUND));
    }

    private CommentDto getCommentDtoFromComment(Comment comment){
        CommentDtoCreateCommand command = commentRepository.getDtoCommandById(comment.getId())
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND));

        return commentMapper.toDto(command);
    }
}
