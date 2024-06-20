package com.aaronnguyen.mykitchen;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.aaronnguyen.mykitchen.model.Search.Product;
import com.aaronnguyen.mykitchen.model.Search.ProductTree;
import com.aaronnguyen.mykitchen.model.Search.ProductUtils;

public class ProductUtilsTest {
    private Context mockContext;
    private File testFile;

    @Before
    public void setUp() throws Exception {
        mockContext = ApplicationProvider.getApplicationContext();
        testFile = new File(mockContext.getFilesDir(), "test_products.csv");

        // Create a test file with test data
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(testFile))) {
            writer.write("Item Name,Item Type,Pantry Expiry Days,Fridge Expiry Days,Freezer Expiry Days,Default Expiry Days\n");
            writer.write("Apple,Fruit,10,5,20,15\n");
            writer.write("Milk,Dairy,7,3,30,10\n");
        }
    }

    @Test
    public void testLoadProductsFromFile() {
        ProductTree products = ProductUtils.loadProductsFromFile(mockContext, "test_products.csv");

        // Verify the loaded product list
        assertEquals(2, products.getProductList().size());

        // Verify product attributes
        Product apple = products.getProductList().stream()
                .filter(product -> product.getName().equals("Apple"))
                .findFirst()
                .orElse(null);
        assertTrue(apple != null);
        assertEquals("Apple", apple.getName());
        assertEquals("Fruit", apple.getType());
        assertEquals(10, apple.getPantryExpiryDays());
        assertEquals(5, apple.getFridgeExpiryDays());
        assertEquals(20, apple.getFreezerExpiryDays());
        assertEquals(15, apple.getDefaultExpiryDays());

        Product milk = products.getProductList().stream()
                .filter(product -> product.getName().equals("Milk"))
                .findFirst()
                .orElse(null);
        assertTrue(milk != null);
        assertEquals("Milk", milk.getName());
        assertEquals("Dairy", milk.getType());
        assertEquals(7, milk.getPantryExpiryDays());
        assertEquals(3, milk.getFridgeExpiryDays());
        assertEquals(30, milk.getFreezerExpiryDays());
        assertEquals(10, milk.getDefaultExpiryDays());
    }

}
