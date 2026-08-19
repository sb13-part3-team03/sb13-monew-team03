package com.codeit.monew.comment.comtroller;


import com.codeit.monew.comment.dto.command.CommentCreateCommand;
import com.codeit.monew.comment.dto.command.CommentQueryCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;
import com.codeit.monew.comment.service.CommentService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentControllerDoc{

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<CursorContainerDto<CommentDto>> getComments(
            @RequestParam UUID articleId,
            @NotNull @RequestParam String orderBy,
            @NotNull @RequestParam String direction,
            @RequestParam String cursor,
            @RequestParam String after,
            @NotNull @RequestParam Integer limit,
            @NotNull @RequestHeader(value = "Monew-Request-User-Id") UUID userId
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
            @RequestBody CommentCreateCommand command
    ){
        return ResponseEntity.ok(commentService.registry(command));
    }


}
