package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {
    void deleteAllByComment_Article_Id(UUID id);
}
