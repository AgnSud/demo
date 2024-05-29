package com.example.demo;

import com.example.demo.apis.ProductController;
import com.example.demo.exceptions.DuplicatePartNumberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testGetAllProducts() throws Exception {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products/list")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testGetProductByName_Found() throws Exception {
        String name = "apple";
        List<Product> products = Arrays.asList(new Product());
        when(productService.getProductsByName(anyString())).thenReturn(products);

        mockMvc.perform(get("/products/{name}", name)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void testGetProductByName_NotFound() throws Exception {
        String name = "dress";
        when(productService.getProductsByName(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/products/{name}", name)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Sorry, the product '" + name + "' was not found"));
    }

    @Test
    public void testGetProductCount_Success() throws Exception {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productService.readProductsFromXml(anyString())).thenReturn(products);

        mockMvc.perform(post("/products/")
                        .param("filename", "products.xml")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Number of records found: 2"));
    }

    @Test
    public void testGetProductCount_DuplicatePartNumberException() throws Exception {
        when(productService.readProductsFromXml(anyString())).thenThrow(new DuplicatePartNumberException(List.of(new Product())));

        mockMvc.perform(post("/products/")
                        .param("filename", "products.xml")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));
    }

    @Test
    public void testGetProductCount_IOException() throws Exception {
        when(productService.readProductsFromXml(anyString())).thenThrow(new IOException("File not found"));

        mockMvc.perform(post("/products/")
                        .param("filename", "products.xml")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Error reading the file: File not found"));
    }
}
