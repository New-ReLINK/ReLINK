package com.my.relink.chat.aop;

import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class TradeCacheEvictionAspect {

    private final CacheManager cacheManager;
    private final TradeService tradeService;


    @AfterReturning(
            value = "execution(* com.my.relink.service.ExchangeItemService.updateExchangeItem(..)) && args(itemId)",
            argNames = "itemId"
    )
    public void evictTradeInfoCache(Long itemId) {
        Long tradeId = tradeService.findByIdWithOwnerItemOrFail(itemId).getId();
        if (tradeId == null) return;
        Cache cache = cacheManager.getCache("tradeInfo");
        if(cache != null) {
            cache.evict(tradeId);
            log.debug("TradeInfo 캐시 무효화 - tradeId: {}", tradeId);
        }
    }

}
