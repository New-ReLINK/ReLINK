package com.my.relink.chat.controller.dto.request;

import com.my.relink.common.validation.ValidFile;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class ChatImageReqDto {

    @ValidFile(message = "이미지를 첨부해야 합니다")
    private MultipartFile image;

}
