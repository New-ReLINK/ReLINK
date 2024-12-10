package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.trade.dto.response.TradeInquiryDetailRespDto;
import com.my.relink.controller.trade.dto.response.TradeRequestRespDto;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.PointTransactionType;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
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
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserTrustScoreService userTrustScoreService;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointHistoryService pointHistoryService;


    /**
     * [문의하기] -> 해당 채팅방의 거래 정보, 상품 정보, 상대 유저 정보 내리기
     *
     * @param tradeId
     * @param userId
     * @return
     */
    public TradeInquiryDetailRespDto getTradeInquiryDetail(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findByIdWithItemsAndUser(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        trade.validateAccess(userId);

        String requestedItemImageUrl = imageService.getExchangeItemUrl(trade.getRequesterExchangeItem());
        User partner = trade.getPartner(userId);
        int trustScoreOfPartner = userTrustScoreService.getTrustScore(partner);

        return new TradeInquiryDetailRespDto(trade, partner, trustScoreOfPartner, requestedItemImageUrl);
    }

    @Transactional
    public TradeRequestRespDto requestTrade(Long tradeId, AuthUser authUser) {//추후 로그인 유저로 바뀔 예정

        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId).
                orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        //해당 로그인 유저의 포인트를 조회
        Point point = pointRepository.findByUserId(authUser.getId())
                .orElseThrow(()-> new BusinessException(ErrorCode.POINT_NOT_FOUND));
        if(point.getAmount()<trade.getOwnerExchangeItem().getDeposit()){
            throw new BusinessException(ErrorCode.POINT_SHORTAGE);
        }

        //포인트 차감
        point.deduct(trade.getOwnerExchangeItem().getDeposit());
        pointRepository.save(point);

        //포인트 이력 생성
        PointHistory pointHistory = PointHistory.create(trade.getOwnerExchangeItem().getDeposit(), PointTransactionType.DEPOSIT, point, trade);
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
            tradeRepository.save(trade);
        }

        return new TradeRequestRespDto(tradeId);
    }

    @Transactional
    public void cancelTradeRequest(Long tradeId, AuthUser authUser) {

        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        //복원 메서드 위임
        pointHistoryService.restorePoints(tradeId, authUser);

        // 요청 상태 업데이트
        if (trade.getRequester().getId().equals(currentUser.getId())) {
            trade.updateHasRequesterRequested(false);
        } else {
            trade.updateHasOwnerRequested(false);
        }

        // 거래 상태 확인 및 업데이트 (양쪽 중 하나가 false라면 초기 상태로 되돌리기)
        if (!trade.getHasRequesterRequested() || !trade.getHasOwnerRequested()) {
            trade.updateTradeStatus(TradeStatus.AVAILABLE);
            tradeRepository.save(trade);
        }
    }
}

