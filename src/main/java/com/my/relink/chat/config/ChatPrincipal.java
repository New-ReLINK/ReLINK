package com.my.relink.chat.config;

import com.my.relink.config.security.AuthUser;
import lombok.Getter;
import java.security.Principal;

@Getter
public class ChatPrincipal implements Principal {

    private final Long userId;

    public ChatPrincipal(AuthUser authUser) {
        this.userId = authUser.getId();
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}
