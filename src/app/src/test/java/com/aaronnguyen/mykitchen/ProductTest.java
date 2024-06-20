package com.aaronnguyen.mykitchen;
import com.aaronnguyen.mykitchen.model.Search.Product;
import com.aaronnguyen.mykitchen.model.Search.ProductTree;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

/**
 * @author      Humza Shaikh u7293071@anu.edu.au
 */
public class ProductTest {

    @Test
    public void testSimple() {
        ProductTree mainTree = new ProductTree();
        mainTree.add(new Product("apple", "FRUIT", 1, 0, 0, 0));

        Product product = mainTree.getNode();
        assertEquals("Product name is not correct", product.getName(), "apple");
        assertEquals("Product Type is not correct", product.getType(), "FRUIT");
    }


    @Test
    public void testProductTreeAdd() {
        ProductTree mainTree = new ProductTree();
        Product apple = new Product("apple", "FRUIT", 1, 0, 0, 0);
        Product banana = new Product("banana", "FRUIT", 1, 0, 0, 0);

        mainTree.add(apple);
        System.out.println(mainTree.getProductList().toString());
        System.out.println(mainTree.getProductList().size());
        assertEquals("Tree does not contain correct number of elements", 1, mainTree.getProductList().size());


        mainTree.add(banana);
        System.out.println(mainTree.getProductList().toString());

        assertEquals("Tree does not contain correct number of elements", 2, mainTree.getProductList().size());

    }

    @Test(timeout=1000)
    public void testSearchSimpleMistake() {
        ProductTree mainTree = new ProductTree();

        Product apple = new Product("apple", "FRUIT", 1, 0, 0, 0);
        mainTree.add(apple);
        List<Product> searchResults = mainTree.search("apl", "");
        assertEquals("Returned list is not of correct size", 1, searchResults.size());
        Product searchResult = searchResults.get(0);
        assertEquals("Product name is not correct", "apple", searchResult.getName());
        assertEquals("Product type is not correct", "FRUIT", searchResult.getType() );
    }

    @Test
    public void testSearchRelevanceThreshold()  {
        ProductTree mainTree = new ProductTree();
        Product apple = new Product("apple", "FRUIT", 1, 0, 0, 0);
        Product banana = new Product("banana", "FRUIT", 1, 0, 0, 0);
        Product orange = new Product("orange", "FRUIT", 1, 0, 0, 0);
        Product otherApple = new Product("pinkladyapple", "FRUIT", 1, 0, 0, 0);
        Product closeToApple = new Product("awpxpylze", "FRUIT", 1, 0, 0, 0);
        Product notAnApple = new Product("applebcdefghijklmnopqrstuvwxyz", "FRUIT", 1, 0, 0, 0);

        mainTree.add(apple);
        mainTree.add(banana);
        mainTree.add(orange);
        //Establishing a relatively high relevance threshold. Makes search less tolerant to mistakes but still
        List<Product> searchResults = mainTree.search("apple", "");
        assertEquals("Returned list is not of correct size", 1, searchResults.size());
        Product searchResult = searchResults.get(0);
        assertEquals("Product name is not correct", "apple", searchResult.getName());
        assertEquals("Product type is not correct", "FRUIT", searchResult.getType() );

        mainTree.add(otherApple);
        searchResults = mainTree.search("apple", "");
        assertEquals("Returned list is not of correct size", 2, searchResults.size());

        mainTree.add(closeToApple);
        searchResults = mainTree.search("apple", "");
        assertEquals("Returned list is not of correct size", 3, searchResults.size());

        mainTree.add(notAnApple);
        searchResults = mainTree.search("apple", "");
        assertEquals("Returned list is not of correct size", 4, searchResults.size());

    }

    @Test
    public void testSearchEmpty()  {
        ProductTree mainTree = new ProductTree();
        Product apple = new Product("apple", "FRUIT", 1,0,0,0);
        Product banana = new Product("banana", "FRUIT", 1,0,0,0);
        Product orange = new Product("orange", "FRUIT", 1,0,0,0);
        mainTree.add(apple);
        mainTree.add(banana);
        mainTree.add(orange);

        List<Product> searchResults = mainTree.search("", "");
        assertEquals("Returned list is of correct size", 3, searchResults.size());
    }

