package com.my.relink.service;


import com.my.relink.controller.report.dto.request.TradeReportCreateReqDto;
import com.my.relink.domain.report.ReportType;
import com.my.relink.domain.report.repository.ReportRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final TradeService tradeService;
    private final ReportRepository reportRepository;

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
}
