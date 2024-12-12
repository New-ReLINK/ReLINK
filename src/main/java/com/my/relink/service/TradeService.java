package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.trade.dto.request.AddressReqDto;
import com.my.relink.controller.trade.dto.request.TrackingNumberReqDto;
import com.my.relink.controller.trade.dto.response.*;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.ImageRepository;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserTrustScoreService userTrustScoreService;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final PointTransactionService pointTransactionService;
    private final ExchangeItemRepository exchangeItemRepository;
    private final ImageRepository imageRepository;

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


    public Trade findByIdOrFail(Long tradeId) {
        return tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));
    }

    @Transactional

    public TradeRequestRespDto requestTrade(Long tradeId, AuthUser authUser) {

        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId).
                orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        //차감 메서드 위임
        pointTransactionService.deductPoints(tradeId, currentUser);

        //요청자/소유자 여부에 따라 적절한 요청 상태 필드 업데이트
        if (trade.isRequester(currentUser.getId())) {
            trade.updateHasRequesterRequested(true);
        } else {
            trade.updateHasOwnerRequested(true);
        }
        // 양쪽 모두 requested가 true라면 거래 상태를 in_exchange로 변경
        if (trade.getHasRequesterRequested() && trade.getHasOwnerRequested()) {
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
        pointTransactionService.restorePoints(tradeId, currentUser);

        // 요청 상태 업데이트
        if (trade.isRequester(currentUser.getId())) {
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

        if (trade.getHasOwnerRequested() && trade.getHasRequesterRequested()) {
            // 소유자 주소와 요청자 주소 업데이트
            if (trade.isRequester(currentUser.getId())) {
                Address requesterAddress = reqDto.toRequesterAddressEntity();  // 요청자 주소 생성
                trade.saveRequesterAddress(requesterAddress);
            } else {
                Address ownerAddress = reqDto.toOwnerAddressEntity();  // 소유자 주소 생성
                trade.saveOwnerAddress(ownerAddress);
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
        if (trade.isRequester(currentUser.getId())) {
            trade.updateHasRequesterReceived(true);
        } else {
            trade.updateHasOwnerReceived(true);
        }

        //양쪽 모두 수령 확인 시 거래 상태 변경
        if (trade.getHasOwnerReceived() && trade.getHasRequesterReceived()) {
            trade.updateTradeStatus(TradeStatus.EXCHANGED);
        }
        //보증금 반환
        Integer amount = trade.getOwnerExchangeItem().getDeposit();
        pointTransactionService.restorePointsForAllTraders(trade, amount);
        tradeRepository.save(trade);

        return new TradeCompleteRespDto(tradeId);
    }

    @Transactional
    public void getExchangeItemTrackingNumber(Long tradeId, TrackingNumberReqDto reqDto, AuthUser authUser) {
        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        if (trade.isRequester(currentUser.getId())) {
            trade.updateRequesterTrackingNumber(reqDto.getTrackingNumber());
        } else {
            trade.updateOwnerTrackingNumber(reqDto.getTrackingNumber());
        }

        if (!trade.getOwnerTrackingNumber().isEmpty() && !trade.getRequesterTrackingNumber().isEmpty()) {
            trade.updateTradeStatus(TradeStatus.IN_DELIVERY);
        }
        tradeRepository.save(trade);
    }

    public TradeCompletionRespDto findCompleteTradeInfo(Long tradeId, AuthUser authUser) {
        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        ExchangeItem myExchangeItem;
        ExchangeItem partnerExchangeItem;

        if (trade.isRequester(currentUser.getId())) {
            myExchangeItem = trade.getRequesterExchangeItem();
            partnerExchangeItem = trade.getOwnerExchangeItem();

        } else {
            myExchangeItem = trade.getOwnerExchangeItem();
            partnerExchangeItem = trade.getRequesterExchangeItem();
        }

        Image myImage = imageRepository.findByEntityIdAndEntityType(myExchangeItem.getId(), EntityType.EXCHANGE_ITEM).orElse(null);
        Image partnerImage = imageRepository.findByEntityIdAndEntityType(partnerExchangeItem.getId(), EntityType.EXCHANGE_ITEM).orElse(null);

        User partnerUser = userRepository.findById(trade.getPartner(currentUser.getId()).getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String completedAt = (trade.getModifiedAt() != null)
                ? trade.getModifiedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH:mm"))
                : "N/A";

        return TradeCompletionRespDto.builder()
                .myItem(TradeCompletionRespDto.TradeItemInfo.builder()
                        .itemName(myExchangeItem.getName())
                        .itemQuality(myExchangeItem.getItemQuality())
                        .itemId(myExchangeItem.getId())
                        .itemImageUrl(myImage != null ? myImage.getImageUrl() : null)
                        .build())

                .partnerItem(TradeCompletionRespDto.TradeItemInfo.builder()
                        .itemName(partnerExchangeItem.getName())
                        .itemQuality(partnerExchangeItem.getItemQuality())
                        .itemId(partnerExchangeItem.getId())
                        .itemImageUrl(partnerImage != null ? partnerImage.getImageUrl() : null)
                        .build())

                .partnerInfo(TradeCompletionRespDto.UserInfo.builder()
                        .partnerAddress(trade.isRequester(partnerUser.getId()) ? trade.getOwnerAddress() : trade.getRequesterAddress())
                        .build())

                .tradeStatusInfo(TradeCompletionRespDto.TradeStatusInfo.builder()
                        .completedAt(completedAt)
                        .tradeStatus(trade.getTradeStatus())
                        .build())
                .build();
    }
}

