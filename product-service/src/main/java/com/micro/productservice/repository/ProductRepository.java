package com.micro.productservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.micro.productservice.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
}
