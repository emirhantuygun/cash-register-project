package com.bit.productservice.initializer;

import com.bit.productservice.entity.Product;
import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.repository.ProductRepository;
import com.bit.productservice.service.BarcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * This class is responsible for initializing the product data in the database.
 * It implements the CommandLineRunner interface to run the initialization process when the application starts.
 *
 * @author Emirhan Tuygun
 */
@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final BarcodeService barcodeService;

    /**
     * This method is responsible for running the initialization process of product data in the database.
     * It is called when the application starts due to implementing the CommandLineRunner interface.
     *
     * @param args Command line arguments. Not used in this method.
     * @throws AlgorithmNotFoundException If the barcode generation algorithm is not found.
     */
    @Override
    @Transactional
    public void run(String... args) throws AlgorithmNotFoundException {
        initializeProducts();
    }

    /**
     * This method initializes the product data in the database.
     * It creates 20 sample products with unique names, descriptions, prices, and generates barcodes.
     * The stock quantity of each product is set to 10.
     *
     * @throws AlgorithmNotFoundException If the barcode generation algorithm is not found.
     */
    private void initializeProducts() throws AlgorithmNotFoundException {

        for (int i = 1; i <= 20; i++) {
            String name = String.format("Product %d", i);
            String description = String.format("This is product %d.", i);
            BigDecimal price = BigDecimal.valueOf(i*10);

            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .barcodeNumber(barcodeService.generateBarcodeNumber(name))
                    .stockQuantity(10)
                    .price(price).build();

            productRepository.save(product);
        }
    }

}
