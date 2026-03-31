package com.tw.joi.delivery.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOffer {
    private String offerType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
