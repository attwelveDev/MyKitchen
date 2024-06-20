package com.aaronnguyen.mykitchen.ui.main.ShoppingCart;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartDairy;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartFruit;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartGrain;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItemFactory;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartOtherItem;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartProtein;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartVegetable;
import com.aaronnguyen.mykitchen.model.Items.Search;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.Overview.OverviewViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author u7648367 Ruixian Wu
 */

public class CartViewModel extends ViewModel {
    public static final String DATE_DISPLAY_FORMAT = "dd/MM/yyyy";

    ShoppingCartFragment cartViewFragment;

    /**
     * List of all item categories
     */
    private final List<String> groupList;
    /**
     * Map whose keys are the item categories, and the value is the list of the item names of items whose item type is of that item category
     */
    protected Map<String, List<String>> itemCollection;
    /**
     * Map whose keys are the item categories, and the value is the list of the expiry days of items whose item type is of that item category
     */
    protected Map<String, List<String>> itemExpiryDays;
    /**
     * Map whose keys are the item categories, and the value is the list of the quantities of items whose item type is of that item category
     */
    protected Map<String, List<String>> itemQuantity;
    /**
     * Map whose keys are the item categories, and the value is the list of the storage locations of items whose item type is of that item category
     */
    protected Map<String, List<String>> itemLocation;
    /**
     * Map whose keys are the item categories, and the value is the list of the schedules of all items whose item type is of that item category
     */
    protected Map<String, List<List<String>>> itemSchedule;

    private Kitchen kitchen;
    private User currentUser;

    public CartViewModel(Kitchen kitchen, User currentUser) {
        this.kitchen = kitchen;
        this.currentUser = currentUser;

        groupList = new ArrayList<>();
        itemCollection = new HashMap<>();
        itemExpiryDays = new HashMap<>();
        itemQuantity = new HashMap<>();
        itemLocation = new HashMap<>();
        itemSchedule = new HashMap<>();

        createGroupList();
    }

    public void setShoppingCartFragment(ShoppingCartFragment fragment) {
        this.cartViewFragment = fragment;
    }

    /**
     * Creates the item category structure of the different maps
     */
    private void createCollection() {
        itemCollection.put(OverviewViewModel.PROTEIN_CAT_NAME, new LinkedList<>());
        itemCollection.put(OverviewViewModel.VEG_CAT_NAME, new LinkedList<>());
        itemCollection.put(OverviewViewModel.FRUIT_CAT_NAME, new LinkedList<>());
        itemCollection.put(OverviewViewModel.DAIRY_CAT_NAME, new LinkedList<>());
        itemCollection.put(OverviewViewModel.GRAIN_CAT_NAME, new LinkedList<>());
        itemCollection.put(OverviewViewModel.OTHER_CAT_NAME, new LinkedList<>());

        itemExpiryDays.put(OverviewViewModel.PROTEIN_CAT_NAME, new LinkedList<>());
        itemExpiryDays.put(OverviewViewModel.VEG_CAT_NAME, new LinkedList<>());
        itemExpiryDays.put(OverviewViewModel.FRUIT_CAT_NAME, new LinkedList<>());
        itemExpiryDays.put(OverviewViewModel.DAIRY_CAT_NAME, new LinkedList<>());
        itemExpiryDays.put(OverviewViewModel.GRAIN_CAT_NAME, new LinkedList<>());
        itemExpiryDays.put(OverviewViewModel.OTHER_CAT_NAME, new LinkedList<>());

        itemQuantity.put(OverviewViewModel.PROTEIN_CAT_NAME, new LinkedList<>());
        itemQuantity.put(OverviewViewModel.VEG_CAT_NAME, new LinkedList<>());
        itemQuantity.put(OverviewViewModel.FRUIT_CAT_NAME, new LinkedList<>());
        itemQuantity.put(OverviewViewModel.DAIRY_CAT_NAME, new LinkedList<>());
        itemQuantity.put(OverviewViewModel.GRAIN_CAT_NAME, new LinkedList<>());
        itemQuantity.put(OverviewViewModel.OTHER_CAT_NAME, new LinkedList<>());

        itemLocation.put(OverviewViewModel.PROTEIN_CAT_NAME, new LinkedList<>());
        itemLocation.put(OverviewViewModel.VEG_CAT_NAME, new LinkedList<>());
        itemLocation.put(OverviewViewModel.FRUIT_CAT_NAME, new LinkedList<>());
        itemLocation.put(OverviewViewModel.DAIRY_CAT_NAME, new LinkedList<>());
        itemLocation.put(OverviewViewModel.GRAIN_CAT_NAME, new LinkedList<>());
        itemLocation.put(OverviewViewModel.OTHER_CAT_NAME, new LinkedList<>());

        itemSchedule.put(OverviewViewModel.PROTEIN_CAT_NAME, new LinkedList<>());
        itemSchedule.put(OverviewViewModel.VEG_CAT_NAME, new LinkedList<>());
        itemSchedule.put(OverviewViewModel.FRUIT_CAT_NAME, new LinkedList<>());
        itemSchedule.put(OverviewViewModel.DAIRY_CAT_NAME, new LinkedList<>());
        itemSchedule.put(OverviewViewModel.GRAIN_CAT_NAME, new LinkedList<>());
        itemSchedule.put(OverviewViewModel.OTHER_CAT_NAME, new LinkedList<>());
    }

