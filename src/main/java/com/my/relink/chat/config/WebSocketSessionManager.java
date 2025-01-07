package com.my.relink.chat.config;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    public void addSession(String sessionId){
        activeSessions.add(sessionId);
    }

    public void removeSession(String sessionId){
        activeSessions.remove(sessionId);
    }

    public int getActiveSessionCount(){
        return activeSessions.size();
    }
}
