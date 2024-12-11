package com.my.relink.controller.item.donation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PagingInfo {
    private long totalDataCount; //전체 데이터 수
    private int totalPages; //전체 페이지 수
    private boolean hasPrev; //이전 페이지 존재여부
    private boolean hasNext; //다음 페이지 존재여부
}