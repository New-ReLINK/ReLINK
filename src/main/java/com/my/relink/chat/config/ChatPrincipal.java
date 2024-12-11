package com.my.relink.chat.config;

import com.my.relink.config.security.AuthUser;
import lombok.Getter;
import java.security.Principal;

@Getter
public class ChatPrincipal implements Principal {

    private final AuthUser authUser;

    public ChatPrincipal(AuthUser authUser) {
        this.authUser = authUser;
    }

    @Override
    public String getName() {
        return authUser.getId().toString();
    }
}
