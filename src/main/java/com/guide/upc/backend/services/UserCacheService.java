package com.guide.upc.backend.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.guide.upc.backend.dtos.UserDto;

@Service
public class UserCacheService {
    private final Map<String, UserDto> userCache = new ConcurrentHashMap<>();

    public void cacheUser(String login, UserDto userDto) {
        userCache.put(login, userDto);
    }

    public UserDto getCachedUser(String login) {
        return userCache.get(login);
    }

    public void clearCache(String login) {
        userCache.remove(login);
    }
}