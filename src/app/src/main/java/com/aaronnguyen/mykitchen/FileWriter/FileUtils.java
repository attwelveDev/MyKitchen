package com.aaronnguyen.mykitchen.FileWriter;

import android.content.Context;
import android.util.Log;

import com.aaronnguyen.mykitchen.model.Search.Product;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


/**
 * Utility class for writing data to CSV files.
 * This class provides a static method to update a CSV file with a list of products.
 * @author u7515796 ChengboYan
 */
public class FileUtils {

    /**
     * Updates a CSV file with the provided list of products.
     *
     * @param context  The context of the application.
     * @param products The list of products to be written to the CSV file.
     * @param filename The name of the CSV file.
     */
    public static void updateCsvFile(Context context, List<Product> products, String filename) {
        File file = new File(context.getFilesDir(), filename);
        try {
            Log.i("FileUtils", "Writing to file at: " + file.getAbsolutePath());
            FileWriter writer = new FileWriter(file, false); // false to overwrite.
            writer.append("Item Name,Item Type,Pantry Expiry Days,Fridge Expiry Days,Freezer Expiry Days,Default Expiry Days\n");
            for (Product product : products) {
                writer.append(product.getName()).append(",")
                        .append(product.getType()).append(",")
                        .append(String.valueOf(product.getPantryExpiryDays())).append(",")
                        .append(String.valueOf(product.getFridgeExpiryDays())).append(",")
                        .append(String.valueOf(product.getFreezerExpiryDays())).append(",")
                        .append(String.valueOf(product.getDefaultExpiryDays())).append("\n");
            }
            writer.flush();
            writer.close();
            Log.i("FileUtils", "CSV file has been updated.");
        } catch (IOException e) {
            Log.e("FileUtils", "Error writing to CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
