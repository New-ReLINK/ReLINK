package com.my.relink.service;

import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.PointTransactionType;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.dto.TradeRequestResponseDto;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public TradeRequestResponseDto requestTrade(Long tradeId, Long userId) {//추후 로그인 유저로 바뀔 예정

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId).
                orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        //해당 로그인 유저의 포인트를 조회
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(()-> new BusinessException(ErrorCode.POINT_NOT_FOUND));
        if(point.getAmount()<trade.getOwnerExchangeItem().getDeposit()){
            throw new BusinessException(ErrorCode.POINT_SHORTAGE);
        }

        //포인트 차감
        point.deduct(trade.getOwnerExchangeItem().getDeposit());
        pointRepository.save(point);

        //포인트 이력 생성
        PointHistory pointHistory = PointHistory.create(-trade.getOwnerExchangeItem().getDeposit(), PointTransactionType.DEPOSIT, point, trade);
        pointHistoryRepository.save(pointHistory);

        //요청자/소유자 여부에 따라 적절한 요청 상태 필드 업데이트
        if(trade.getRequester().getId().equals(currentUser.getId())){
            trade.updateHasRequesterRequested(true);
        } else{
            trade.updateHasOwnerRequested(true);
        }
        // 양쪽 모두 requested가 true라면 거래 상태를 in_exchange로 변경
        if(trade.getHasRequesterRequested()&&trade.getHasOwnerRequested()){
            trade.updateTradeStatus(TradeStatus.IN_EXCHANGE);
        }

        return new TradeRequestResponseDto(tradeId);
    }

    public void cancelTradeRequest(Long tradeId, Long userId) {
        // 로그인 유저 확인
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Trade 엔티티 확인
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        // 해당 Trade에 연결된 PointHistory 확인
        PointHistory pointHistory = pointHistoryRepository.findByTradeIdOrderByCreatedAtDesc(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_HISTORY_NOT_FOUND));

        // 차감된 금액 복원
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));
        point.add(pointHistory.getAmount() * -1); // 차감된 금액을 복원
        pointRepository.save(point);

        // 요청 상태 업데이트
        if (trade.getRequester().getId().equals(currentUser.getId())) {
            trade.updateHasRequesterRequested(false);
        } else {
            trade.updateHasOwnerRequested(false);
        }

        // 새로운 포인트 이력 생성 (복원 내역)
        PointHistory restorePointHistory = PointHistory.create(
                pointHistory.getAmount() * -1, // 복원 금액은 기존의 음수 금액을 양수로 변경
                PointTransactionType.RETURN,
                point,
                trade
        );
        pointHistoryRepository.save(restorePointHistory);

        // 거래 상태 확인 및 업데이트 (양쪽 중 하나가 false라면 초기 상태로 되돌리기)
        if (!trade.getHasRequesterRequested() || !trade.getHasOwnerRequested()) {
            trade.updateTradeStatus(TradeStatus.AVAILABLE);
        }
    }
}
