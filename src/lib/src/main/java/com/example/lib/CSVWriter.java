package com.example.lib;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * Writes list of items to app/src/main/res/raw/item_list.csv
 * @author u7648367 Ruixian Wu
 */
public class CSVWriter {
    static String outputPath = "app/src/main/res/raw/item_list.csv";
    public static void main(String[] args) throws FileNotFoundException {

        HashSet<ItemCSV> items = CSVReader.read();

        System.out.println(items.size());

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputPath));

            out.write("Item Name,Item Type,Expiry Days in Pantry,Expiry Days in Fridge,Expiry Days in Freezer,Default\n");

            int i = items.size();

            for (ItemCSV item : items) {

                out.write(item.name + ","
                        + item.type + ","
                        + item.pantryExpiryDays + ","
                        + item.fridgeExpiryDays + ","
                        + item.freezerExpiryDays + ","
                        + item.defaultExpiryDays);

                if (i-- > 1) {
                    out.write("\n");
                }
            }

            out.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
