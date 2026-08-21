package com.codeit.monew.comment.service;

import com.codeit.monew.comment.dto.command.CommentLikeDtoCreateCommand;
import com.codeit.monew.comment.dto.command.like.CommentLikeCancelCommand;
import com.codeit.monew.comment.dto.command.like.CommentLikeRegistryCommand;
import com.codeit.monew.comment.dto.response.CommentLikeDto;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentLikeServiceImpl implements CommentLikeService {

    private final String SERVICE_NAME = "CommentLike";

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentLikeDto registry(CommentLikeRegistryCommand command){

        // todo - duplication depend need

        Comment comment = getCommentOrExcept(command.commentId());
        User user = getUserOrExcept(command.userId());

        log.debug(
                "{} - CommentLike registry requested. user - {}, comment - {}",
                SERVICE_NAME,
                command.userId(),
                command.commentId()
        );

        CommentLike commentLike = commentLikeRepository.save(new CommentLike(comment,user));

        CommentLikeDtoCreateCommand createCommand = new CommentLikeDtoCreateCommand(

                // commentLike information
                commentLike.getId(),
                commentLike.getUser().getId(),
                commentLike.getCreatedAt(),

                // comment information
                comment.getId(),
                comment.getArticle().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                commentLikeRepository.countAllByComment(comment),
                comment.getCreatedAt()
        );


        return commentMapper.toDto(createCommand);
    }

    @Override
    @Transactional
    public void cancel(CommentLikeCancelCommand command){


        Comment comment = getCommentOrExcept(command.commentId());

        if (!comment.getUser().getId().equals(command.userId()))
            throw new CommentException(ErrorCode.COMMENT_INVALID_VALUE);

    }

    // todo - 1 + N 쿼리 해결위해 find 매서드 join 매서드로 별도 생성.

    private User getUserOrExcept(UUID userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_USER_NOT_FOUND));
    }

    private Comment getCommentOrExcept(UUID commentId){
        return commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
