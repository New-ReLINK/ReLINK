package com.my.relink.chat.service.cache.metric;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KeyMetadata{
    private Long ttl;
    private Long size;
}