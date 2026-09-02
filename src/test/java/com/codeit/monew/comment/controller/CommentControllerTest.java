package com.codeit.monew.comment.controller;

import com.codeit.monew.comment.dto.command.comment.CommentCreateCommand;
import com.codeit.monew.comment.dto.command.comment.CommentQueryCommand;
import com.codeit.monew.comment.dto.command.comment.CommentUpdateCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;
import com.codeit.monew.comment.service.CommentLikeServiceImpl;
import com.codeit.monew.comment.service.CommentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import({
        CommentController.class
})
@DisplayName("comment controller test")
@Slf4j
public class CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CommentServiceImpl commentService;

    @MockitoBean
    private CommentLikeServiceImpl commentLikeService;

    private String commentCreateRequestJson(UUID article, UUID user, String content){
        return String.join(" ",
                "{\n",
                "\t\"articleId\"",": \"" + article.toString() + "\",",
                "\t\"userId\"",": \"" + user.toString() + "\",",
                "\t\"content\"",": \"" + content + "\"",
                "\n}"
                );
    }

    private CommentDto commentDtoDemo(UUID articleId, UUID userId){
        return new CommentDto(
                UUID.randomUUID(),
                articleId,
                userId,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Nested
    @DisplayName("/api/comments POST test")
    class RegistryTest{
        @Test
        @DisplayName("success test")
        void success() throws Exception{
            UUID article = UUID.randomUUID();
            UUID user = UUID.randomUUID();


            given(commentService.registry(any(CommentCreateCommand.class)))
                    .willReturn(commentDtoDemo(article,user));

            mvc.perform(
                    post("/api/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(commentCreateRequestJson(article,user,"contents"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.articleId").value(article.toString()))
                    .andExpect(jsonPath("$.userId").value(user.toString()));

            // check method called onetime
            verify(commentService,times(1)).registry(any());
        }

    }


    @Nested
    @DisplayName("/api/comments GET test")
    class QueryTest{
        @Test
        @DisplayName("success test")
        void QuerySuccess() throws Exception{
            given(commentService.query(any(CommentQueryCommand.class)))
                    .willReturn(mock(CursorContainerDto.class));

            mvc.perform(get("/api/comments")
                            .param("articleId",UUID.randomUUID().toString())
                            .param("orderBy","createdAt")
                            .param("direction","DESC")
                            .param("limit","50")
                            .header("Monew-Request-User-Id",UUID.randomUUID().toString())
                    )
                    .andExpect(status().isOk());

            verify(commentService,times(1)).query(any());
        }
    }

    @Nested
    @DisplayName("/api/comments/{commentId} Patch test")
    class ModifyTest{
        @Test
        @DisplayName("success test")
        void QuerySuccess() throws Exception{
            UUID comment = UUID.randomUUID();

            given(commentService.update(any(CommentUpdateCommand.class)))
                    .willReturn(mock(CommentDto.class));


            mvc.perform(patch("/api/comments/" + comment)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\" : \"new contents.\"}")
                            .header("Monew-Request-User-Id",UUID.randomUUID().toString())
                    )
                    .andExpect(status().isOk());

            verify(commentService,times(1)).update(any());
        }
    }

    @Nested
    @DisplayName("/api/comments/{commentId} Delete test")
    class DeleteTest{
        @Test
        @DisplayName("success test")
        void MaskSuccess() throws Exception{
            UUID comment = UUID.randomUUID();

            mvc.perform(delete("/api/comments/" + comment))
                    .andExpect(status().isNoContent());

            verify(commentService,times(1)).mask(any(UUID.class));
        }

        @Test
        @DisplayName("success test")
        void DeleteSuccess() throws Exception{
            UUID comment = UUID.randomUUID();

            mvc.perform(delete("/api/comments/" + comment + "/hard"))
                    .andExpect(status().isNoContent());

            verify(commentService,times(1)).delete(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("CommentLike Controller")
    class CommentLike{
        @Test
        @DisplayName("registryTest")
        void registry() throws Exception {
            UUID comment = UUID.randomUUID();
            mvc.perform(
                    post("/api/comments/" + comment + "/comment-likes")
                    .header("Monew-Request-User-Id",UUID.randomUUID().toString())
            ).andExpect(status().isOk());

            verify(commentLikeService,times(1)).registry(any());
        }
    }

    @Test
    @DisplayName("cancelTest")
    void cancel() throws Exception {
        UUID comment = UUID.randomUUID();
        mvc.perform(
                delete("/api/comments/" + comment + "/comment-likes")
                        .header("Monew-Request-User-Id",UUID.randomUUID().toString())
        ).andExpect(status().isNoContent());

        verify(commentLikeService,times(1)).cancel(any());
    }

}
