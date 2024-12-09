package com.my.relink.service;

import com.my.relink.controller.trade.dto.response.MessageRespDto;
import com.my.relink.controller.trade.dto.response.TradeInquiryDetailRespDto;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.message.MessageRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.PointTransactionType;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.dto.TradeRequestResponseDto;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;


    /**
     * [문의하기] -> 해당 채팅방의 거래 정보, 상품 정보, 상대 유저 정보 내리기
     *
     * @param tradeId
     * @param user
     * @return
     */
    public TradeInquiryDetailRespDto getTradeInquiryDetail(Long tradeId, User user) {
        Trade trade = tradeRepository.findByIdWithItemsAndUser(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        trade.validateAccess(user);

        String requestedItemImageUrl = imageService.getExchangeItemUrl(trade.getRequesterExchangeItem());
        User partner = trade.getPartner(user);
        int trustScoreOfPartner = userService.getTrustScore(partner);

        return new TradeInquiryDetailRespDto(trade, partner, trustScoreOfPartner, requestedItemImageUrl);
    }

    /**
     * 채팅방 이전 대화 내역 조회하기
     * 커서 기반 페이징 진행
     * @param tradeId
     * @param size
     * @param cursor
     * @return
     */
    public MessageRespDto getChatRoomMessage(Long tradeId, int size, Long cursor) {
        // cursor에 값이 없을 시 가장 최근 메시지부터 조회 위해 값 설정
        cursor = (cursor == null || cursor == 0) ? Long.MAX_VALUE : cursor;

        List<Message> messageList = messageRepository.findMessagesBeforeCursor(tradeId, cursor, PageRequest.of(0, size + 1));
        List<Message> pageMessageList = messageList.size() > size
                ? messageList.subList(0, size)
                : messageList;
        Long nextCursor = !pageMessageList.isEmpty()
                ? pageMessageList.get(pageMessageList.size() - 1).getId()
                : null;

        return new MessageRespDto(pageMessageList, nextCursor);
    }

    public Trade findByIdOrFail(Long tradeId) {
        return tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));
    }

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
}

