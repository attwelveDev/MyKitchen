package com.aaronnguyen.mykitchen.ui.other.Search;

import static com.aaronnguyen.mykitchen.FileWriter.FileUtils.updateCsvFile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Search.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of products in a RecyclerView.
 * This class handles the binding of product data to the views and manages user interactions.
 * @author u7515796 ChengboYan
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> allProductsList; // The complete product list
    private List<Product> showingProductList;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable fileUpdateRunnable;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.allProductsList = new ArrayList<>(productList); // Initialize with a copy of the complete list
        this.showingProductList = new ArrayList<>();
        for (Product product : productList) {
            for (int i = 0; i < 4; i++) {
                showingProductList.add(product);
            }
        }
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = showingProductList.get(position);
        holder.productName.setText(product.getName());
        holder.productType.setText("  " + product.getType());
        holder.expiryDays.removeTextChangedListener(holder.textWatcher);

        switch (position % 4) {
            case 0:
                holder.expiryDaysPrefix.setText("Pantry: ");
                holder.expiryDays.setText(String.valueOf(product.getPantryExpiryDays()));
                break;
            case 1:
                holder.expiryDaysPrefix.setText("Fridge: ");
                holder.expiryDays.setText(String.valueOf(product.getFridgeExpiryDays()));
                break;
            case 2:
                holder.expiryDaysPrefix.setText("Freezer: ");
                holder.expiryDays.setText(String.valueOf(product.getFreezerExpiryDays()));
                break;
            case 3:
                holder.expiryDaysPrefix.setText("Default: ");
                holder.expiryDays.setText(String.valueOf(product.getDefaultExpiryDays()));
                break;
        }

        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String newExpiryDays = s.toString();
                if (!newExpiryDays.isEmpty()) {
                    int newDays;
                    try {
                        newDays = Integer.parseInt(newExpiryDays);
                    } catch (NumberFormatException e) {
                        // Handle invalid number format
                        return;
                    }

                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        Product product = showingProductList.get(adapterPosition);
                        switch (adapterPosition % 4) {
                            case 0:
                                product.setPantryExpiryDays(newDays);
                                break;
                            case 1:
                                product.setFridgeExpiryDays(newDays);
                                break;
                            case 2:
                                product.setFreezerExpiryDays(newDays);
                                break;
                            case 3:
                                product.setDefaultExpiryDays(newDays);
                                break;
                        }
                        if (fileUpdateRunnable != null) {
                            handler.removeCallbacks(fileUpdateRunnable);
                        }
                        fileUpdateRunnable = () -> {
                            Log.i("show trees number", "" + allProductsList.size());
                            updateCsvFile(context, allProductsList, "item_list.csv");
                        };
                        handler.postDelayed(fileUpdateRunnable, 2000);
                    }
                }
            }
        };

        holder.expiryDays.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return showingProductList.size();
    }

    public void updateProductList(List<Product> productList) {
        this.showingProductList.clear();
        for (Product product : productList) {
            showingProductList.add(product);
            showingProductList.add(product);
            showingProductList.add(product);
            showingProductList.add(product);
        }
        notifyDataSetChanged();
    }

    public void updateAllProductsList(List<Product> allProductsList) {
        this.allProductsList = new ArrayList<>(allProductsList); // Update the complete product list
    }


    /**
     * ViewHolder class for individual product items within the RecyclerView.
     * It holds references to the views used to display the product name, type, and expiry days.
     * @author u7515796 ChenboYan
     *
     */
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView productName;
        public TextView productType;
        public TextView expiryDaysPrefix;
        public EditText expiryDays;
        public TextWatcher textWatcher;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productType = itemView.findViewById(R.id.productType);
            expiryDaysPrefix = itemView.findViewById(R.id.expiryDaysPrefix);
            expiryDays = itemView.findViewById(R.id.expiryDays);
        }
    }
}
