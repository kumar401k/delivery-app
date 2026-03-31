package com.tw.joi.delivery.dto.request;

import java.time.LocalDateTime;

public record AddOfferRequest(
        String storeId,
        String offerType,
        LocalDateTime startDate,
        LocalDateTime endDate) {
}