    /**
     * Syncs kitchen items with CartView model items
     */
    public void startUpFetch() {
        refreshList(kitchen.getCartItemList());
    }

    public void onSearch(String s) {
        ArrayList<CartItem> searchResult = Search.searchCartItem(kitchen.getCartItemList(), s);
        refreshList(searchResult);
    }

    public List<String> getGroupList() {
        return groupList;
    }


    /**
     * Creates the groupList
     */
    private void createGroupList() {
        groupList.add(OverviewViewModel.PROTEIN_CAT_NAME);
        groupList.add(OverviewViewModel.VEG_CAT_NAME);
        groupList.add(OverviewViewModel.FRUIT_CAT_NAME);
        groupList.add(OverviewViewModel.DAIRY_CAT_NAME);
        groupList.add(OverviewViewModel.GRAIN_CAT_NAME);
        groupList.add(OverviewViewModel.OTHER_CAT_NAME);
    }

    /**
     * Add a cart item to the current kitchen
     * @param cartFragment that addCartItem was called from
     * @param kitchen that the item is to be added to
     * @param foodName Name of the item to be added
     * @param type Type of the item to be added
     * @param expiresAfter Number of days that the item to be added will expire after
     * @param quantity of the item to be added
     * @param storageLoc of the item to be added
     * @param user that is adding the item
     * @throws InvalidQuantityException
     */
    public void addCartItem(ShoppingCartFragment cartFragment, Kitchen kitchen, String foodName, String type, int expiresAfter, int quantity, String storageLoc, User user) throws InvalidQuantityException {
        this.cartViewFragment = cartFragment;
        CartItem item = CartItemFactory.createCartItem(foodName, type, expiresAfter, user, quantity, storageLoc, new ArrayList<>());
        Log.i("DEBUG", "adding: " + item.toString());
        kitchen.addCartItem(item);
    }

    /**
     * Change the quantity of the item at tthe given group and child position
     * @param cartFragment that changeCartItemQuantity was called from
     * @param kitchen that the item belongs to
     * @param user that is changing the quantity
     * @param groupPosition of the item whose quantity is to be changed
     * @param childPosition of the item whose quantity is to be changed (within the group)
     * @param newQuantity Item quantity will be changed to the newQuantity
     * @throws InvalidQuantityException
     */
    public void changeCartItemQuantity(ShoppingCartFragment cartFragment, Kitchen kitchen, User user, int groupPosition, int childPosition, int newQuantity) throws InvalidQuantityException {
        this.cartViewFragment = cartFragment;

        CartItem item = getCartItem(user, groupPosition, childPosition);

        kitchen.changeCartItemQuantity(item, newQuantity);
    };

