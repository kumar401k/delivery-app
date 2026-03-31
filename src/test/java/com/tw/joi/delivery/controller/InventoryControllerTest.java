package com.tw.joi.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tw.joi.delivery.domain.ProductOffer;
import com.tw.joi.delivery.domain.User;
import com.tw.joi.delivery.dto.request.AddOfferRequest;
import com.tw.joi.delivery.dto.request.NewProductRequest;
import com.tw.joi.delivery.dto.request.ProductAndStoreRequest;
import com.tw.joi.delivery.dto.response.ProductInventoryInfo;
import com.tw.joi.delivery.service.InventoryService;
import com.tw.joi.delivery.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductInventoryInfo sampleProduct() {
        return new ProductInventoryInfo("product101", "Wheat Bread",
                BigDecimal.valueOf(10.5), null, null, 30, 40, null);
    }

    private User adminUser() {
        return User.builder().userId("admin101").role("ADMIN").build();
    }

    private User regularUser() {
        return User.builder().userId("user101").role("USER").build();
    }

    @Test
    void returnGoodInventoryHealthOfTheStore() throws Exception {
        when(inventoryService.getProductsByStore("store101")).thenReturn(List.of(sampleProduct()));

        mockMvc.perform(MockMvcRequestBuilders.get("/inventory/health?storeId={storeId}", "store101")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("Inventories available"));
    }

    @Test
    void returnNoInventoriesMessage_whenStoreHasNoProducts() throws Exception {
        when(inventoryService.getProductsByStore("store102")).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/inventory/health?storeId={storeId}", "store102")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("No Inventories available"));
    }

    @Test
    void shouldReturnProductsByStore() throws Exception {
        when(inventoryService.getProductsByStore("store101")).thenReturn(List.of(sampleProduct()));

        mockMvc.perform(MockMvcRequestBuilders.get("/inventory/products?storeId={storeId}", "store101")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].productId").value("product101"))
            .andExpect(jsonPath("$[0].productName").value("Wheat Bread"));
    }

    @Test
    void shouldReturnEmptyList_whenNoProductsInStore() throws Exception {
        when(inventoryService.getProductsByStore("storeXYZ")).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/inventory/products?storeId={storeId}", "storeXYZ")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnProductByStoreAndProductId() throws Exception {
        when(inventoryService.getProductByStoreAndProductId("store101", "product101"))
                .thenReturn(Optional.of(sampleProduct()));

        mockMvc.perform(MockMvcRequestBuilders.get("/inventory/products/{productId}?storeId={storeId}",
                        "product101", "store101")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value("product101"))
            .andExpect(jsonPath("$.productName").value("Wheat Bread"))
            .andExpect(jsonPath("$.availableStock").value(30));
    }

    @Test
    void shouldReturn404_whenProductNotFoundInStore() throws Exception {
        when(inventoryService.getProductByStoreAndProductId("store101", "productXYZ"))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/inventory/products/{productId}?storeId={storeId}",
                        "productXYZ", "store101")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateProductStock() throws Exception {
        ProductAndStoreRequest request = new ProductAndStoreRequest("store101", 100);
        ProductInventoryInfo updated = new ProductInventoryInfo(
                "product101", "Wheat Bread", BigDecimal.valueOf(10.5), null, null, 100, 40, null);
        when(inventoryService.updateProductStock(eq("product101"), any(ProductAndStoreRequest.class)))
                .thenReturn(Optional.of(updated));

        mockMvc.perform(MockMvcRequestBuilders.put("/inventory/products/{productId}/stock", "product101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value("product101"))
            .andExpect(jsonPath("$.availableStock").value(100));
    }

    @Test
    void shouldReturn404_whenUpdatingStockForNonExistentProduct() throws Exception {
        ProductAndStoreRequest request = new ProductAndStoreRequest("store101", 100);
        when(inventoryService.updateProductStock(eq("productXYZ"), any(ProductAndStoreRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/inventory/products/{productId}/stock", "productXYZ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnLowStockProducts() throws Exception {
        when(inventoryService.getLowStockProductsInStore("store101")).thenReturn(List.of(sampleProduct()));

        mockMvc.perform(MockMvcRequestBuilders.get("/inventory/products/low-stock/{storeId}", "store101")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].productId").value("product101"))
            .andExpect(jsonPath("$[0].availableStock").value(30));
    }

    @Test
    void shouldReturnEmptyList_whenNoLowStockProducts() throws Exception {
        when(inventoryService.getLowStockProductsInStore("store101")).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/inventory/products/low-stock/{storeId}", "store101")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldAddProductToStore() throws Exception {
        NewProductRequest request = new NewProductRequest("product104", "Tomatoes",
                "store101", "Fresh Picks");
        List<ProductInventoryInfo> updatedList = List.of(
                sampleProduct(),
                new ProductInventoryInfo("product104", "Tomatoes", BigDecimal.valueOf(10.5), null, null, 30, 40, null));
        when(userService.fetchUserById("admin101")).thenReturn(adminUser());
        when(inventoryService.addProductToStore(any(NewProductRequest.class))).thenReturn(updatedList);

        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/products/add")
                .header("X-User-Id", "admin101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[1].productId").value("product104"))
            .andExpect(jsonPath("$[1].productName").value("Tomatoes"));
    }

    @Test
    void shouldReturnSingleProduct_whenAddingToNewStore() throws Exception {
        NewProductRequest request = new NewProductRequest("product201", "Oranges", "store999", "New Store");
        List<ProductInventoryInfo> result = List.of(
                new ProductInventoryInfo("product201", "Oranges", BigDecimal.valueOf(10.5), null, null, 30, 40, null));
        when(userService.fetchUserById("admin101")).thenReturn(adminUser());
        when(inventoryService.addProductToStore(any(NewProductRequest.class))).thenReturn(result);

        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/products/add")
                .header("X-User-Id", "admin101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].productId").value("product201"));
    }

    @Test
    void shouldReturn403_whenNonAdminTriesToAddProduct() throws Exception {
        NewProductRequest request = new NewProductRequest("product104", "Tomatoes", "store101", "Fresh Picks");
        when(userService.fetchUserById("user101")).thenReturn(regularUser());

        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/products/add")
                .header("X-User-Id", "user101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenNoUserIdHeaderProvided() throws Exception {
        NewProductRequest request = new NewProductRequest("product104", "Tomatoes", "store101", "Fresh Picks");

        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAddOfferToProduct() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59);
        AddOfferRequest request = new AddOfferRequest("store101", "DISCOUNT_10", start, end);
        ProductOffer offer = ProductOffer.builder().offerType("DISCOUNT_10").startDate(start).endDate(end).build();
        ProductInventoryInfo withOffer = new ProductInventoryInfo(
                "product101", "Wheat Bread", BigDecimal.valueOf(10.5), null, null, 30, 40, offer);
        when(userService.fetchUserById("admin101")).thenReturn(adminUser());
        when(inventoryService.addOfferToProduct(eq("product101"), any(AddOfferRequest.class))).thenReturn(Optional.of(withOffer));

        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/products/{productId}/offer", "product101")
                .header("X-User-Id", "admin101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value("product101"))
            .andExpect(jsonPath("$.offer.offerType").value("DISCOUNT_10"));
    }

    @Test
    void shouldReturn404_whenAddingOfferToNonExistentProduct() throws Exception {
        AddOfferRequest request = new AddOfferRequest("store101", "DISCOUNT_10",
                LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 4, 30, 23, 59));
        when(userService.fetchUserById("admin101")).thenReturn(adminUser());
        when(inventoryService.addOfferToProduct(eq("productXYZ"), any(AddOfferRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/products/{productId}/offer", "productXYZ")
                .header("X-User-Id", "admin101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403_whenNonAdminTriesToAddOffer() throws Exception {
        AddOfferRequest request = new AddOfferRequest("store101", "DISCOUNT_10",
                LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 4, 30, 23, 59));
        when(userService.fetchUserById("user101")).thenReturn(regularUser());

        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/products/{productId}/offer", "product101")
                .header("X-User-Id", "user101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenNoUserIdHeaderProvidedForOffer() throws Exception {
        AddOfferRequest request = new AddOfferRequest("store101", "DISCOUNT_10",
                LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 4, 30, 23, 59));

        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/products/{productId}/offer", "product101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
