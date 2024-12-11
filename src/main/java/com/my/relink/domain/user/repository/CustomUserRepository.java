package com.my.relink.domain.user.repository;

import com.my.relink.domain.user.repository.dto.UserInfoWithCountRepositoryDto;

import java.util.Optional;

public interface CustomUserRepository {

    Optional<UserInfoWithCountRepositoryDto> findUserDetailInfo(Long userId);

    Double avgStar(Long userId);
}
