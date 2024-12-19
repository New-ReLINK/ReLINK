package com.my.relink.controller.report.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public class UploadImagesForReportReqDto {
    @NotNull(message = "이미지를 첨부해야 합니다")
    private MultipartFile image1;
    private MultipartFile image2;
    private MultipartFile image3;
    private MultipartFile image4;
    private MultipartFile image5;
    private MultipartFile image6;
    private MultipartFile image7;

    public List<MultipartFile> getNonNullImages() {
        return Stream.of(
                        image1,
                        image2,
                        image3,
                        image4,
                        image5,
                        image6,
                        image7)
                .filter(Objects::nonNull)
                .toList();
    }
}
