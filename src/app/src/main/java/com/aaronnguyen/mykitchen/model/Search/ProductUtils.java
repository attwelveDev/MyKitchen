    package com.aaronnguyen.mykitchen.model.Search;

    import android.content.Context;
    import android.util.Log;

    import java.io.BufferedReader;
    import java.io.File;
    import java.io.FileOutputStream;
    import java.io.FileReader;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.OutputStream;

    /**
     * Utility class for handling product-related operations.
     * This class is used to parse a CSV file and add products to the ProductTree.
     *
     * @author u7517596 Chengbo Yan
     */

    public class ProductUtils {

        private static ProductTree productTree = null;

        /**
         * It initializes the ProductTree by loading products from the specified file if not already initialized.
         *
         * @param context the application context
         * @param fileName the name of the file containing product data
         * @return the instance of ProductTree
         */
        public static ProductTree getInstance(Context context, String fileName) {
            if (productTree == null) {
                productTree = loadProductsFromFile(context, fileName);
            }
            return productTree;
        }

        /**
         * Loads products from a CSV file and adds them to the ProductTree.
         *
         * @param context the application context
         * @param fileName the name of the file containing product data
         * @return the ProductTree containing loaded products
         */
        public static ProductTree loadProductsFromFile(Context context, String fileName) {
            File file = new File(context.getFilesDir(), fileName);

            if (!file.exists()) {
                copyFileFromAssets(context, fileName, file.getAbsolutePath());
            }
            ProductTree products = new ProductTree();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    String[] tokens = line.split(",");
                    if (tokens.length >= 6) {
                        try {
                            Product product = new Product(tokens[0].trim(), tokens[1].trim().toUpperCase(),
                                    Integer.parseInt(tokens[2].trim()), Integer.parseInt(tokens[3].trim()),
                                    Integer.parseInt(tokens[4].trim()), Integer.parseInt(tokens[5].trim()));
                            products.add(product);
                        } catch (NumberFormatException e) {
                            Log.e("ProductUtils", "Error parsing integer from file for expiry days", e);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("ProductUtils", "Error loading products from file", e);
            }
            return products;
        }

        /**
         * Copies a file from the assets folder to the app's file directory.
         *
         * @param context the application context
         * @param fileName the name of the file to copy
         * @param destinationPath the destination path where the file should be copied
         */
        private static void copyFileFromAssets(Context context, String fileName, String destinationPath) {
            try (InputStream is = context.getAssets().open(fileName);
                 OutputStream os = new FileOutputStream(destinationPath)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } catch (IOException e) {
                Log.e("ProductUtils", "Error copying file from assets", e);
            }
        }
    }

