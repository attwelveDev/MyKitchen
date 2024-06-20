package com.aaronnguyen.mykitchen.ui.main.History;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aaronnguyen.mykitchen.databinding.ActivityHistoryBinding;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.KitchenFragmentsViewModelFactory;
import com.aaronnguyen.mykitchen.ui.main.KitchenHomeActivity;

import java.util.List;

/**
 * Fragment for displaying the history of item usage in a kitchen.
 * @author u7515796 ChengboYan
 */
public class HistoryFragment extends Fragment {
    private ItemUsageAdapter expandableListAdapterHistory;
    private HistoryViewModel historyViewModel;

    private ActivityHistoryBinding binding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Kitchen kitchen = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.KITCHEN_INTENT_TAG, Kitchen.class);
        User user = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.USER_INTENT_TAG, User.class);

        historyViewModel = new ViewModelProvider(
            this,
            new KitchenFragmentsViewModelFactory(
                    kitchen,
                    user,
                    KitchenFragmentsViewModelFactory.TargetViewModelCode.History
            )
        ).get(HistoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityHistoryBinding.inflate(inflater, container, false);

        expandableListAdapterHistory = new ItemUsageAdapter(
                getContext(),
                historyViewModel.getItemUsageData().getValue(),
                historyViewModel.getKitchen()
        );
        binding.KitchenHistoryListView.setAdapter(expandableListAdapterHistory);

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("DEBUG","Expand History");

        historyViewModel.getItemUsageData().observe(getViewLifecycleOwner(), uiObserver());
        historyViewModel.startUpFetch();

        binding.KitchenHistoryListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int lastExpandedPosition = -1;
            @Override
            public void onGroupExpand(int groupPosition) {
                if(lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                    binding.KitchenHistoryListView.collapseGroup(lastExpandedPosition);
                }

                lastExpandedPosition = groupPosition;
            }
        });


    }
    public HistoryViewModel getHistoryViewModel() {
        return historyViewModel;
    }

    public Observer<List<ItemUsage>> uiObserver() {
        return itemUsageList -> {
            if(itemUsageList.isEmpty()) {
                binding.noHistoryLbl.setVisibility(View.VISIBLE);
            } else {
                binding.noHistoryLbl.setVisibility(View.GONE);
                expandableListAdapterHistory.update(itemUsageList);
                expandableListAdapterHistory.notifyDataSetChanged();
            }
        };
    }
}
