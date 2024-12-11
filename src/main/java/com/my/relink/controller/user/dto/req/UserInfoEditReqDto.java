package com.my.relink.controller.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoEditReqDto {

    @NotBlank(message = "닉네임을 적어주세요.")
    @Length(max = 20, message = "닉네임은 최대 20자까지 입니다.")
    private String nickname;

    @NotBlank(message = "이름을 적어주세요.")
    @Length(max = 20, message = "이름은 최대 20자까지 입니다.")
    private String name;
}
