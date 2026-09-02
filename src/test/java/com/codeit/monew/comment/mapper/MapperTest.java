package com.codeit.monew.comment.mapper;

import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.dto.command.CommentLikeDtoCreateCommand;
import com.codeit.monew.comment.dto.command.CursorContainerCreateCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CommentLikeDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("mapper test")
public class MapperTest {


    CommentMapper commentMapper = new CommentMapper();

    @Test
    @DisplayName("CommentDto convert test")
    void commentDtoTest(){
        UUID article = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        UUID comment = UUID.randomUUID();

        CommentDtoCreateCommand command = new CommentDtoCreateCommand(
                comment,
                user,
                article,
                "kims",
                "contents",
                0L,
                false,
                Instant.now()
        );

        CommentDto dto = commentMapper.toDto(command);

        assertThat(dto.id()).isEqualTo(comment);

    }

    @Test
    @DisplayName("CommentLikeDto test")
    void commentLikeDtoTest(){
        CommentLikeDtoCreateCommand command = new CommentLikeDtoCreateCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(),
                null,
                null,
                null,
                null,
                "content",
                0L,
                null
        );

        CommentLikeDto dto = commentMapper.toDto(command);

        assertThat(dto.id()).isEqualTo(command.id());
    }

    @Test
    @DisplayName("Cursor pagenation test")
    void cursorDtoTest(){
        CursorContainerCreateCommand<CommentDto> command = new CursorContainerCreateCommand<>(
                List.of(),
                "",
                "",
                0L,
                0L,
                false
        );

        CursorContainerDto<CommentDto> dto = commentMapper.toDto(command);

        assertThat(dto.hasNext()).isFalse();

    }

}
