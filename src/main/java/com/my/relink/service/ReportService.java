package com.my.relink.service;


import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final TradeService tradeService;
    private final UserService userService;

    @Transactional
    public void createTradeReport(Long tradeId, Long userId) {
        Trade trade = tradeService.findByIdOrFail(tradeId);
        User user = userService.findByIdOrFail(userId);

        return null;
    }
}
