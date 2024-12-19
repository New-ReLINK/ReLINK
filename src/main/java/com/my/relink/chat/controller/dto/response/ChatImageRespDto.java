package com.my.relink.chat.controller.dto.response;

import com.my.relink.domain.image.Image;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatImageRespDto {
    private String imageUrl;

    public ChatImageRespDto(Image image) {
        this.imageUrl = image.getImageUrl();
    }
}
