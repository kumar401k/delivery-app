package com.tw.joi.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tw.joi.delivery.dto.request.NewProductRequest;
import com.tw.joi.delivery.dto.request.ProductAndStoreRequest;
import com.tw.joi.delivery.dto.response.ProductInventoryInfo;
import com.tw.joi.delivery.seedData.SeedData;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InventoryServiceTest {

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        SeedData.groceryProducts = Arrays.asList(
                SeedData.createGroceryProduct("Wheat Bread", "product101", SeedData.store101),
                SeedData.createGroceryProduct("Spinach", "product102", SeedData.store101),
                SeedData.createGroceryProduct("Crackers", "product103", SeedData.store101));
        inventoryService = new InventoryService();
    }

    @Test
    void getProductsByStore_returnsAllProductsForMatchingStore() {
        List<ProductInventoryInfo> result = inventoryService.getProductsByStore("store101");

        assertEquals(3, result.size());
    }

    @Test
    void getProductsByStore_returnsEmptyListForStoreWithNoProducts() {
        List<ProductInventoryInfo> result = inventoryService.getProductsByStore("store102");

        assertTrue(result.isEmpty());
    }

    @Test
    void getProductsByStore_returnsEmptyListForUnknownStore() {
        List<ProductInventoryInfo> result = inventoryService.getProductsByStore("storeXYZ");

        assertTrue(result.isEmpty());
    }

    @Test
    void getProductByStoreAndProductId_returnsProductWhenFound() {
        Optional<ProductInventoryInfo> result =
                inventoryService.getProductByStoreAndProductId("store101", "product101");

        assertTrue(result.isPresent());
        assertEquals("product101", result.get().productId());
        assertEquals("Wheat Bread", result.get().productName());
    }

    @Test
    void getProductByStoreAndProductId_returnsEmptyWhenProductNotInStore() {
        Optional<ProductInventoryInfo> result =
                inventoryService.getProductByStoreAndProductId("store102", "product101");

        assertTrue(result.isEmpty());
    }

    @Test
    void getProductByStoreAndProductId_returnsEmptyWhenProductDoesNotExist() {
        Optional<ProductInventoryInfo> result =
                inventoryService.getProductByStoreAndProductId("store101", "productXYZ");

        assertTrue(result.isEmpty());
    }

    @Test
    void updateProductStock_updatesStockAndReturnsUpdatedProduct() {
        ProductAndStoreRequest request = new ProductAndStoreRequest("store101", 100);

        Optional<ProductInventoryInfo> result =
                inventoryService.updateProductStock("product101", request);

        assertTrue(result.isPresent());
        assertEquals(100, result.get().availableStock());
        assertEquals("product101", result.get().productId());
    }

    @Test
    void updateProductStock_returnsEmptyWhenProductDoesNotExist() {
        ProductAndStoreRequest request = new ProductAndStoreRequest("store101", 100);

        Optional<ProductInventoryInfo> result =
                inventoryService.updateProductStock("productXYZ", request);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateProductStock_returnsEmptyWhenStoreDoesNotMatch() {
        ProductAndStoreRequest request = new ProductAndStoreRequest("store102", 100);

        Optional<ProductInventoryInfo> result =
                inventoryService.updateProductStock("product101", request);

        assertTrue(result.isEmpty());
    }

    @Test
    void getLowStockProductsInStore_returnsProductsBelowThreshold() {
        // seed products have availableStock=30 < threshold=40
        List<ProductInventoryInfo> result =
                inventoryService.getLowStockProductsInStore("store101");

        assertEquals(3, result.size());
    }

    @Test
    void getLowStockProductsInStore_returnsEmptyListWhenNoProductsInStore() {
        List<ProductInventoryInfo> result =
                inventoryService.getLowStockProductsInStore("store102");

        assertTrue(result.isEmpty());
    }

    @Test
    void getLowStockProductsInStore_returnsEmptyListWhenStockIsAboveThreshold() {
        inventoryService.updateProductStock("product101", new ProductAndStoreRequest("store101", 50));
        inventoryService.updateProductStock("product102", new ProductAndStoreRequest("store101", 50));
        inventoryService.updateProductStock("product103", new ProductAndStoreRequest("store101", 50));

        List<ProductInventoryInfo> result =
                inventoryService.getLowStockProductsInStore("store101");

        assertTrue(result.isEmpty());
    }

    @Test
    void addProductToStore_addsProductToExistingStoreAndReturnsUpdatedList() {
        NewProductRequest request = new NewProductRequest("product104", "Tomatoes", "store101", "Fresh Picks");

        List<ProductInventoryInfo> result = inventoryService.addProductToStore(request);

        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(p -> p.productId().equals("product104")));
        assertTrue(result.stream().anyMatch(p -> p.productName().equals("Tomatoes")));
    }

    @Test
    void addProductToStore_returnsListWithSingleProductForNewStore() {
        NewProductRequest request = new NewProductRequest("product201", "Oranges", "store999", "New Store");

        List<ProductInventoryInfo> result = inventoryService.addProductToStore(request);

        assertEquals(1, result.size());
        assertEquals("product201", result.get(0).productId());
    }

    @Test
    void addProductToStore_doesNotAffectOtherStores() {
        NewProductRequest request = new NewProductRequest("product104", "Tomatoes", "store101", "Fresh Picks");
        inventoryService.addProductToStore(request);

        List<ProductInventoryInfo> store102Products = inventoryService.getProductsByStore("store102");

        assertTrue(store102Products.isEmpty());
    }

    @Test
    void addProductToStore_existingStoreProductCountIncreasesByOne() {
        int before = inventoryService.getProductsByStore("store101").size();
        NewProductRequest request = new NewProductRequest("product105", "Milk", "store101", "Fresh Picks");

        inventoryService.addProductToStore(request);

        int after = inventoryService.getProductsByStore("store101").size();
        assertEquals(before + 1, after);
    }
}
