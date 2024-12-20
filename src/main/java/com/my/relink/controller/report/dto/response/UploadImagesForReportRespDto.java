package com.my.relink.controller.report.dto.response;

import com.my.relink.domain.report.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UploadImagesForReportRespDto {

    private Long reportId;

    public UploadImagesForReportRespDto(Report report) {
        this.reportId = report.getId();
    }
}
