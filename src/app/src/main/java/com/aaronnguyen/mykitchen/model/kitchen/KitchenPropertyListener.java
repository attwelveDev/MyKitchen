package com.aaronnguyen.mykitchen.model.kitchen;

/**
 * This interface listens to the changes happen to the meta-data of the kitchen
 */
public interface KitchenPropertyListener {
    /**
     * Called when a new Kitchen name is fetched.
     *
     * @param kitchenName the new Kitchen name.
     */
    void onKitchenNameUpdateListener(String kitchenName);

    /**
     * Called when a Kitchen is deleted.
     *
     * @param kitchenID the ID of the deleted Kitchen.
     */
    void onKitchenDeleteListener(String kitchenID);

    void onKitchenDeleteFailureListener(Exception e);
}
