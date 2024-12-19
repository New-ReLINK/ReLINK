package com.my.relink.chat.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@Getter
@Setter
public class ChatImageReqDto {

    @NotNull(message = "이미지를 첨부해야 합니다")
    private MultipartFile image;

}
