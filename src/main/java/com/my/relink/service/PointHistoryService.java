package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.PointTransactionType;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final PointRepository pointRepository;

    @Transactional
    public void restorePoints(Long tradeId, AuthUser authUser){
        // 해당 Trade에 연결된 PointHistory 확인
        PointHistory pointHistory = pointHistoryRepository.findByTradeIdOrderByCreatedAtDesc(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_HISTORY_NOT_FOUND));

        Point point = pointRepository.findByUserId(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));

        Integer amount = pointHistory.getAmount();
        // 차감된 금액을 복원
        point.restore(amount);
        pointRepository.save(point);

        // 새로운 포인트 이력 생성 (복원 내역)
        PointHistory restorePointHistory = PointHistory.create(
                pointHistory.getAmount(), // 복원 금액은 기존의 음수 금액을 양수로 변경
                PointTransactionType.RETURN,
                point,
                pointHistory.getTrade()
        );
        pointHistoryRepository.save(restorePointHistory);
    }


}
