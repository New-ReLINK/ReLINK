package com.my.relink.service;

import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PointService {

    private final PointRepository pointRepository;

    public Point findByIdOrFail(User user){
        return pointRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.error("[포인트 조회 실패] userId = {}", user.getId());
                    return new BusinessException(ErrorCode.POINT_INFO_NOT_FOUND);
                });
    }
}
