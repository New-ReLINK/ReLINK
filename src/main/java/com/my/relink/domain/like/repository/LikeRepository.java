package com.my.relink.domain.like.repository;

import com.my.relink.domain.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> , CustomLikeRepository{
}
