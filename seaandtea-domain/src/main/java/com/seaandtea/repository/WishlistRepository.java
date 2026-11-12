package com.seaandtea.repository;

import com.seaandtea.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Wishlist> findByUserIdAndTourId(Long userId, Long tourId);

    void deleteByUserIdAndTourId(Long userId, Long tourId);

    boolean existsByUserIdAndTourId(Long userId, Long tourId);
}
