package com.my.relink.dto.user.req;

import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class UserCreateReqDto {

    @NotBlank(message = "이름을 적어주세요.")
    @Length(max = 20, message = "이름은 최대 20자까지 입니다.")
    private String name;

    @NotBlank(message = "닉네임을 적어주세요.")
    @Length(max = 20, message = "닉네임은 최대 20자까지 입니다.")
    private String nickname;

    @NotBlank(message = "이메일을 적어주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Length(max = 30)
    private String email;

    @NotBlank(message = "패스워드를 적어주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$",
            message = "비밀번호는 영문 대소문자와 숫자를 포함해서 8~20자리여야 합니다"
    )
    private String password;

    @Pattern(
            regexp = "^010-[0-9]{4}-[0-9]{4}$",
            message = "올바른 전화번호 형식이 아닙니다."
    )
    private String contact;

    @Valid
    private AddressCreateReqDto address;

    @Builder
    public UserCreateReqDto(String name, String nickname, String email, String password, String contact, AddressCreateReqDto address) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.contact = contact;
        this.address = address;
    }

    public User toEntity(UserCreateReqDto dto) {
        return User.builder()
                .name(dto.getName())
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .contact(dto.getContact())
                .address(dto.address.toEntity(dto.getAddress()))
                .role(Role.USER)
                .build();
    }

    public void changePassword(String password) {
        this.password = password;
    }
}
