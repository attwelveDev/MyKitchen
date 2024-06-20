package com.aaronnguyen.mykitchen.model.kitchen;

import java.util.List;

public interface KitchenMemberListener {
    /**
     * Called when the ownerID is fetched.
     */
    void onOwnerIDUpdateListener(String ownerID);

    /**
     * This updates the active member list (members who have access to the kitchen)
     * @param userID the list of new active user IDs
     */
    void onKitchenActiveMembersUpdateListener(List<String> userID);

    /**
     * This updates the pending member list (usually use when a user is either accepted or rejected to a kitchen)
     * @param userID the list of new pending user IDs
     */
    void onKitchenPendingMembersUpdateListener(List<String> userID);

    /**
     * This updates the banned member list (usually called when a user is banned or unbanned in relative to a kitchen)
     * @param userID the list of new banned user IDs
     */
    void onKitchenBannedMembersUpdateListener(List<String> userID);
}
