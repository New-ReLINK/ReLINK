package com.my.relink.service;


import com.my.relink.controller.report.dto.request.ExchangeItemReportCreateReqDto;
import com.my.relink.controller.report.dto.request.TradeReportCreateReqDto;
import com.my.relink.controller.report.dto.response.ExchangeItemInfoRespDto;
import com.my.relink.controller.report.dto.response.TradeInfoRespDto;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.report.ReportType;
import com.my.relink.domain.report.repository.ReportRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final TradeService tradeService;
    private final ReportRepository reportRepository;
    private final ExchangeItemService exchangeItemService;
    private final DateTimeUtil dateTimeUtil;
    private final ImageService imageService;
    private final ExchangeItemRepository exchangeItemRepository;

    @Transactional
    public void createTradeReport(Long tradeId, Long userId, TradeReportCreateReqDto tradeReportCreateReqDto) {
        Trade trade = tradeService.findByIdWithUsersOrFail(tradeId);
        trade.validateAccess(userId);
        User partner = trade.getPartner(userId);
        reportRepository.findByEntityIdAndReportTypeAndTargetUserId(
                tradeId,
                ReportType.TRADE,
                partner.getId()
        ).ifPresent(report -> {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED_TRADE);
        });
        reportRepository.save(tradeReportCreateReqDto.toEntity(trade, partner));
    }

    @Transactional
    public void createExchangeItemReport(Long itemId, ExchangeItemReportCreateReqDto exchangeItemReportCreateReqDto) {
        ExchangeItem exchangeItem = exchangeItemService.findByIdOrFail(itemId);
        reportRepository.save(exchangeItemReportCreateReqDto.toEntity(exchangeItem));
    }

    /**
     * 신고 전 거래 정보 조회
     * - 거래에 참여한 상대방이 탈퇴해도 거래 자체의 신고는 가능하다
     * @param tradeId
     * @param userId
     * @return
     */
    public TradeInfoRespDto getTradeInfoForReport(Long tradeId, Long userId) {
        Trade trade = tradeService.findByIdFetchItemsAndUsersOrFail(tradeId);
        trade.validateAccess(userId);
        ExchangeItem exchangeItem = trade.getPartnerItem(userId);
        String exchangeItemUrl = imageService.getExchangeItemThumbnailUrl(exchangeItem);
        return new TradeInfoRespDto(
                exchangeItem,
                exchangeItemUrl,
                dateTimeUtil.getExchangeStartFormattedTime(trade.getCreatedAt()));
    }

    /**
     * 신고 전 교환 상품 정보 조회
     * @param itemId 교환 가능/교환 중인 상품 id
     * @return 교환 상품 및 소유자 정보
     */
    public ExchangeItemInfoRespDto getExchangeItemInfoForReport(Long itemId) {
        log.info("신고 전 교환 상품 정보 조회: itemId = {}", itemId);
        ExchangeItem exchangeItem = exchangeItemService.findByIdFetchUser(itemId);
        String exchangeItemUrl = imageService.getExchangeItemThumbnailUrl(exchangeItem);
        return new ExchangeItemInfoRespDto(exchangeItem, exchangeItemUrl);
    }
}
