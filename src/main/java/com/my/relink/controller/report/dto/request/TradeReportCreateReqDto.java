package com.my.relink.controller.report.dto.request;

import com.my.relink.common.validation.EnumValidator;
import com.my.relink.domain.report.Report;
import com.my.relink.domain.report.ReportReason;
import com.my.relink.domain.report.ReportType;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class TradeReportCreateReqDto {

    @EnumValidator(enumClass = ReportReason.class)
    @NotBlank(message = "신고 사유는 필수입니다")
    private ReportReason reportReason;

    private String description;

    public Report toEntity(Trade trade, User targetUser){
        return Report.builder()
                .reportType(ReportType.TRADE)
                .entityId(trade.getId())
                .targetUserId(targetUser.getId())
                .reportReason(reportReason)
                .description(description)
                .build();
    }


}
