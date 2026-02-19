package com.seaandtea.repository;

import com.seaandtea.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE (:role IS NULL OR u.role = :role) AND (:isActive IS NULL OR u.isActive = :isActive) ORDER BY u.createdAt DESC")
    Page<User> findAllForAdmin(@Param("role") User.UserRole role, @Param("isActive") Boolean isActive, Pageable pageable);
}

