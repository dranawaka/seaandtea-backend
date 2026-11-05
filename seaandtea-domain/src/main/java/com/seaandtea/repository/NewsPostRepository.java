package com.seaandtea.repository;

import com.seaandtea.entity.NewsPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsPostRepository extends JpaRepository<NewsPost, Long> {

    @Query("SELECT p FROM NewsPost p LEFT JOIN FETCH p.author WHERE p.isPublished = true ORDER BY p.createdAt DESC")
    Page<NewsPost> findPublishedOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM NewsPost p LEFT JOIN FETCH p.author ORDER BY p.createdAt DESC")
    Page<NewsPost> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM NewsPost p LEFT JOIN FETCH p.author WHERE p.id = :id")
    java.util.Optional<NewsPost> findByIdWithAuthor(@Param("id") Long id);
}
