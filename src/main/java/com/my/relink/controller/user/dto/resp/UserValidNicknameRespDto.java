package com.my.relink.controller.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserValidNicknameRespDto {
    private boolean duplicated;
}
