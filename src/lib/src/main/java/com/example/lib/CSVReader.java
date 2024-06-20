package com.example.lib;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reads list of items from app/src/main/res/raw/australia_grocery_2022sep.csv
 * @author u7648367 Ruixian Wu
 */
public class CSVReader {
    static Random rand = new Random();

    public static HashSet<ItemCSV> read() throws FileNotFoundException {
        HashSet<ItemCSV> items = new HashSet<ItemCSV>();
        int index = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("app/src/main/res/raw/australia_grocery_2022sep_processed.csv"))) {

            String record;
            while((record = br.readLine()) != null && index <= 3000) {
                AtomicInteger tokenIndex = new AtomicInteger(2);
                String[] tokens = record.split(",");

                //Getting category name
                StringBuilder category = getToken(tokens, tokenIndex);

                //Getting sub-category name
                StringBuilder subCategory = getToken(tokens, tokenIndex);

                //Moving tokenIndex over the product type
                StringBuilder productType = getToken(tokens, tokenIndex);

                String name = tokens[tokenIndex.get()];

                System.out.println("category: " + category);
                System.out.println("subCategory: " + subCategory);
                System.out.println("name: " + name);


                String type = category(category.toString(), subCategory.toString());


                int pantryExpiryDays = 0;
                int fridgeExpiryDays = 0;
                int freezerExpiryDays = 0;
                int defaultExpiryDays = 0;


                while (pantryExpiryDays == 0) {
                    pantryExpiryDays = rand.nextInt(30);
                }

                while (fridgeExpiryDays == 0) {
                    fridgeExpiryDays = rand.nextInt(30);
                }

                while (freezerExpiryDays == 0) {
                    freezerExpiryDays = rand.nextInt(30);
                }

                while (defaultExpiryDays == 0) {
                    defaultExpiryDays = rand.nextInt(30);
                }


                items.add(new ItemCSV(name.toString(),
                        type,
                        pantryExpiryDays,
                        fridgeExpiryDays,
                        freezerExpiryDays,
                        defaultExpiryDays));

                index++;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    public static StringBuilder getToken(String[] tokens, AtomicInteger tokenIndex) {
        StringBuilder stringBuilder = new StringBuilder(tokens[tokenIndex.get()]);
        if (tokens[tokenIndex.get()].charAt(0) == '"') {
            tokenIndex.set(tokenIndex.get() + 1);
            stringBuilder.append(tokens[tokenIndex.get()]);
            if (tokens[tokenIndex.get()].charAt(tokens[tokenIndex.get()].length() - 1) != '\"') {
                while (tokens[tokenIndex.get()].charAt(0) != '"') {
                    stringBuilder.append(tokens[tokenIndex.get()]);
                    tokenIndex.set(tokenIndex.get() + 1);
                }
            }


            stringBuilder.deleteCharAt(0);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        }

        tokenIndex.set(tokenIndex.get() + 1);

        return stringBuilder;

    }


    public static String category(String category, String subCategory) {
        if (category.equals("Meat & seafood")) {
            return "Protein";
        }
        if (category.equals("Dairy eggs & fridge")) {
            return "Dairy";
        }
        if (category.equals("Fruit & vegetables")) {
            if (subCategory.equals("Fruit")) {
                return "Fruit";
            } else if (subCategory.equals("Vegetables")) {
                return "Vegetable";
            } else {
                return "";
            }
        }

        if (category.equals("Bakery")) {
            return "Grain";
        }

        return "OtherItem";
    }
}
