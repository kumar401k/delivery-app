package com.tw.joi.delivery.service;

import com.tw.joi.delivery.domain.Cart;
import com.tw.joi.delivery.domain.GroceryProduct;
import com.tw.joi.delivery.domain.User;
import com.tw.joi.delivery.dto.request.AddProductRequest;
import com.tw.joi.delivery.dto.response.CartProductInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CartServiceTest {

    @BeforeEach
    public void init() {
    }

    @Test
    public void getCartForUser() {
        //Assign
        UserService userService = mock(UserService.class);
        User user = new User();
        user.setUserId("user101");
        user.setEmail("user@mail.com");
        when(userService.fetchUserById("user101")).thenReturn(user);
        ProductService productService = mock(ProductService.class);
        CartService service = new CartService(userService, productService);

        //Act
        Cart cart = service.getCartForUser("user101");

        //Assert
        Assertions.assertNotNull(cart.getCartId());
    }

    @Test
    public void addProductToCartForUser_shouldAddProductWhenProductExists() {
        //Assign
        UserService userService = mock(UserService.class);
        User user = new User();
        user.setUserId("user101");
        user.setEmail("user@mail.com");
        when(userService.fetchUserById("user101")).thenReturn(user);

        GroceryProduct product = GroceryProduct.builder()
            .productId("product101")
            .productName("Apple")
            .mrp(new BigDecimal("50.00"))
            .sellingPrice(new BigDecimal("45.00"))
            .build();

        ProductService productService = mock(ProductService.class);
        when(productService.getProduct("product101", "store101")).thenReturn(Optional.of(product));

        CartService service = new CartService(userService, productService);

        AddProductRequest request = new AddProductRequest();
        request.setUserId("user101");
        request.setProductId("product101");
        request.setOutletId("store101");

        //Act
        CartProductInfo result = service.addProductToCartForUser(request);

        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("product101", result.product().getProductId());
        Assertions.assertEquals(new BigDecimal("45.00"), result.sellingPrice());
    }

    @Test
    public void addProductToCartForUser_shouldThrowExceptionWhenProductNotFound() {
        //Assign
        UserService userService = mock(UserService.class);
        User user = new User();
        user.setUserId("user101");
        user.setEmail("user@mail.com");
        when(userService.fetchUserById("user101")).thenReturn(user);

        ProductService productService = mock(ProductService.class);
        when(productService.getProduct("productXYZ", "store101")).thenReturn(Optional.empty());

        CartService service = new CartService(userService, productService);

        AddProductRequest request = new AddProductRequest();
        request.setUserId("user101");
        request.setProductId("productXYZ");
        request.setOutletId("store101");

        //Act & Assert
        Assertions.assertThrows(NoSuchElementException.class,
            () -> service.addProductToCartForUser(request));
    }

    @Test
    public void removeProductFromCart_shouldRemoveProductIfPresentInCart() {
        //Assign
        UserService userService = mock(UserService.class);
        User user = new User();
        user.setUserId("user101");
        user.setEmail("user@mail.com");
        when(userService.fetchUserById("user101")).thenReturn(user);

        ProductService productService = mock(ProductService.class);

        Map<String,Cart> userCarts = null;

        CartService service = new CartService(userService, productService);

        //Act
        Cart cart = service.removeProductFromCart("user101", "test");

        //Assert
        Assertions.assertNotNull(cart);
    }
}
