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
import com.codeit.monew.notification.enums.ResourceType;
import com.codeit.monew.notification.service.NotificationService;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentLikeServiceImpl implements CommentLikeService {

    private final String SERVICE_NAME = "CommentLike";

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentLikeDto registry(CommentLikeRegistryCommand command){

        if (commentLikeRepository.existsByComment_IdAndUser_Id(command.commentId(),command.userId()))
            throw new CommentException(ErrorCode.COMMENT_LIKE_ALREADY_EXISTED);


        Comment comment = getCommentOrExcept(command.commentId());
        User user = getUserOrExcept(command.userId());

        log.debug(
                "{} - CommentLike registry requested. user - {}, comment - {}",
                SERVICE_NAME,
                command.userId(),
                command.commentId()
        );

        CommentLike commentLike = commentLikeRepository.save(new CommentLike(comment,user));

        // 댓글 좋아요 누를 시 해당 사용자에게 알림 생성, 본인이 좋아요 누를 시 생성안함
        User commentAuthor = comment.getUser();
        if (!Objects.equals(commentAuthor.getId(), user.getId())) {
            notificationService.publishCreateEvent(
                    user.getNickname() + "님이 나의 댓글을 좋아합니다.",
                    commentAuthor.getId(),
                    ResourceType.COMMENT,
                    comment.getId()
            );
        }

        CommentLikeDtoCreateCommand createCommand = commentLikeRepository.findCommentLikeByIdToDtoCommand(commentLike.getId());

        return commentMapper.toDto(createCommand);
    }

    @Override
    @Transactional
    public void cancel(CommentLikeCancelCommand command){
        CommentLike commentLike = getCommentLikeOrExcept(command.commentId(),command.userId());
        commentLikeRepository.delete(commentLike);
    }

    private User getUserOrExcept(UUID userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_USER_NOT_FOUND));
    }

    private Comment getCommentOrExcept(UUID commentId){
        return commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private CommentLike getCommentLikeOrExcept(UUID commentId, UUID userId){
        return commentLikeRepository.findByComment_IdAndUser_Id(commentId,userId)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_LIKE_NOT_FOUND));
    }

}
