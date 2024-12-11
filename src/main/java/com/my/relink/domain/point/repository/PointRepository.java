package com.my.relink.domain.point.repository;

import com.my.relink.domain.point.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByUserId(Long userId);

}
