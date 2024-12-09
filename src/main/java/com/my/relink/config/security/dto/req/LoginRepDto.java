package com.my.relink.config.security.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class LoginRepDto {

    @NotBlank(message = "이메일을 적어주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "패스워드를 적어주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$",
            message = "비밀번호는 영문 대소문자와 숫자를 포함해서 8~20자리여야 합니다"
    )
    private String password;
}
