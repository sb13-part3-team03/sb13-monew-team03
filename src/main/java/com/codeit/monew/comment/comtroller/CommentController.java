package com.codeit.monew.comment.comtroller;


import com.codeit.monew.comment.dto.command.CommentCreateCommand;
import com.codeit.monew.comment.dto.command.CommentQueryCommand;
import com.codeit.monew.comment.dto.request.CommentRegisterRequest;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;
import com.codeit.monew.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentControllerDoc{

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<CursorContainerDto<CommentDto>> getComments(
            @RequestParam(required = false) UUID articleId,
            @RequestParam String orderBy,
            @RequestParam String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String after,
            @RequestParam Integer limit,
            @RequestHeader(value = "Monew-Request-User-Id") UUID userId
    ){
        CommentQueryCommand command = new CommentQueryCommand(
                articleId,
                orderBy,
                direction,
                cursor,
                after,
                (long) limit,
                userId
        );

        CursorContainerDto<CommentDto> result = commentService.query(command);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @Validated @RequestBody CommentRegisterRequest request
    ){

        return ResponseEntity.ok(
                commentService.registry(
                        new CommentCreateCommand(
                            request.articleId(),
                            request.userId(),
                            request.content()
                        )
                )
        );
    }


}
