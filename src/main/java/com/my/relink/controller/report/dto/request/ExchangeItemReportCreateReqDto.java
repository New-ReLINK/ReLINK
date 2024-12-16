package com.my.relink.controller.report.dto.request;

import com.my.relink.common.validation.EnumValidator;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.report.Report;
import com.my.relink.domain.report.ReportReason;
import com.my.relink.domain.report.ReportType;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ExchangeItemReportCreateReqDto {
    @EnumValidator(
            enumClass = ReportReason.class,
            message = "유효하지 않은 신고 사유입니다"
    )
    @NotNull(message = "신고 사유는 필수입니다")
    private String reportReason;

    private String description;

    public Report toEntity(ExchangeItem item){
        return Report.builder()
                .reportType(ReportType.ITEM)
                .entityId(item.getId())
                .targetUserId(item.getUser().getId())
                .reportReason(ReportReason.from(reportReason))
                .description(description)
                .build();
    }

}
