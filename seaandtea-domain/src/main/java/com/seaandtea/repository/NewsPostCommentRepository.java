package com.seaandtea.repository;

import com.seaandtea.entity.NewsPostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface NewsPostCommentRepository extends JpaRepository<NewsPostComment, Long> {

    @Query(value = "SELECT c FROM NewsPostComment c JOIN FETCH c.user WHERE c.post.id = :postId ORDER BY c.createdAt ASC",
           countQuery = "SELECT COUNT(c) FROM NewsPostComment c WHERE c.post.id = :postId")
    Page<NewsPostComment> findByPostIdOrderByCreatedAtAsc(@Param("postId") Long postId, Pageable pageable);

    long countByPostId(Long postId);

    boolean existsByPostIdAndId(Long postId, Long commentId);

    @org.springframework.data.jpa.repository.Modifying
    void deleteByPostId(Long postId);
}
