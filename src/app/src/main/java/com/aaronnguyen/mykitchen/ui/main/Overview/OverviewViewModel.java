package com.aaronnguyen.mykitchen.ui.main.Overview;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.DAO.FetchListener;
import com.aaronnguyen.mykitchen.DAO.KitchenFirebaseDAO;
import com.aaronnguyen.mykitchen.DAO.UserDaoFirebase;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Dairy;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Fruit;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Grain;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemFactory;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.OtherItem;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Protein;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Vegetable;
import com.aaronnguyen.mykitchen.model.Items.Search;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.notification.AppAddingNotification;
import com.aaronnguyen.mykitchen.model.notification.AppExpiryNotification;
import com.aaronnguyen.mykitchen.model.notification.NotificationFactory;
import com.aaronnguyen.mykitchen.model.user.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OverviewViewModel extends ViewModel {
    public static final String PROTEIN_CAT_NAME = "Protein/Meat";
    public static final String VEG_CAT_NAME = "Vegetable";
    public static final String FRUIT_CAT_NAME = "Fruit";
    public static final String DAIRY_CAT_NAME = "Dairy";
    public static final String GRAIN_CAT_NAME = "Grain";
    public static final String OTHER_CAT_NAME = "Other Items";
    public static final String DATE_DISPLAY_FORMAT = "dd/MM/yyyy";

    private final MutableLiveData<OverviewUIState> uiState;

    private Kitchen kitchen;
    private final User user;

    public OverviewViewModel(Kitchen kitchen, User user) {
        this.kitchen = kitchen;
        this.user = user;

        UserDaoFirebase.getInstance().syncUser(this.user.getUid(), new FetchListener() {
            @Override
            public <T> void onFetchSuccess(T data) {
                if(data instanceof User userData) {
                    user.setEmail(userData.getEmail());
                    user.setUserName(userData.getUserName());
                }
            }

            @Override
            public void onFetchFailure(Exception exception) {
                // This should be ignored
            }
        });

        uiState = new MutableLiveData<>(new OverviewUIState(
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>()
        ));

        uiState.getValue().createGroupList();
    }

    public void startUpFetch() {
        refreshList(kitchen.getItemList());
    }

    /**
     * This is the default API that should be used when adding an item
     * @param kitchen the corresponding kitchen
     * @param name the Name of the item
     * @param type the type of the item
     * @param boughtDate the boughtDate
     * @param expiryDate the expiryDate
     * @param currentUser the user for current session
     * @param quantity number of items being added
     */
    public void addItem(Context context, Kitchen kitchen, String name, String type, Date boughtDate, Date expiryDate, User currentUser, int quantity, String storageLocation) throws InvalidQuantityException {
        Item item = ItemFactory.createItem(name, type, expiryDate, boughtDate, currentUser, quantity, storageLocation, new ArrayList<>());
        kitchen.addItem(item);
        kitchen.addNotification(new AppAddingNotification(currentUser.getUserName()
                +  " has add item " + item.getName(),1));
        kitchen.allKindsNotification();
    }

    public void useItem(Kitchen kitchen, User currentUser, int groupIndex, int childIndex, int useQuantity) throws ParseException {
        Item item = getItem(user, groupIndex, childIndex);
        kitchen.useItem(item, useQuantity, user);
    }

    /**
     * Edit a food item given the new attributes. Some attributes may be the same as the old item.
     *
     * @param kitchen the kitchen with the item.
     * @param oldItem the item to edit.
     * @param name the name of the item.
     * @param type the food type of the item.
     * @param boughtDate the bought date of the item.
     * @param expiryDate the expiry date of the item.
     * @param quantity the quantity of the item.
     * @param storageLocation the storage location of the item.
     * @author u7333216 Aaron Nguyen, u7515796 ChengboYan
     */
    public void editItem(Kitchen kitchen, Item oldItem, String name, String type, Date boughtDate, Date expiryDate, int quantity, String storageLocation) {
        Item newItem = ItemFactory.createItem(name, type, expiryDate, boughtDate, user, quantity, storageLocation, new ArrayList<>());
        String kitchenId = kitchen.getKitchenID();
        KitchenFirebaseDAO.getInstance().editItem(kitchenId, oldItem, newItem);
        if(oldItem.isCloseToExpiry()){
            if (oldItem.DaysBeforeExpiry() > 0) {
                String days = " day";
                int beforeExpiryDay = oldItem.DaysBeforeExpiry();
                if(beforeExpiryDay == 1) {
                    days = "1" + days;
                }
                else {
                    days =  beforeExpiryDay + " days";
                }
                AppExpiryNotification appExpiryNotification = NotificationFactory.createNotification(oldItem.getName() + " " +
                        "will expire in " + days, 2);
                KitchenFirebaseDAO.getInstance().removeNotification(kitchenId, appExpiryNotification);
            } else if (oldItem.DaysBeforeExpiry() == 0) {
                AppExpiryNotification appExpiryNotification = NotificationFactory.createNotification
                        (oldItem.getName() + " will expire today", 2);
                KitchenFirebaseDAO.getInstance().removeNotification(kitchenId, appExpiryNotification);
            }
        }
        if (oldItem.OutOfExpiry()) {
            AppExpiryNotification appExpiryNotification =
                    NotificationFactory.createNotification(oldItem.getName() + " has expired ", 3);
            KitchenFirebaseDAO.getInstance().removeNotification(kitchenId, appExpiryNotification);
        }
        kitchen.editTextNotification(oldItem,newItem);

    }

    public void scheduleItemUse(KitchenOverviewFragment overviewFragment, Kitchen kitchen, User currentUser, int groupIndex, int childIndex, int scheduledUseQuantity, Date scheduledUseDate) {
        Item item = getItem(currentUser, groupIndex, childIndex);
        ItemUseSchedule newSchedule = new ItemUseSchedule(scheduledUseDate, scheduledUseQuantity, currentUser);

        boolean result = kitchen.scheduleItemUse(overviewFragment, kitchen, item, newSchedule);
    }

    public void removeScheduleItemUse(Kitchen kitchen, User currentUser, int groupIndex, int childIndex, int which) {
        Item item = getItem(currentUser, groupIndex, childIndex);

        List<String> groupList = getUiState().getValue().getGroupList();
        Map<String, List<List<String>>> itemSchedule = getUiState().getValue().getItemSchedule();


        List<String> scheduleListS = itemSchedule.get(groupList.get(groupIndex)).get(childIndex);

        String scheduleToBeRemovedS = scheduleListS.get(which);
        ItemUseSchedule schedule = new ItemUseSchedule(scheduleToBeRemovedS);

        kitchen.removeScheduleItemUse(item, schedule);
    }
    /**
     * This function will re-create the whole displaying list according to the new itemArrayList
     * Therefore, the usage of this function should be kept to minimum as it might create a performance hit.
     * @param itemArrayList the new itemArrayList
     */
    public void refreshList(final ArrayList<Item> itemArrayList) {
        uiState.getValue().refreshList(itemArrayList);
        notifyUiStateObserver();
    }

    public Item getItem(User currentUser, int groupIndex, int childIndex) {
        List<String> groupList = getUiState().getValue().getGroupList();
        Map<String, List<String>> itemCollection = getUiState().getValue().getItemCollection();
        Map<String, List<String>> itemExpiryDate = getUiState().getValue().getItemExpiryDate();
        Map<String, List<String>> itemBoughtDate = getUiState().getValue().getItemBoughtDate();
        Map<String, List<String>> itemQuantity = getUiState().getValue().getItemQuantity();
        Map<String, List<String>> itemLocation = getUiState().getValue().getItemLocation();
        Map<String, List<List<String>>> itemSchedule = getUiState().getValue().getItemSchedule();

        String name = itemCollection.get(groupList.get(groupIndex)).get(childIndex);
        String type = groupList.get(groupIndex).split("/")[0];
        String expiryDateS = itemExpiryDate.get(groupList.get(groupIndex)).get(childIndex);
        String boughtDateS = itemBoughtDate.get(groupList.get(groupIndex)).get(childIndex);
        String originalQuantityS = itemQuantity.get(groupList.get(groupIndex)).get(childIndex);
        String location = itemLocation.get(groupList.get(groupIndex)).get(childIndex);
        List<String> scheduleListS = itemSchedule.get(groupList.get(groupIndex)).get(childIndex);

        DateFormat df = new SimpleDateFormat(DATE_DISPLAY_FORMAT);

        int originalQuantity = Integer.parseInt(originalQuantityS);


        Date expiryDate;
        Date boughtDate;
        List<ItemUseSchedule> scheduleList = new ArrayList<>();
        for (String schedule : scheduleListS) {
            scheduleList.add(new ItemUseSchedule(schedule));
        }

        try {
            expiryDate = df.parse(expiryDateS);
            boughtDate = df.parse(boughtDateS);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return ItemFactory.createItem(name, type, expiryDate, boughtDate, currentUser, originalQuantity, location, scheduleList);
    }

    public void onSearch(String s) {
        ArrayList<Item> searchResult = Search.search(kitchen.getItemList(), s);
        refreshList(searchResult);
    }

    public User getUser() {
        return user;
    }

    private int getGroupIndex(Item item) {
        int groupIndex = -1;
        if(item instanceof Protein)
            groupIndex = 0;
        else if(item instanceof Vegetable)
            groupIndex = 1;
        else if(item instanceof Fruit)
            groupIndex = 2;
        else if(item instanceof Dairy)
            groupIndex = 3;
        else if(item instanceof Grain)
            groupIndex = 4;
        else if(item instanceof OtherItem)
            groupIndex = 5;

        if(groupIndex == -1)
            throw new IllegalArgumentException("Unavailable type of item");

        return groupIndex;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    class OverviewUIState {
        private final List<String> groupList;
        private final Map<String, List<String>> itemCollection;
        private final Map<String, List<String>> itemExpiryDate;
        private final Map<String, List<String>> itemBoughtDate;
        private final Map<String, List<String>> itemQuantity;
        private final Map<String, List<String>> itemLocation;
        private final Map<String, List<List<String>>> itemSchedule;

        public OverviewUIState(List<String> groupList,
                               Map<String, List<String>> itemCollection,
                               Map<String, List<String>> itemExpiryDate,
                               Map<String, List<String>> itemBoughtDate,
                               Map<String, List<String>> itemQuantity,
                               Map<String, List<String>> itemLocation,
                               Map<String, List<List<String>>> itemSchedule) {
            this.groupList = groupList;
            this.itemCollection = itemCollection;
            this.itemExpiryDate = itemExpiryDate;
            this.itemBoughtDate = itemBoughtDate;
            this.itemQuantity = itemQuantity;
            this.itemLocation = itemLocation;
            this.itemSchedule = itemSchedule;

            createGroupList();
            createCollection();
        }

        public void refreshList(ArrayList<Item> itemArrayList) {
            createCollection();

            if(itemArrayList == null)
                return;

            for (Item item : itemArrayList) {
                int groupIndex = getGroupIndex(item);
                DateFormat df = new SimpleDateFormat(DATE_DISPLAY_FORMAT);

                itemCollection.get(groupList.get(groupIndex)).add(item.getName());
                itemExpiryDate.get(groupList.get(groupIndex)).add(df.format(item.getExpiryDate()));
                itemBoughtDate.get(groupList.get(groupIndex)).add(df.format(item.getBoughtDate()));
                itemQuantity.get(groupList.get(groupIndex)).add(String.valueOf(item.getQuantity()));
                itemLocation.get(groupList.get(groupIndex)).add(String.valueOf(item.getStorageLocation()));

                List<String> scheduleList = new ArrayList<>();
                for (ItemUseSchedule schedule : item.getSchedule()) {
                    scheduleList.add(schedule.toString());
                }
                itemSchedule.get(groupList.get(groupIndex)).add(scheduleList);
            }

            Log.i("DEBUG", "Refresh List Called and Performed");
        }

        private void createGroupList() {
            groupList.add(PROTEIN_CAT_NAME);
            groupList.add(VEG_CAT_NAME);
            groupList.add(FRUIT_CAT_NAME);
            groupList.add(DAIRY_CAT_NAME);
            groupList.add(GRAIN_CAT_NAME);
            groupList.add(OTHER_CAT_NAME);
        }

        /**
         * This create the structure of different collections
         */
        private void createCollection() {
            itemCollection.put(OverviewViewModel.PROTEIN_CAT_NAME, new LinkedList<>());
            itemCollection.put(OverviewViewModel.VEG_CAT_NAME, new LinkedList<>());
            itemCollection.put(OverviewViewModel.FRUIT_CAT_NAME, new LinkedList<>());
            itemCollection.put(OverviewViewModel.DAIRY_CAT_NAME, new LinkedList<>());
            itemCollection.put(OverviewViewModel.GRAIN_CAT_NAME, new LinkedList<>());
            itemCollection.put(OverviewViewModel.OTHER_CAT_NAME, new LinkedList<>());

            itemExpiryDate.put(OverviewViewModel.PROTEIN_CAT_NAME, new LinkedList<>());
            itemExpiryDate.put(OverviewViewModel.VEG_CAT_NAME, new LinkedList<>());
            itemExpiryDate.put(OverviewViewModel.FRUIT_CAT_NAME, new LinkedList<>());
            itemExpiryDate.put(OverviewViewModel.DAIRY_CAT_NAME, new LinkedList<>());
            itemExpiryDate.put(OverviewViewModel.GRAIN_CAT_NAME, new LinkedList<>());
            itemExpiryDate.put(OverviewViewModel.OTHER_CAT_NAME, new LinkedList<>());

            itemBoughtDate.put(OverviewViewModel.PROTEIN_CAT_NAME, new LinkedList<>());
            itemBoughtDate.put(OverviewViewModel.VEG_CAT_NAME, new LinkedList<>());
            itemBoughtDate.put(OverviewViewModel.FRUIT_CAT_NAME, new LinkedList<>());
            itemBoughtDate.put(OverviewViewModel.DAIRY_CAT_NAME, new LinkedList<>());
            itemBoughtDate.put(OverviewViewModel.GRAIN_CAT_NAME, new LinkedList<>());
            itemBoughtDate.put(OverviewViewModel.OTHER_CAT_NAME, new LinkedList<>());

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

        public List<String> getGroupList() {
            return groupList;
        }

        public Map<String, List<String>> getItemCollection() {
            return itemCollection;
        }

        public Map<String, List<String>> getItemExpiryDate() {
            return itemExpiryDate;
        }

        public Map<String, List<String>> getItemBoughtDate() {
            return itemBoughtDate;
        }

        public Map<String, List<String>> getItemQuantity() {
            return itemQuantity;
        }

        public Map<String, List<String>> getItemLocation() {
            return itemLocation;
        }

        public Map<String, List<List<String>>> getItemSchedule() {
            return itemSchedule;
        }
    }

    private void notifyUiStateObserver() {
        uiState.setValue(uiState.getValue());
    }

    MutableLiveData<OverviewUIState> getUiState() {
        return uiState;
    }
}