package com.example.demo.apis;

import com.example.demo.Product;
import com.example.demo.ProductService;
import com.example.demo.exceptions.DuplicatePartNumberException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController{

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String root(){
        return "Hello user";
    }

    @PostMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getProductCount(@RequestParam("filename") String filename) {
        try {
            List<Product> products = productService.readProductsFromXml(filename);
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("Number of records found: " + products.size());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).contentType(MediaType.TEXT_PLAIN).body("Error reading the file: " + e.getMessage());
        } catch (DuplicatePartNumberException e) {
            e.printStackTrace();
            return ResponseEntity.status(400).contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
        }
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProductByName(@PathVariable String name) {
        List<Product> product = productService.getProductsByName(name);
        if (!product.isEmpty()) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sorry, the product '" + name + "' was not found");
        }
    }
}
