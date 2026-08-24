package com.codeit.monew.comment.mapper;

import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.dto.command.CommentLikeDtoCreateCommand;
import com.codeit.monew.comment.dto.command.CursorContainerCreateCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CommentLikeDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;
import org.springframework.stereotype.Component;


@Component
public class CommentMapper {

    public CommentDto toDto(CommentDtoCreateCommand command){
        return new CommentDto(
                command.id(),
                command.articleId(),
                command.userId(),
                command.userNickName(),
                command.content(),
                command.likeCount(),
                command.likeByMe(),
                command.createdAt()
        );
    }

    public CursorContainerDto<CommentDto> toDto (CursorContainerCreateCommand<CommentDto> command){
        return new CursorContainerDto<>(
                command.contents(),
                command.nextCursor(),
                command.nextAfter(),
                command.size(),
                command.totalElement(),
                command.hasNext()
        );
    }

    public CommentLikeDto toDto (CommentLikeDtoCreateCommand command){
        return new CommentLikeDto(
                command.id(),
                command.likeBy(),
                command.createdAt(),
                command.commentId(),
                command.articleId(),
                command.commentUserId(),
                command.commentUserNickname(),
                command.commentContent(),
                command.commentLikeCount(),
                command.commentCreateAt()
        );
    }

}
