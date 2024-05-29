package com.example.demo;

import com.example.demo.exceptions.DuplicatePartNumberException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Set;

import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> readProductsFromXml(String filename) throws IOException, DuplicatePartNumberException {
        String filePath = System.getProperty("user.dir") + File.separator + filename;
        XmlMapper xmlMapper = new XmlMapper();
        ProductsWrapper productsWrapper = xmlMapper.readValue(new File(filePath), ProductsWrapper.class);

        Set<String> existingPartNumbers = productRepository.findAll().stream()
                .map(Product::getPartNumberNR)
                .collect(Collectors.toSet());

        List<Product> newUniqueProducts = new ArrayList<>();
        List<Product> duplicatesProducts = new ArrayList<>();
        for (Product product : productsWrapper.getProducts()) {
            if (existingPartNumbers.contains(product.getPartNumberNR())) {
                duplicatesProducts.add(product);
            } else {
                newUniqueProducts.add(product);
                existingPartNumbers.add(product.getPartNumberNR());
            }
        }
        productRepository.saveAll(newUniqueProducts);

        if (!duplicatesProducts.isEmpty()){
            throw new DuplicatePartNumberException(duplicatesProducts);
        }
        return productRepository.findAll();
    }

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    public List<Product> getProductsByName(String name) {
        return productRepository.findByNameIgnoreCase(name);
    }
}
