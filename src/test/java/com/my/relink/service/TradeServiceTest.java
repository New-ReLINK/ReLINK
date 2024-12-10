package com.my.relink.service;

import com.my.relink.controller.trade.dto.response.TradeInquiryDetailRespDto;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.DummyObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest extends DummyObject {
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private UserTrustScoreService userTrustScoreService;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private TradeService tradeService;

    @Nested
    @DisplayName("거래 상세 조회 테스트")
    class RetrieveTradeDetail {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {
            @Test
            @DisplayName("거래 상세 정보를 정상적으로 조회한다")
            void retrieveTradeDetail_success() {
                User user = mockRequesterUser();
                User partner = mockOwnerUser();

                Trade trade = mockTrade(partner, user);
                String imageUrl = "image";
                int trustScore = 80;

                when(tradeRepository.findByIdWithItemsAndUser(trade.getId())).thenReturn(Optional.of(trade));
                when(imageService.getExchangeItemUrl(trade.getRequesterExchangeItem())).thenReturn(imageUrl);
                when(userTrustScoreService.getTrustScore(partner)).thenReturn(trustScore);

                TradeInquiryDetailRespDto result = tradeService.getTradeInquiryDetail(trade.getId(), user.getId());

                assertAll(
                        () -> assertNotNull(result),
                        () -> verify(imageService).getExchangeItemUrl(trade.getRequesterExchangeItem()),
                        () -> verify(userTrustScoreService).getTrustScore(partner),
                        () -> assertEquals(imageUrl, result.getExchangeItemInfoDto().getRequestedItem().getImgUrl()),
                        () -> assertEquals(trustScore, result.getTradePartnerInfoDto().getTrustScore())
                );
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {
            @Test
            @DisplayName("존재하지 않는 거래 id로 조회 시 예외가 발생한다")
            void retrieveTradeDetail_WhenTradeNotFound_ThrowsException() {
                Long tradeId = 1L;
                User user = mockRequesterUser();

                when(tradeRepository.findByIdWithItemsAndUser(tradeId))
                        .thenReturn(Optional.empty());

                BusinessException exception = assertThrows(BusinessException.class,
                        () -> tradeService.getTradeInquiryDetail(tradeId, user.getId()));
                assertEquals(exception.getErrorCode(), ErrorCode.TRADE_NOT_FOUND);
            }

            @Test
            @DisplayName("권한이 없는 사용자가 조회 시 예외가 발생한다")
            void retrieveTradeDetail_WhenAccessDenied_ThrowsException() {
                User user = mockRequesterUser();
                Trade trade = mockTrade(mockOwnerUser(), mockOwnerUser());

                when(tradeRepository.findByIdWithItemsAndUser(trade.getId())).thenReturn(Optional.of(trade));

                BusinessException exception = assertThrows(BusinessException.class,
                        () -> tradeService.getTradeInquiryDetail(trade.getId(), user.getId()));
                assertEquals(exception.getErrorCode(), ErrorCode.TRADE_ACCESS_DENIED);
            }
        }
    }
}