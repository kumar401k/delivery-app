package com.tw.joi.delivery.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewProductRequest {
    private String productId;
    private String productName;
    private String storeId;
    private String storeName;
    /*private String offer;
    private Date startDate;
    private Date endDate;*/
    private AddOfferRequest offer;

}
