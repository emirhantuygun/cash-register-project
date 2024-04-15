package com.bit.productservice.initializer;

import com.bit.productservice.entity.Product;
import com.bit.productservice.repository.ProductRepository;
import com.bit.productservice.service.BarcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final BarcodeService barcodeService;

    @Override
    @Transactional
    public void run(String... args) {
        initializeProducts();
    }

    private void initializeProducts() {

        for (int i = 1; i <= 20; i++) {
            String name = String.format("Product %d", i);
            String description = String.format("This is product %d.", i);
            BigDecimal price = BigDecimal.valueOf(i*10);

            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .barcodeNumber(barcodeService.generateBarcodeNumber(name))
                    .price(price).build();

            productRepository.save(product);
        }
    }

}
