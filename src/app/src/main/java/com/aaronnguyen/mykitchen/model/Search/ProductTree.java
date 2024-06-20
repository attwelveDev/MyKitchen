package com.aaronnguyen.mykitchen.model.Search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author      Humza Shaikh u7293071@anu.edu.au
 */
public class ProductTree {
    ProductTree origin;
    ProductTree parent;
    public Product node;
    ProductTree left;
    ProductTree right;

    public ProductTree() {

        this.parent = null;
        this.node = null;
        this.right = null;
        this.left = null;
    }

    public ProductTree(ProductTree parent) {
        this.parent = parent;
        this.node = null;
        this.left = null;
        this.right = null;
    }

    public ProductTree(ProductTree parent, Product node, ProductTree left, ProductTree right) {
        this.parent = parent;
        this.node = node;
        this.left = left;
        this.right = right;
    }

    /**
     * Add element to the tree
     *
     * @param  product  Product being added to the tree
     * @return Boolean representing if the product was successfully added to the tree
     */
    public boolean add(Product product) {
        //Log.i("show trees", "" + this.toString());
        boolean rtn = false;
        if (node == null) {
            node = product;
            left = new ProductTree(this);
            right = new ProductTree(this);
            rtn = true;
        } else {
            if (goLeft(product.toString())) {
                 rtn = left.add(product);
            } else {
                 rtn = right.add(product);
            }
        }
        if (getBalanceFactor() > 1 || getBalanceFactor() < -1) {
            balanceTree();
        }

        return rtn;

    }

    /**
     * Balance the tree using rotations
     * Balances subtrees first to ensure balanceFactor is always 1 or less
     */
    public void balanceTree() {
        int balanceFactor = getBalanceFactor();
        if (balanceFactor == 0 || balanceFactor == -1 || balanceFactor == 1) return;
        if (balanceFactor > 1) {
            right.balanceTree();
            rightRotate();
        } else {
            left.balanceTree();
            leftRotate();
        }
    }

    /**
     * Search the tree for the elements that best match a string
     *
     * @param  searchTerm  string being searched for
     * @return List of products that match target string, ordered from most relevant to least
     */
    public List<Product> search(String searchTerm, String filter) {
        if (node == null) return new ArrayList<>();
        if (searchTerm.equals("") && filter.equals("")) return getProductList();

        //Track items and their similarity
        Map<Product, Integer> searchResultsMap = new HashMap<>();

        searchHelper(searchTerm, searchResultsMap, filter);
        //Iterate and find x most relevant items
        return orderSearchResults(searchResultsMap);
    }

    /**
     * Helper function to sort searched items by editDistance ascending
     *
     * @param  map  HashMap of products and an integer representing their associated editDistance
     * @return List of products, ordered from lowest to highest editDistance
     */
    public List<Product> orderSearchResults(Map<Product, Integer> map) {
        Map<Product, Integer> sortedMap =
                map.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
        return new ArrayList<>(sortedMap.keySet());
    }


    /**
     * Helper function to recursively iterate through the tree to find products that match the search
     *
     * @param  target  String being searched for
     * @param  searchResultsMap  HashMap of products and an integer representing their associated editDistance
     * @param  filter String representing if the search query has a filter or not
     */
    public void searchHelper(String target, Map<Product, Integer> searchResultsMap, String filter) {
        //Similarity threshold
        if (node == null) return;
        if (node.getType().equals(filter) || filter.equals("")) {
            String currentNode = node.getName().replaceAll("\\s+","").toLowerCase();
            int searchThreshold = filter.equals("") ? 5 : 10;
            int editDist = editDist(target, currentNode, searchThreshold);
            if (currentNode.contains(target)) editDist = -1;
            if (target.equals("")) editDist = -1;
            if (editDist < searchThreshold ) {
                searchResultsMap.put(node, editDist);
            }
        }
        left.searchHelper(target, searchResultsMap, filter);
        right.searchHelper(target, searchResultsMap, filter);
    }

    /**
     * Heuristic to determine order of the tree. If a string is alphabetically greater, then go left
     * otherwise go right. Works for both insertion and search.
     * @param  s    string being compared to the current node
     * @return Boolean representing if the next node should be left or right of current node
     */
    public boolean goLeft(String s) {

        String currentNode = node.getType() + node.getName().replaceAll("\\s+","").toLowerCase();
        if (s == currentNode) {
            return false;
        }
        int i = 0;
        while (s.charAt(i) == currentNode.charAt(i)) {
            i++;
            if (i == s.length()) {
                return false;
            }
            if (i == currentNode.length()) {
                return true;
            }
        }

        return s.charAt(i) < currentNode.charAt(i);
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



    /**
     * Get an arraylist of all products in the current tree
     *
     * @return An array list containing all products in the current tree
     */
    public List<Product> getProductList() {
        if (node == null) return new ArrayList<Product>();
        List<Product> products = new ArrayList<>();
        products.add(node);
        products.addAll(left.getProductList());
        products.addAll(right.getProductList());
        return products;
    }

    /** Rotate the node so it becomes the child of its left branch
    /*
     e.g.
            [x]                    a
           /   \                 /   \
         a       b     == >     c     [x]
        / \     / \                   /  \
       c  d    e   f                 d    b
      / \
     e   f
     */
    public void rightRotate() {
        ProductTree newX = new ProductTree(this.left, this.node, this.left.right, this.right);
        ProductTree c = this.left.left;

        this.node = this.left.node;
        this.left = c;
        this.right = newX;

    }

    /** Rotate the node so it becomes the child of its right branch
     /*
     e.g.
            [x]                    b
           /   \                 /   \
         a       b     == >   [x]     f
        / \     / \           /  \
       c  d    e   f         a    e
      / \
     c   d
     */
    public void leftRotate() {
        ProductTree newX = new ProductTree(this.right, this.node, this.left, this.right.left);
        ProductTree f = this.right.right;

        this.node = this.right.node;
        this.right = f;
        this.left = newX;

    }

    /**
     * Get the difference in heights of the left and right branches
     *
     * @return An integer representing the difference in heights between the node's branches
     * */
    public int getBalanceFactor() {
        if (node == null) return 0;
        return left.height() - right.height();
    }

    /**
     * Get height of current node
     *
     * @return height of the current node
     * */
    public int height() {
        if (node == null) return 0;
        return 1 + Math.max(left.height(), right.height());
    }

    /**
     * Display method to make the tree readable
     *
     * @return A visualization of the tree as a String
     * */
    public String display() {
        return display(0);
    }

    public String display(int tabs) {
        if (node == null) return "";

        StringBuilder sb = new StringBuilder(node.toString());
        sb.append("\n").append("\t".repeat(tabs)).append("├─").append(left.display(tabs + 1));
        sb.append("\n").append("\t".repeat(tabs)).append("├─").append(right.display(tabs + 1));
        return sb.toString();
    }

    public Product getNode() {
        return this.node;
    }

    public ProductTree getParent() {
        return this.parent;
    }

    public ProductTree getRight() {
        return this.right;
    }
    public ProductTree getLeft() {
        return this.left;
    }

    @Override
    public String toString() {
        if (this.node == null) return "|";
        return " < " + this.left.toString() + "{" + this.node.toString() + "}" + this.right.toString() + " > ";
    }
}