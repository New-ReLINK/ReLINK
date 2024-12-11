package com.my.relink.controller.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserValidEmailRespDto {
    private boolean duplicated;
}
