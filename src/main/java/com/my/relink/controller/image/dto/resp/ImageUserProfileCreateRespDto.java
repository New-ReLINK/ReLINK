package com.my.relink.controller.image.dto.resp;

import com.my.relink.domain.image.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageUserProfileCreateRespDto {
    private Long id;
    private String imageUrl;

    public ImageUserProfileCreateRespDto(Image image) {
        this.id = image.getId();
        this.imageUrl = image.getImageUrl();
    }
}
