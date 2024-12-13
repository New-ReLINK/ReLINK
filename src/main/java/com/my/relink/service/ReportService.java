package com.my.relink.service;


import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.report.dto.request.ExchangeItemReportCreateReqDto;
import com.my.relink.controller.report.dto.request.TradeReportCreateReqDto;
import com.my.relink.controller.report.dto.response.TradeInfoRespDto;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.report.Report;
import com.my.relink.domain.report.ReportType;
import com.my.relink.domain.report.repository.ReportRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final TradeService tradeService;
    private final ReportRepository reportRepository;
    private final ExchangeItemService exchangeItemService;
    private final DateTimeUtil dateTimeUtil;
    private final ImageService imageService;

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

    public TradeInfoRespDto getTradeInfo(Long tradeId, Long userId) {
        Trade trade = tradeService.findByIdWithItemsAndUsersOrFail(tradeId);
        ExchangeItem exchangeItem = trade.isRequester(userId) ?
                trade.getRequesterExchangeItem() :
                trade.getOwnerExchangeItem();
        String exchangeItemUrl = imageService.getExchangeItemUrl(exchangeItem);
        return new TradeInfoRespDto(trade, exchangeItem, exchangeItemUrl, dateTimeUtil);
    }
}
