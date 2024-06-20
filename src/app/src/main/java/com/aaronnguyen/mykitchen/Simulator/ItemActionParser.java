package com.aaronnguyen.mykitchen.Simulator;

import android.util.Log;

import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.model.chat.ChatMessage;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.notification.AppAddingNotification;
import com.aaronnguyen.mykitchen.model.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class will simulate the action given a list of items.
 * First, Sample User 1 (Dummy 1) will put the items in to the fridge periodically and send a message to the group chat
 * The second user, Sample User 2 (Dummy 2) will take items from the fridge periodically and leave a message in the group chat
 */
public class ItemActionParser implements DataStreamActionParser {

    private static List<Item> pushedItems = new ArrayList<>();
    private static synchronized List<Item> getPushedItems() {
        return pushedItems;
    }
    private static synchronized void pushItem(Item item) {
        pushedItems.add(item);
    }
    private final Kitchen kitchen;
    private Item item;

    public ItemActionParser(Item item, Kitchen kitchen) {
        this.item = item;
        this.kitchen = kitchen;
    }

    /**
     * This parse function will actually do a bunch of things
     */
    @Override
    public void parse() throws InvalidQuantityException {
        Log.i(ActionSimulator.SIMULATION_CHANNEL, "Try to put " + item.toString());
        tryAddItem(item);
        ChatMessage putMessage = new ChatMessage(
                "Dummy 1",
                new Date(),
                "I have put a " + item.toString() + " in " + item.getStorageLocation(),
                "Sample User 1"
        );
        trySendMessage(putMessage);

        Thread task = new Thread(() -> {
            try {
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                Log.e("DEBUG","Unable to finish use action");
            }

            Log.i(ActionSimulator.SIMULATION_CHANNEL, "Now use / take the item");
            boolean isRemove = item.hashCode() % 2 == 0;     // This just gives some randomness to the action
            Log.i(ActionSimulator.SIMULATION_CHANNEL, getPushedItems().toString());
            if (isRemove) {
                Item removeItem;
                removeItem = getPushedItems().get(0);
                Log.i(ActionSimulator.SIMULATION_CHANNEL, "Try remove " + removeItem.toJSONObject());
                tryRemoveItem(removeItem);

                ChatMessage useMessage = new ChatMessage(
                        "Dummy 2",
                        new Date(),
                        "I have tried to use " + removeItem + " in " + removeItem.getStorageLocation(),
                        "Sample User 2"
                );
                trySendMessage(useMessage);

                getPushedItems().remove(0);
            }
            else {
                Item useItem;
                useItem = getPushedItems().get(0);
                Log.i(ActionSimulator.SIMULATION_CHANNEL, "Try use " + useItem.toJSONObject());
                boolean result = tryUseItem(useItem, 1);
                if(result) {
                    if(getPushedItems().get(0).getQuantity() == 1) {
                        getPushedItems().remove(0);
                    } else {
                        getPushedItems().get(0).setQuantity(1);
                    }

                    ChatMessage takeMessage = new ChatMessage(
                            "Dummy 2",
                            new Date(),
                            "I have taken " + useItem + " in " + useItem.getStorageLocation(),
                            "Sample User 2"
                    );
                    trySendMessage(takeMessage);
                } else {
                    getPushedItems().remove(0);
                }
            }
        });

        task.start();
    }

    private void tryAddItem(Item item) throws InvalidQuantityException {
        pushItem(item);
        kitchen.addItem(item);
        kitchen.addNotification(new AppAddingNotification(ActionSimulator.DUMMY1.getUserName()
                +  " has add item " + item.getName(),1));
    }

    private boolean tryRemoveItem(Item item) {
        ArrayList<Item> itemsInKitchen = kitchen.itemList;
        for (Item itemInKitchen : itemsInKitchen) {
            if(itemInKitchen.getName().equals(item.getName())) {
                return kitchen.useItem(itemInKitchen, itemInKitchen.getQuantity(), ActionSimulator.DUMMY2);
            }
        }
        return false;
    }

    private boolean tryUseItem(Item item, int quantity) {
        ArrayList<Item> itemsInKitchen = kitchen.itemList;
        for (Item itemInKitchen : itemsInKitchen) {
            if(itemInKitchen.getName().equals(item.getName())) {
                return kitchen.useItem(itemInKitchen, quantity, ActionSimulator.DUMMY2);
            }
        }
        return false;
    }

    private void trySendMessage(ChatMessage chatMessage) {
        kitchen.sendMessageOnline(chatMessage);
    }
}
