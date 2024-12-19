package com.my.relink.service;


import com.my.relink.config.s3.S3Service;
import com.my.relink.controller.report.dto.request.ExchangeItemReportCreateReqDto;
import com.my.relink.controller.report.dto.request.TradeReportCreateReqDto;
import com.my.relink.controller.report.dto.request.UploadImagesForReportReqDto;
import com.my.relink.controller.report.dto.response.ExchangeItemInfoRespDto;
import com.my.relink.controller.report.dto.response.TradeInfoRespDto;
import com.my.relink.controller.report.dto.response.UploadImagesForReportRespDto;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.report.Report;
import com.my.relink.domain.report.ReportType;
import com.my.relink.domain.report.repository.ReportRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
    private final S3Service s3Service;

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
        ExchangeItem exchangeItem = exchangeItemService.findByIdFetchUser(itemId);
        String exchangeItemUrl = imageService.getExchangeItemThumbnailUrl(exchangeItem);
        return new ExchangeItemInfoRespDto(exchangeItem, exchangeItemUrl);
    }

    @Transactional
    public UploadImagesForReportRespDto uploadImagesForTradeReport(Long tradeId, UploadImagesForReportReqDto uploadImagesForReportReqDto) {
        Report report = reportRepository.findByEntityIdAndReportType(tradeId, ReportType.TRADE)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOR_FOUND));
        List<Image> imageList = uploadImages(uploadImagesForReportReqDto, report);
        try {
            imageService.saveImages(imageList);
        }catch (Exception e){
            handleImageUploadFail(imageList);
            //에러 던지기
        }
        return new UploadImagesForReportRespDto(report);
    }

    private void handleImageUploadFail(List<Image> imageList){
        for (Image image : imageList) {
            try {
                s3Service.deleteImage(image.getImageUrl());
            } catch (Exception e){
                log.error("[이미지 삭제 실패] imageUrl = {}, cause = {}", image.getImageUrl(), e.getMessage(), e);
            }
        }

    }

    private List<Image> uploadImages(UploadImagesForReportReqDto uploadImagesForReportReqDto, Report report){
        List<Image> uploadedImages = new ArrayList<>();

        Stream.of(
                    uploadImagesForReportReqDto.getImage1(),
                    uploadImagesForReportReqDto.getImage2(),
                    uploadImagesForReportReqDto.getImage3(),
                    uploadImagesForReportReqDto.getImage4(),
                    uploadImagesForReportReqDto.getImage5(),
                    uploadImagesForReportReqDto.getImage6(),
                    uploadImagesForReportReqDto.getImage7())
            .filter(Objects::nonNull)
            .forEach(file -> {
                try {
                    String imageUrl = s3Service.upload(file);
                    uploadedImages.add(Image.builder()
                            .entityId(report.getId())
                            .entityType(EntityType.REPORT)
                            .imageUrl(imageUrl)
                            .build());
                } catch (Exception e) {
                    handleImageUploadFail(uploadedImages);
                    throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
                }
            });

        return uploadedImages;
    }
}
