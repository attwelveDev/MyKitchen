package com.aaronnguyen.mykitchen.ui.main.History;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing the history of item usage in a kitchen.
 * @author u7515796 ChengboYan
 */
public class HistoryViewModel extends ViewModel {
    private final MutableLiveData<List<ItemUsage>> itemUsageData;
    private final Kitchen kitchen;

    public HistoryViewModel(Kitchen kitchen) {
        this.kitchen = kitchen;
        itemUsageData = new MutableLiveData<>(new ArrayList<>());
    }

    public void startUpFetch() {
        refreshList(kitchen.getItemUsageList());
    }

    public void refreshList(ArrayList<ItemUsage> itemUsages) {
        this.itemUsageData.setValue(itemUsages);
    }

    public Kitchen getKitchen() {
        return kitchen;
    }

    MutableLiveData<List<ItemUsage>> getItemUsageData() {
        return itemUsageData;
    }
}
