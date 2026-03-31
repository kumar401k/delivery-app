package com.tw.joi.delivery.dto.response;

import com.tw.joi.delivery.domain.GroceryProduct;
import com.tw.joi.delivery.domain.ProductOffer;
import lombok.NonNull;

import java.math.BigDecimal;

public record ProductInventoryInfo(
        String productId,
        String productName,
        BigDecimal mrp,
        BigDecimal sellingPrice,
        BigDecimal discount,
        int availableStock,
        int threshold,
        ProductOffer offer) {

    public static @NonNull ProductInventoryInfo from(GroceryProduct product) {
        return new ProductInventoryInfo(
            product.getProductId(),
            product.getProductName(),
            product.getMrp(),
            product.getSellingPrice(),
            product.getDiscount(),
            product.getAvailableStock(),
            product.getThreshold(),
            product.getOffer()
        );
    }
}
