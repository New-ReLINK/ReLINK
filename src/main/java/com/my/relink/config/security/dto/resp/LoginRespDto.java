package com.my.relink.config.security.dto.resp;

import com.my.relink.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRespDto {

    private Long id;
    private String nickname;
    private String name;

    public LoginRespDto(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.name = user.getName();
    }
}
