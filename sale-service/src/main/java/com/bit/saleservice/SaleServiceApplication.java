package com.bit.saleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SaleServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaleServiceApplication.class, args);
	}

}
