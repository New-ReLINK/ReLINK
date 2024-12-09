package com.my.relink.controller.user.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class UserValidEmailReqDto {

    @NotBlank(message = "이메일을 적어주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Length(max = 30)
    private String email;
}
