package com.aaronnguyen.mykitchen.model.Items;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;

import java.util.ArrayList;

/**
 * @author      Humza Shaikh u7293071@anu.edu.au
 */
public class Search {
    /**
     * Search through current kitchen items that match the search
     *
     * @param  itemList List of items to search through
     * @param  target   string being searched for
     * @return List of items that match the search
     */
    public static ArrayList<Item> search(ArrayList<Item> itemList, String target) {
        if (target == "") return itemList;
        int searchThreshold = 4;
        ArrayList<Item> searchResults = new ArrayList<>();

        for (Item curr : itemList) {
            String item = curr.getName().replace("\\s+", "").toLowerCase();

            if (editDist(item, target, searchThreshold) < searchThreshold) {
              searchResults.add(curr);
            }

        }
        return searchResults;
    }

    /**
     * Search through current shopping cart items that match the search
     *
     * @param  itemList List of items to search through
     * @param  target   string being searched for
     * @return List of items that match the search
     */
    public static ArrayList<CartItem> searchCartItem(ArrayList<CartItem> itemList, String target) {
        if (target == "") return itemList;
        int searchThreshold = 4;
        ArrayList<CartItem> searchResults = new ArrayList<>();
        for (CartItem curr : itemList) {
            String item = curr.getName().replace("\\s+", "").toLowerCase();

            if (editDist(item, target, searchThreshold) < searchThreshold) {
                searchResults.add(curr);
            }
        }
        return searchResults;
    }

    /**
     * Find the edit distance between two strings
     *
     * @param  s1   First string being compared
     * @param  s2   Second string being compared
     * @param limit maximum edit distance before nullifying search
     * @return Integer representing the edit distance between the two strings
     */
    public static int editDist(String s1, String s2, int limit) {
        if (s1 == null) throw new NullPointerException("s1 must not be null");
        if (s2 == null) throw new NullPointerException("s2 must not be null");
        if (s1.equals(s2)) return 0;
        if (s1.length() == 0) return s2.length();
        if (s2.length() == 0) return s1.length();

        // create two work vectors of integer distances
        int[] v0 = new int[s2.length() + 1];
        int[] v1 = new int[s2.length() + 1];
        int[] vtemp;

        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < v0.length; i++) v0[i] = i;

        for (int i = 0; i < s1.length(); i++) {
            // calculate v1 (current row distances) from the previous row v0
            // first element of v1 is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;
            int minv1 = v1[0];

            // use formula to fill in the rest of the row
            for (int j = 0; j < s2.length(); j++) {
                int cost = 1;
                if (s1.charAt(i) == s2.charAt(j)) cost = 0;
                v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost));
                minv1 = Math.min(minv1, v1[j + 1]);
            }
            if (minv1 >= limit) return limit;
            // Flip references to current and previous row
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;
        }
        return v0[s2.length()];
    }
}
