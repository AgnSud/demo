package com.example.demo;

import com.example.demo.exceptions.DuplicatePartNumberException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReadProductsFromXml_NoDuplicates() throws IOException, DuplicatePartNumberException {
        String filename = "src/data/products_test_no_duplicates.xml"; // Ensure this path is correct
        String filePath = System.getProperty("user.dir") + File.separator + filename;

        XmlMapper xmlMapper = new XmlMapper();
        ProductsWrapper productsWrapper = xmlMapper.readValue(new File(filePath), ProductsWrapper.class);

        when(productRepository.findAll()).thenReturn(List.of());

        ArgumentCaptor<List<Product>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRepository, times(1)).saveAll(argumentCaptor.capture());

        List<Product> capturedProducts = argumentCaptor.getValue();

        assertEquals(productsWrapper.getProducts().size(), capturedProducts.size());

        verify(productRepository, times(2)).findAll();
    }


    @Test
    public void testReadProductsFromXml_WithDuplicates() throws IOException {
        String filename = "src/data/products_test_duplicates.xml";
        String filePath = System.getProperty("user.dir") + File.separator + filename;
        XmlMapper xmlMapper = new XmlMapper();

        ProductsWrapper productsWrapper = xmlMapper.readValue(new File(filePath), ProductsWrapper.class);

        Product existingProduct1 = new Product();
        existingProduct1.setId(1);
        existingProduct1.setPartNumberNR("J1A-G-M-W982F-PO");
        existingProduct1.setName("desk");

        Product existingProduct2 = new Product();
        existingProduct2.setId(2);
        existingProduct2.setPartNumberNR("E7R-Q-M-K287B-YH");
        existingProduct2.setName("lamp");

        List<Product> existingProducts = List.of(existingProduct1, existingProduct2);

        when(productRepository.findAll()).thenReturn(existingProducts);

        List<Product> productsWithDuplicates = productsWrapper.getProducts().stream()
                .filter(p -> existingProducts.stream()
                        .map(Product::getPartNumberNR)
                        .collect(Collectors.toSet())
                        .contains(p.getPartNumberNR()))
                .toList();

        DuplicatePartNumberException exception = assertThrows(DuplicatePartNumberException.class, () -> productService.readProductsFromXml(filename));

        String expectedMessage = "Duplicate partNumberNR found for the following products: " +
                productsWithDuplicates.stream()
                        .map(product -> String.format("[id=%d, name=%s, partNumberNR=%s]", product.getId(), product.getName(), product.getPartNumberNR()))
                        .collect(Collectors.joining(", "));
        assertEquals(expectedMessage, exception.getMessage());

        ArgumentCaptor<List<Product>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRepository, times(1)).saveAll(argumentCaptor.capture());

        List<Product> capturedProducts = argumentCaptor.getValue();

        List<Product> expectedNewProducts = productsWrapper.getProducts().stream()
                .filter(p -> !existingProducts.stream()
                        .map(Product::getPartNumberNR)
                        .collect(Collectors.toSet())
                        .contains(p.getPartNumberNR()))
                .collect(Collectors.toList());

        assertEquals(expectedNewProducts.size(), capturedProducts.size());
        assertTrue(containsAllByPartNumberNRAndName(capturedProducts, expectedNewProducts));

        verify(productRepository, times(1)).findAll();
    }

    private boolean containsAllByPartNumberNRAndName(List<Product> list1, List<Product> list2) {
        for (Product p2 : list2) {
            boolean found = false;
            for (Product p1 : list1) {
                if (p1.getPartNumberNR().equals(p2.getPartNumberNR()) && p1.getName().equals(p2.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testGetAllProducts() {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testGetProductsByName() {
        String name = "apple";
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productRepository.findByNameIgnoreCase(name)).thenReturn(products);

        List<Product> result = productService.getProductsByName(name);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findByNameIgnoreCase(name);
    }
}