    /**
     * Remove the item at the given group and child position from the current kitchen
     * @param cartFragment that removeCartItem was called from
     * @param kitchen that the item we want to remove is in
     * @param user that is removing the item
     * @param groupPosition of the item to be removed
     * @param childPosition of the item to be removed, within the given group
     */
    public void removeCartItem(ShoppingCartFragment cartFragment, Kitchen kitchen, User user, int groupPosition, int childPosition) {
        this.cartViewFragment = cartFragment;
        CartItem item = getCartItem(user, groupPosition, childPosition);
        kitchen.deleteCartItem(item);
    }

    /**
     * Delete all the checked cart items from the kitchen and add their corresponding kitchen item to the kitchen
     * @param kitchen that we want to delete cart items from and add items to
     * @param user that is buying the car items
     * @param checkedChildren The positions of all cart items whose checkboxes are currently checked
     * @throws InvalidQuantityException
     */
    public void buyCartItems(Kitchen kitchen, User user, Set<CartExpandableListAdapter.Position> checkedChildren) throws InvalidQuantityException {
        Set<CartItem> checkedItems = new HashSet<>();
        for (CartExpandableListAdapter.Position pos : checkedChildren) {
            Log.i("DEBUG", pos.toString());
        }
        for (CartExpandableListAdapter.Position pos : checkedChildren) {
            checkedItems.add(getCartItem(user, pos.getGroupPosition(), pos.getChildPosition()));
        }
        kitchen.buyCartItems(checkedItems, user);
    }

    /**
     * Update the CartViewModel with the new cart items
     * @param cartItemArrayList New cart items
     */
    public void refreshList(final ArrayList<CartItem> cartItemArrayList) {
        createCollection();

        if(cartItemArrayList == null) {
            return;
        }


        for (CartItem item : cartItemArrayList) {
            int groupIndex = getGroupIndex(item);

            itemCollection.get(groupList.get(groupIndex)).add(item.getName());
            itemExpiryDays.get(groupList.get(groupIndex)).add(item.getExpiryDays() + "");
            itemQuantity.get(groupList.get(groupIndex)).add(String.valueOf(item.getQuantity()));
            itemLocation.get(groupList.get(groupIndex)).add(String.valueOf(item.getStorageLocation()));

            List<String> scheduleList = new ArrayList<>();
            for (CartSchedule schedule : item.getSchedule()) {
                scheduleList.add(schedule.toString());
            }
            itemSchedule.get(groupList.get(groupIndex)).add(scheduleList);

        }

        cartViewFragment.notifyListViewChanges();
    }

    /**
     * Add a CartSchedule to the given cart item
     * @param fragment that addCartSchedule was called from
     * @param kitchen that holds the cart item we want to add a cart schedule to
     * @param user that is adding the cart schedule
     * @param groupPosition of the item that the cart schedule is being added to
     * @param childPosition of the item that the cart schedule is being added to (within the given group)
     * @param quantity How much of the cart item we want to have added in the schedule
     * @param scheduleDate The date of the next time that the schedule will cause a change in the cart item quantity
     * @param daysReoccurring The number of days between each time that the schedule adds quantity to the cart item
     *                        Value is zero if the schedule only adds quantity once
     */
    public void addCartSchedule(ShoppingCartFragment fragment,
                                Kitchen kitchen,
                                User user,
                                int groupPosition,
                                int childPosition,
                                int quantity,
                                Date scheduleDate,
                                int daysReoccurring) {

        this.cartViewFragment = fragment;

        CartSchedule schedule = new CartSchedule(scheduleDate, quantity, daysReoccurring, user);

        CartItem item = getCartItem(user, groupPosition, childPosition);


        kitchen.addCartSchedule(fragment, item, schedule);


    }

    /**
     * Remove a given schedule from a given cart item
     * @param kitchen that the specified cart item belongs to
     * @param user that is removing the schedule
     * @param groupPosition of the cart item
     * @param childPosition of the cart item (within the group)
     * @param scheduleIndex Index of the schedule in the list of schedules held by the cart item
     */
    public void removeCartSchedule(Kitchen kitchen,
                                   User user,
                                   int groupPosition,
                                   int childPosition,
                                   int scheduleIndex) {
        CartItem item = getCartItem(user, groupPosition, childPosition);

        List<String> scheduleListS = itemSchedule.get(groupList.get(groupPosition)).get(childPosition);
        String scheduleToBeRemovedS = scheduleListS.get(scheduleIndex);
        CartSchedule schedule = new CartSchedule(scheduleToBeRemovedS);

        kitchen.removeCartSchedule(kitchen.getKitchenID(), item, schedule);
    }

