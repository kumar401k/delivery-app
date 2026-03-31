package com.tw.joi.delivery.service;

import com.tw.joi.delivery.domain.GroceryProduct;
import com.tw.joi.delivery.domain.GroceryStore;
import com.tw.joi.delivery.domain.ProductOffer;
import com.tw.joi.delivery.dto.request.AddOfferRequest;
import com.tw.joi.delivery.dto.request.NewProductRequest;
import com.tw.joi.delivery.dto.request.ProductAndStoreRequest;
import com.tw.joi.delivery.dto.response.ProductInventoryInfo;
import com.tw.joi.delivery.seedData.SeedData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final List<GroceryProduct> products = new ArrayList<>(SeedData.groceryProducts);

    public List<ProductInventoryInfo> getProductsByStore(String storeId) {
        return products.stream()
            .filter(product -> product.getStore().getOutletId().equals(storeId))
            .map(ProductInventoryInfo::from)
            .toList();
    }

    public Optional<ProductInventoryInfo> getProductByStoreAndProductId(String storeId, String productId) {
        return products.stream()
            .filter(product -> product.getStore().getOutletId().equals(storeId)
                && product.getProductId().equals(productId))
            .findFirst()
            .map(ProductInventoryInfo::from);
    }

    public Optional<ProductInventoryInfo> updateProductStock(String productId,
                                                             ProductAndStoreRequest productStockReq) {
        return products.stream()
            .filter(product -> product.getStore().getOutletId().equals(productStockReq.getStoreId())
                && product.getProductId().equals(productId))
            .findFirst()
            .map(product -> {
                product.setAvailableStock(productStockReq.getQuantity());
                return ProductInventoryInfo.from(product);
            });
    }

    public List<ProductInventoryInfo> getLowStockProductsInStore(String storeId) {
        return products.stream()
                .filter(product -> product.getStore().getOutletId().equals(storeId)
                    && product.getAvailableStock() < product.getThreshold())
                .map(ProductInventoryInfo :: from)
                .toList();
    }

    public Optional<ProductInventoryInfo> addOfferToProduct(String productId, AddOfferRequest request) {
        return products.stream()
                .filter(product -> product.getStore().getOutletId().equals(request.storeId())
                        && product.getProductId().equals(productId))
                .findFirst()
                .map(product -> {
                    ProductOffer offer = ProductOffer.builder()
                            .offerType(request.offerType())
                            .startDate(request.startDate())
                            .endDate(request.endDate())
                            .build();
                    product.setOffer(offer);
                    return ProductInventoryInfo.from(product);
                });
    }

    public List<ProductInventoryInfo> addProductToStore(NewProductRequest request) {
        GroceryStore store = products.stream()
                .filter(p -> p.getStore().getOutletId().equals(request.getStoreId()))
                .map(GroceryProduct::getStore)
                .findFirst()
                .orElseGet(() -> SeedData.createStore(request.getStoreName(), request.getStoreId()));

        GroceryProduct newProduct = SeedData.createGroceryProduct(
                request.getProductName(), request.getProductId(), store);

        ProductOffer offer = ProductOffer.builder()
                .offerType(request.getOffer().offerType())
                .startDate(request.getOffer().startDate())
                .endDate(request.getOffer().endDate())
                .build();

        newProduct.setOffer(offer);
        products.add(newProduct);

        return getProductsByStore(request.getStoreId());
    }
}
