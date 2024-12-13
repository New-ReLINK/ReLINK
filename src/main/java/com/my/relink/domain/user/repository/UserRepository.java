package com.my.relink.domain.user.repository;

import com.my.relink.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {
    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

}