    @Test
    public void testSearchType() {
        ProductTree mainTree = new ProductTree();

        Product apple = new Product("apple", "FRUIT", 1, 0, 0, 0);
        Product orange = new Product("orange", "FRUIT", 1, 0, 0, 0);

        Product milk = new Product("milk", "DAIRY", 1, 0, 0, 0);

        Product bread = new Product("bread", "GRAIN", 1, 0, 0, 0);

        Product potato = new Product("potato", "VEGETABLE", 1, 0, 0, 0);

        Product chicken = new Product("chicken", "PROTEIN", 1, 0, 0, 0);

        Product tea = new Product("tea", "OTHER", 1, 0, 0, 0);

        mainTree.add(apple);
        mainTree.add(orange);
        mainTree.add(milk);
        mainTree.add(bread);
        mainTree.add(potato);
        mainTree.add(chicken);
        mainTree.add(tea);

        System.out.println(mainTree.toString());

        List<Product> searchResults = mainTree.search(" ", "FRUIT");
        assertEquals("Returned list is not of correct size", 2, searchResults.size());

    }

    @Test
    public void testSearchTypeComplex() {
        ProductTree mainTree = new ProductTree();

        mainTree.add(new Product("apple", "FRUIT", 1, 0, 0, 0));
        mainTree.add(new Product("apple", "DAIRY", 1, 0, 0, 0));
        mainTree.add(new Product("apple", "GRAIN", 1, 0, 0, 0));
        mainTree.add(new Product("apple", "VEGETABLE", 1, 0, 0, 0));
        mainTree.add(new Product("apple", "PROTEIN", 1, 0, 0, 0));
        mainTree.add(new Product("apple", "OTHER", 1, 0, 0, 0));

        List<Product> searchResults = mainTree.search(" ", "FRUIT");
        System.out.println(searchResults);
        System.out.println(mainTree.display());
        assertEquals("Returned list is not of correct size", 1, searchResults.size());
        assertEquals("Product name is not correct", "apple", searchResults.get(0).getName());
        assertEquals("Product type is not correct", "FRUIT", searchResults.get(0).getType());

        searchResults = mainTree.search("apple", "");
        System.out.println(searchResults);
        System.out.println(mainTree.display());

        //assertEquals("Returned list is not of correct size", 6, searchResults.size());
    }
    @Test
    public void testSearchFilterComplex() {
        ProductTree mainTree = new ProductTree();

        mainTree.add(new Product("apple", "FRUIT", 1, 0, 0, 0));
        mainTree.add(new Product("banana", "FRUIT", 1, 0, 0, 0));
        mainTree.add(new Product("pinkladyapple", "FRUIT", 1, 0, 0, 0));
        mainTree.add(new Product("orange", "FRUIT", 1, 0, 0, 0));
        mainTree.add(new Product("cherry", "FRUIT", 1, 0, 0, 0));


        List<Product> searchResults = mainTree.search("", "FRUIT");
        assertEquals("Returned list is not of correct size", 5, searchResults.size());

        searchResults = mainTree.search("apple", "");
        System.out.println(searchResults);
        System.out.println(mainTree);

        //assertEquals("Returned list is not of correct size", 6, searchResults.size());
    }

    @Test
    public void testRealData() {
        ProductTree mainTree = new ProductTree();
        mainTree.add(new Product("organic Chopped Kale", "VEGETABLE",25,24,11,20));
        mainTree.add(new Product("greek Style Natural Yoghurt","DAIRY",27,20,24,28));

        List<Product> results = mainTree.search("organic", "");
        assertEquals("Did not return expected result", 1, results.size());
        assertEquals("did not return correct item", "organic Chopped Kale", results.get(0).getName());
    }
}
