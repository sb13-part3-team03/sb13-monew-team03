package com.codeit.monew.comment.mapper;

import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
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

}