    /**
     *
     * @param user that is getting the cart item
     * @param groupPosition of the cart item
     * @param childPosition of the cart item (within the group)
     * @return the cart item at the given group and child position
     */
    public CartItem getCartItem(User user, int groupPosition, int childPosition) {
        for (String s : itemCollection.get(groupList.get(groupPosition))) {

        }
        String foodName = itemCollection.get(groupList.get(groupPosition)).get(childPosition);
        String type = groupList.get(groupPosition).split("/")[0];;
        int expiresAfter = Integer.parseInt(itemExpiryDays.get(groupList.get(groupPosition)).get(childPosition));
        int quantity = Integer.parseInt(itemQuantity.get(groupList.get(groupPosition)).get(childPosition));
        String storageLoc = itemLocation.get(groupList.get(groupPosition)).get(childPosition);

        List<String> scheduleString = itemSchedule.get(groupList.get(groupPosition)).get(childPosition);
        List<CartSchedule> schedule = new ArrayList<>();
        for (String string : scheduleString) {
            schedule.add(new CartSchedule(string));
        }

        return CartItemFactory.createCartItem(foodName, type, expiresAfter, user, quantity, storageLoc, schedule);
    }


    /**
     *
     * @param groupPosition of the cart item
     * @param childPosition of the cart item (within the group)
     * @return true if the cart item at the specified group and child position does not have any cart schedules
     */
    public boolean isCartItemScheduleEmpty(int groupPosition, int childPosition) {
        return itemSchedule.get(groupList.get(groupPosition)).get(childPosition).isEmpty();
    }

    /**
     *
     * @param item
     * @return the index of a cart item category in the field groupList
     */
     int getGroupIndex(CartItem item) {
        int groupIndex = -1;
        if(item instanceof CartProtein)
            groupIndex = 0;
        else if(item instanceof CartVegetable)
            groupIndex = 1;
        else if(item instanceof CartFruit)
            groupIndex = 2;
        else if(item instanceof CartDairy)
            groupIndex = 3;
        else if(item instanceof CartGrain)
            groupIndex = 4;
        else if(item instanceof CartOtherItem)
            groupIndex = 5;

        if(groupIndex == -1)
            throw new IllegalArgumentException("Unavailable type of item");

        return groupIndex;
    }


    /**
     * Change the storage location of the cart item at the given group and child position
     * @param cartFragment that changeCartItemStorageLocation is being called from
     * @param kitchen that the cart item belongs to
     * @param user that is changing the storage location
     * @param storageLocation The storage location that we want to change the cart item's current storage location to
     * @param groupPosition of the cart item
     * @param childPosition of the cart item (within the group)
     * @throws InvalidQuantityException
     */
    public void changeCartItemStorageLocation(ShoppingCartFragment cartFragment, Kitchen kitchen, User user, String storageLocation, int groupPosition, int childPosition) throws InvalidQuantityException {
        this.cartViewFragment = cartFragment;

        CartItem item = getCartItem(user, groupPosition, childPosition);

        kitchen.changeCartItemStorageLocation(item, storageLocation);
    }

    /**
     *  Change the expiry days of the cart item at the given group and child position
     * @param cartFragment that changeCartItemExpiry is being called from
     * @param kitchen that the cart item belongs to
     * @param user that is changing the expiry days
     * @param groupPosition of the cart item
     * @param childPosition of the cart item (within the group)
     * @param expiryDays The new integer we want to change the cart item's expiry days to
     * @throws InvalidQuantityException
     */
    public void changeCartItemExpiry(ShoppingCartFragment cartFragment, Kitchen kitchen, User user, int groupPosition, int childPosition, int expiryDays) throws InvalidQuantityException {
        this.cartViewFragment = cartFragment;

        CartItem item = getCartItem(user, groupPosition, childPosition);

        kitchen.changeCartItemExpiry(item, expiryDays);
    }

}
