package com.seaandtea.repository;

import com.seaandtea.entity.NewsPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsPostLikeRepository extends JpaRepository<NewsPostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<NewsPostLike> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    @org.springframework.data.jpa.repository.Modifying
    void deleteByPostId(Long postId);
}
