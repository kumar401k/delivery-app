package com.tw.joi.delivery.controller;

import com.tw.joi.delivery.dto.request.AddOfferRequest;
import com.tw.joi.delivery.dto.request.NewProductRequest;
import com.tw.joi.delivery.dto.request.ProductAndStoreRequest;
import com.tw.joi.delivery.dto.response.ProductInventoryInfo;
import com.tw.joi.delivery.service.InventoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductInventoryInfo>> getProductsByStore(
        @RequestParam(name = "storeId") String storeId) {
        return ResponseEntity.ok(inventoryService.getProductsByStore(storeId));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductInventoryInfo> getProductByStore(
        @PathVariable String productId,
        @RequestParam(name = "storeId") String storeId) {
        return inventoryService.getProductByStoreAndProductId(storeId, productId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/products/{productId}/stock")
    public ResponseEntity<ProductInventoryInfo> updateStock(
        @PathVariable String productId,
        @RequestBody ProductAndStoreRequest productStockReq) {
        return inventoryService.updateProductStock(productId, productStockReq)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<String> fetchStoreInventoryHealth(
            @RequestParam(name = "storeId") String storeId) {
        var products = inventoryService.getProductsByStore(storeId);
        if (products.isEmpty()) {
            return ResponseEntity.ok("No Inventories available");
        }
        return ResponseEntity.ok("Inventories available");
    }

    @GetMapping("/products/low-stock/{storeId}")
    public ResponseEntity<List<ProductInventoryInfo>> fetchLowStockItems(
            @PathVariable String storeId) {
        return ResponseEntity.ok(inventoryService.getLowStockProductsInStore(storeId));
    }

    @PostMapping("/products/add")
    public ResponseEntity<List<ProductInventoryInfo>> addNewProductToStore(@RequestBody NewProductRequest req) {
        return ResponseEntity.ok(inventoryService.addProductToStore(req));
    }

    @PostMapping("/products/{productId}/offer")
    public ResponseEntity<ProductInventoryInfo> addOfferToProduct(
            @PathVariable String productId,
            @RequestBody AddOfferRequest req) {
        return inventoryService.addOfferToProduct(productId, req)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
