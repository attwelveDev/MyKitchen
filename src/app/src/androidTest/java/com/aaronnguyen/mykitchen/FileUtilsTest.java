package com.aaronnguyen.mykitchen;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.aaronnguyen.mykitchen.FileWriter.FileUtils;
import com.aaronnguyen.mykitchen.model.Search.Product;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileUtilsTest {
    private Context mockContext;
    private File testFile;
    private List<Product> productList;

    @Before
    public void setUp() {
        mockContext = ApplicationProvider.getApplicationContext();
        testFile = new File(mockContext.getFilesDir(), "test_item_list3.csv");

        productList = new ArrayList<>();
        productList.add(new Product("Apple", "Fruit", 10, 5, 20, 15));
        productList.add(new Product("Milk", "Dairy", 7, 3, 30, 10));
    }

    @Test
    public void testUpdateCsvFile() throws Exception {

        FileUtils.updateCsvFile(mockContext, productList, "test_item_list3.csv");

        BufferedReader reader = new BufferedReader(new FileReader(testFile));
        StringBuilder fileContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line).append("\n");
        }
        reader.close();

        String expectedContent = "Item Name,Item Type,Pantry Expiry Days,Fridge Expiry Days,Freezer Expiry Days,Default Expiry Days\n" +
                "Apple,Fruit,10,5,20,15\n" +
                "Milk,Dairy,7,3,30,10\n";

        assertEquals(expectedContent, fileContent.toString());

        assertTrue(testFile.exists());

        if (testFile.exists()) {
            assertTrue(testFile.delete());
        }
    }
}
