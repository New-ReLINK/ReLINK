package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.trade.dto.request.AddressReqDto;
import com.my.relink.controller.trade.dto.response.AddressRespDto;
import com.my.relink.controller.trade.dto.response.TradeCompleteRespDto;
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
import com.my.relink.domain.user.Address;
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
    private final PointTransactionService pointTransactionService;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointRepository pointRepository;

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
    public TradeRequestRespDto requestTrade(Long tradeId, AuthUser authUser) {

        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId).
                orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        //차감 메서드 위임
        pointTransactionService.deductPoints(tradeId, authUser);

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
        pointTransactionService.restorePoints(tradeId, authUser);

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

    @Transactional
    public AddressRespDto createAddress(Long tradeId, AddressReqDto reqDto, AuthUser authUser) {
        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        if(trade.getHasOwnerRequested()&&trade.getHasRequesterRequested()){
            // 소유자 주소와 요청자 주소 업데이트
            if(trade.getRequester().getId().equals(currentUser.getId())){
                Address requesterAddress = reqDto.toRequesterAddressEntity();  // 요청자 주소 생성
                trade.saveRequesterAddress(requesterAddress);
                //trade.saveOwnerAddress(reqDto.toOwnerAddressEntity());
            } else{
                Address requesterAddress = reqDto.toRequesterAddressEntity();  // 요청자 주소 생성
                trade.saveRequesterAddress(requesterAddress);
                //trade.saveRequesterAddress(reqDto.toRequesterAddressEntity());
            }

            tradeRepository.save(trade);
            return new AddressRespDto(tradeId);
        } else {
            throw new BusinessException(ErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    @Transactional
    public TradeCompleteRespDto completeTrade(Long tradeId, AuthUser authUser) {
        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        //요청자/소유자 여부에 따라 수령상태 변경
        if(trade.getRequester().getId().equals(currentUser.getId())){
            trade.updateHasRequesterReceived(true);
        } else{
            trade.updateHasOwnerReceived(true);
        }

        //양쪽 모두 수령 확인 시 거래 상태 변경
        if(trade.getHasOwnerReceived()&&trade.getHasRequesterReceived()){
            trade.updateTradeStatus(TradeStatus.EXCHANGED);
        }
        //보증금 반환
        //해당 tradeId를 갖는 포인트 히스토리가 있는지 찾음
        PointHistory pointHistory = pointHistoryRepository.findFirstByTradeIdOrderByCreatedAtDesc(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode. POINT_HISTORY_NOT_FOUND));

        //amount가 해당거래 아이팀의 보증금과 일지하는지 확인
        Integer amount = trade.getOwnerExchangeItem().getDeposit();

        //amount와 type확인 후 두 유저에게 반환
        boolean isRequesterRestored = pointHistoryRepository.existsByTradeIdAndPointUserIdAndPointTransactionType(
                tradeId, trade.getOwner().getId(), PointTransactionType.RETURN
        );
        boolean isOwnerRestored = pointHistoryRepository.existsByTradeIdAndPointUserIdAndPointTransactionType(
                tradeId, trade.getOwner().getId(), PointTransactionType.RETURN
        );

        if(!isRequesterRestored){
            Point requesterPoint = pointRepository.findByUserId(trade.getRequester().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));

            requesterPoint.restore(amount);
            pointRepository.save(requesterPoint);

            PointHistory requesterRestoreHistory = PointHistory.create(
                    amount,
                    PointTransactionType.RETURN,
                    requesterPoint,
                    trade
            );
            pointHistoryRepository.save(requesterRestoreHistory);
        }

        if(!isOwnerRestored){
            Point ownerPoint = pointRepository.findByUserId(trade.getOwner().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));

            ownerPoint.restore(amount);
            pointRepository.save(ownerPoint);

            PointHistory ownerRestoreHistory = PointHistory.create(
                    amount,
                    PointTransactionType.RETURN,
                    ownerPoint,
                    trade
            );
            pointHistoryRepository.save(ownerRestoreHistory);
        }
        return new TradeCompleteRespDto(tradeId);
    }
}

