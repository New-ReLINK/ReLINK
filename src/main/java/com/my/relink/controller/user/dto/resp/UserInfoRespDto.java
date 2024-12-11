package com.my.relink.controller.user.dto.resp;

import com.my.relink.domain.image.Image;
import com.my.relink.domain.user.User;
import lombok.Getter;

@Getter
public class UserInfoRespDto {

    private String name;
    private String nickname;
    private String imageUrl;

    public UserInfoRespDto(User user, Image image) {
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.imageUrl = image != null ? image.getImageUrl() : null;
    }
}
