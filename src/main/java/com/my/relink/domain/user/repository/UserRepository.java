package com.my.relink.domain.user.repository;

import com.my.relink.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {
    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    boolean existsByIdAndIsDeletedFalse(Long id);

    @Query("select u from User u where u.email = :email and u.isDeleted = false")
    Optional<User> findByEmailActiveUser(@Param("email") String email);
}
