package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.repository.querydsl.CommentRepositoryDsl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryDsl {

    long count();

    Long countAllByArticleId(UUID id);
}
