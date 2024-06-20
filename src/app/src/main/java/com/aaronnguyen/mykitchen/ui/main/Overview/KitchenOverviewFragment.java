package com.aaronnguyen.mykitchen.ui.main.Overview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.ButtonRequiringEditText;
import com.aaronnguyen.mykitchen.ui.main.KitchenFragmentsViewModelFactory;
import com.aaronnguyen.mykitchen.ui.main.KitchenHomeActivity;
import com.aaronnguyen.mykitchen.ui.other.Search.SearchActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class KitchenOverviewFragment extends Fragment {
    private EditText searchEditText;
    private FloatingActionButton addItemBtn;
    private Button boughtOnBtn;
    private Button expiresOnBtn;
    private Button useOnBtn;
    private Button foodGroupBtn;
    private Button storageLocationBtn;

    static final private String BOUGHT_DATE_PICKER_TAG = "boughtDatePicker";
    static final private String EXPIRES_DATE_PICKER_TAG = "expiresDatePicker";
    static final private String USE_ON_DATE_TAG = "useOnDatePicker";
    private Date boughtDate;
    private Date expiryDate;
    private Date useDate;
    private Calendar scheduledUseDate = Calendar.getInstance();

    private PopupMenu categoryPopupMenu;
    private androidx.appcompat.app.AlertDialog addItemDialog;
    private androidx.appcompat.app.AlertDialog removeItemDialog;

    private enum ContextMenuItems {
        USE_ITEM(0),
        EDIT_ITEM(1),
        VIEW_SCHEDULES(2);

        final int id;

        ContextMenuItems(int id) {
            this.id = id;
        }
    }

    MaterialDatePicker<Long> datePicker;
    MaterialTimePicker timePicker;

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    OverviewViewModel pageViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Kitchen kitchen = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.KITCHEN_INTENT_TAG, Kitchen.class);
        User user = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.USER_INTENT_TAG, User.class);

        pageViewModel = new ViewModelProvider(
            this,
            new KitchenFragmentsViewModelFactory(
                kitchen,
                user,
                KitchenFragmentsViewModelFactory.TargetViewModelCode.Overview
            )
        ).get(OverviewViewModel.class);
        pageViewModel.getUiState().observe(this, uiStateObserver());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceStatus) {
        return inflater.inflate(R.layout.activity_kitchen_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchEditText = view.findViewById(R.id.search_edit_text);

        TextInputLayout searchEditTextContainer = view.findViewById(R.id.search_edit_text_container);
        searchEditTextContainer.setOnClickListener(v -> {
            Log.i("Fragment", "TextInputLayout clicked - Opening Search Activity");

            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: do something with search query 's'
                getPageViewModel().onSearch(s.toString().replaceAll("\\s+","").toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        expandableListView = view.findViewById(R.id.KitchenOverviewListView);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int lastExpandedPosition = -1;
            @Override
            public void onGroupExpand(int groupPosition) {
                if(lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                    expandableListView.collapseGroup(lastExpandedPosition);
                }

                lastExpandedPosition = groupPosition;
            }
        });

        registerForContextMenu(expandableListView);

        addItemBtn = view.findViewById(R.id.add_item_overview_btn);
        addItemBtn.setOnClickListener(v -> showAddOrEditItemAlertDialog(null));

        // Note that this will have to go behind all the set up of the UIs,
        // so that the view model has access to the expandable list
        pageViewModel.startUpFetch();
    }

    public OverviewViewModel getPageViewModel() {
        return pageViewModel;
    }

    public void notifyListViewChanges() {
        ((BaseExpandableListAdapter) expandableListAdapter).notifyDataSetChanged();
    }

    /**
     * Show the add or edit item alert dialog.
     * Users can enter name, quantity, bought and expiry dates, food group, and storage location.
     *
     * @param item an item to edit; if null, then add an item.
     * @author u733216 Aaron Nguyen (edits by Isaac, Ruixian)
     */
    private void showAddOrEditItemAlertDialog(Item item) {
        if (getActivity() == null) {
            return;
        }

        if (addItemDialog != null && addItemDialog.isShowing()) {
            return;
        }
        View addItemView = getLayoutInflater().inflate(R.layout.add_item_alert_dialog, null);
        TextView titleTextView = addItemView.findViewById(R.id.add_food_lbl);

        EditText foodNameEditText = addItemView.findViewById(R.id.food_name_edit_text);
        EditText quantityEditText = addItemView.findViewById(R.id.quantity_edit_text);
        CheckBox boughtTodayCheckBox = addItemView.findViewById(R.id.bought_today_checkbox);
        boughtOnBtn = addItemView.findViewById(R.id.bought_on_btn);
        expiresOnBtn = addItemView.findViewById(R.id.expires_on_btn);
        foodGroupBtn = addItemView.findViewById(R.id.food_gp_btn);
        storageLocationBtn = addItemView.findViewById(R.id.storage_loc_btn);

        Button expireRefBtn = addItemView.findViewById(R.id.expire_ref_btn);
        Log.i("ACT", "Opening Search Activity");
        expireRefBtn.setOnClickListener(v -> {
            Log.i("Fragment", "Opening Search Activity");

            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        });

        String posBtnText;

        if (item != null) {  // edit existing item
            titleTextView.setText(R.string.edit_food_lbl);
            posBtnText = getString(R.string.edit_btn);

            foodNameEditText.setText(item.getName());
            quantityEditText.setText(String.valueOf(item.getQuantity()));
            boughtOnBtn.setText(convertDateToString(item.getBoughtDate()));
            expiresOnBtn.setText(convertDateToString(item.getExpiryDate()));

            String storageLocation = item.getStorageLocation();
            storageLocation = storageLocation.substring(0, 1).toUpperCase() + storageLocation.substring(1);  // capitalise first letter

            storageLocationBtn.setText(storageLocation);

            if (item.getTypeString().equals("others")) {
                foodGroupBtn.setText(R.string.other_food_category);
            } else {
                foodGroupBtn.setText(item.getTypeString());
            }
        } else {  // add new item
            titleTextView.setText(R.string.add_food_lbl);
            posBtnText = getString(R.string.add_btn);
        }

        MaterialAlertDialogBuilder addItemDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
        addItemDialogBuilder.setView(addItemView).setPositiveButton(posBtnText, (dialog, which) -> {}).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        addItemDialog = addItemDialogBuilder.create();
        addItemDialog.setOnShowListener(dialog -> {
            Button positiveButton = addItemDialog.getButton(AlertDialog.BUTTON_POSITIVE);  // TODO: disable button when at least one field is empty
            positiveButton.setOnClickListener(v -> {  // To prevent unintended dismiss (i.e. when invalid input)
                String foodNameText = foodNameEditText.getText().toString();
                String quantityText = quantityEditText.getText().toString();

                String foodGroupText = foodGroupBtn.getText().toString();
                String storageLocText = storageLocationBtn.getText().toString();

                if (foodNameText.isEmpty() ||
                        quantityText.isEmpty() ||
                        boughtOnBtn.getText().toString().isEmpty() ||
                        expiresOnBtn.getText().toString().isEmpty() ||
                        foodGroupText.isEmpty() ||
                        storageLocText.isEmpty()) {
                    Toast.makeText(requireActivity().getApplicationContext(), R.string.fill_in_all_fields_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                // Ensure quantityText is a number
                for (Character character : quantityText.toCharArray()) {
                    if (!Character.isDigit(character)) {
                        Toast.makeText(requireActivity().getApplicationContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                int quantity = Integer.parseInt(quantityText);
                KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();
                if (quantity <= 0) {
                    Toast.makeText(requireActivity().getApplicationContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                } else if (item == null) {  // add new item
                    try {
                        pageViewModel.addItem(
                                getContext(),
                                kitchenHomeActivity.getKitchen(),
                                foodNameText,
                                foodGroupText,
                                boughtDate,
                                expiryDate,
                                kitchenHomeActivity.getCurrentUser(),
                                quantity,
                                storageLocText
                        );
                    } catch (InvalidQuantityException e) {
                        throw new RuntimeException(e);
                    }

                    dialog.dismiss();
                } else {  // edit existing item
                    // If user has not changed dates, use existing dates from edited item
                    Date boughtDate = this.boughtDate == null ? item.getBoughtDate() : this.boughtDate;
                    Date expiryDate = this.expiryDate == null ? item.getExpiryDate() : this.expiryDate;

                    pageViewModel.editItem(
                            kitchenHomeActivity.getKitchen(),
                            item,
                            foodNameText,
                            foodGroupText,
                            boughtDate,
                            expiryDate,
                            quantity,
                            storageLocText
                    );

                    dialog.dismiss();
                }
            });
        });

        boughtTodayCheckBox.setOnClickListener(v1 -> {
            if (boughtTodayCheckBox.isChecked()) {
                Date currentDate = new Date();
                String today = convertDateToString(currentDate);

                boughtOnBtn.setEnabled(false);
                boughtOnBtn.setText(today);
                boughtDate = currentDate;
            } else {
                boughtOnBtn.setEnabled(true);
            }
        });

        boughtOnBtn.setOnClickListener(v1 -> showDatePickerDialog(BOUGHT_DATE_PICKER_TAG));

        expiresOnBtn.setOnClickListener(v1 -> showDatePickerDialog(EXPIRES_DATE_PICKER_TAG));

        foodGroupBtn.setOnClickListener(v1 -> showCategoryPopupMenu(foodGroupBtn));
        storageLocationBtn.setOnClickListener(v1 -> showCategoryPopupMenu(storageLocationBtn));

        addItemDialog.show();
    }

    private void showRemoveItemDialog(int groupPosition, int childPosition) {
        if (removeItemDialog != null && removeItemDialog.isShowing()) {
            return;
        }
        View removeItemView = getLayoutInflater().inflate(R.layout.remove_item_dialog, null);

        EditText quantityEditText = removeItemView.findViewById(R.id.quantity_edit_text);
        CheckBox useLaterCheckbox = removeItemView.findViewById(R.id.use_later_checkbox);
        useOnBtn = removeItemView.findViewById(R.id.use_on_btn);
        useOnBtn.setText(calendarToBtnString(Calendar.getInstance()));
        //EditText daysReoccurringEditText = removeItemView.findViewById(R.id.days_reoccurring_edit_text);

        useOnBtn.setOnClickListener(v1 -> {
            showDatePickerDialog(USE_ON_DATE_TAG);
        });

        useLaterCheckbox.setOnClickListener(v1 -> {
            if (!useLaterCheckbox.isChecked()) {
                Date currentDate = new Date();
                String today = convertDateToString(currentDate);

                useOnBtn.setEnabled(false);
                useDate = currentDate;
                //daysReoccurringEditText.setEnabled(false);
            } else {
                useOnBtn.setEnabled(true);
                //daysReoccurringEditText.setEnabled(true);
            }
        });

        MaterialAlertDialogBuilder removeItemDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
        removeItemDialogBuilder.setView(removeItemView).setPositiveButton(R.string.rm_btn, (dialog, which) -> {}).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        removeItemDialog = removeItemDialogBuilder.create();
        removeItemDialog.setOnShowListener(dialog -> {
            Button positiveButton = removeItemDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String quantityS = quantityEditText.getText().toString();

                if(!quantityS.matches("[0-9]+$")) {
                    Toast.makeText(getContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                int quantity = Integer.parseInt(quantityS);

                if(quantity <= 0) {
                    Toast.makeText(getContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();
                if (!useLaterCheckbox.isChecked()) {
                    try {
                        pageViewModel.useItem(
                                kitchenHomeActivity.getKitchen(),
                                kitchenHomeActivity.getCurrentUser(),
                                groupPosition,
                                childPosition,
                                quantity);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    pageViewModel.scheduleItemUse(
                            this,
                            kitchenHomeActivity.getKitchen(),
                            kitchenHomeActivity.getCurrentUser(),
                            groupPosition,
                            childPosition,
                            quantity,
                            scheduledUseDate.getTime()
                    );
                }
                dialog.dismiss();
            });

            // Initially should be disabled
            Date currentDate = new Date();
            String today = calendarToBtnString(Calendar.getInstance());

            useOnBtn.setEnabled(false);
            useOnBtn.setText(today);
            useDate = currentDate;

            ButtonRequiringEditText.attachEditTextsToButton(positiveButton, new EditText[]{quantityEditText});
        });
        removeItemDialog.show();
    }


    private void showScheduleItemUseDialog(int groupPosition, int childPosition) {
        View scheduleItemUseView = getLayoutInflater().inflate(R.layout.item_schedule_use_alert_dialog, null);
        TextView noScheduleTextView = scheduleItemUseView.findViewById(R.id.no_schedules_lbl);

        ListAdapter scheduleListAdapter = new ScheduleItemUseListAdapter(getContext(), pageViewModel, groupPosition, childPosition);

        if (scheduleListAdapter.getCount() > 0) {
            noScheduleTextView.setVisibility(View.GONE);
        } else {
            noScheduleTextView.setVisibility(View.VISIBLE);
        }

        MaterialAlertDialogBuilder scheduleItemUseDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
        scheduleItemUseDialogBuilder.setView(scheduleItemUseView).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();

        //Reference: https://developer.android.com/develop/ui/views/components/dialogs#AddAList
        scheduleItemUseDialogBuilder.setTitle("Schedules")
                .setAdapter(scheduleListAdapter, (dialog, which) -> {
                    MaterialAlertDialogBuilder confirmRemoveBuilder = new MaterialAlertDialogBuilder(getActivity());
                    confirmRemoveBuilder.setTitle("Are you sure you want to remove this schedule?");
                    confirmRemoveBuilder.setNegativeButton(R.string.cancel, (d, w) -> d.cancel());
                    confirmRemoveBuilder.setPositiveButton(R.string.rm_btn, (d, w) -> {

                        pageViewModel.removeScheduleItemUse(
                                kitchenHomeActivity.getKitchen(),
                                kitchenHomeActivity.getCurrentUser(),
                                groupPosition,
                                childPosition,
                                which);

                    });
                    androidx.appcompat.app.AlertDialog confirmRemoveDialog = confirmRemoveBuilder.create();
                    confirmRemoveDialog.show();
                });

        androidx.appcompat.app.AlertDialog scheduleItemUseDialog = scheduleItemUseDialogBuilder.create();
        scheduleItemUseDialog.show();
    }
    /**
     * Show a date picker dialog depending on the date the user wants to select.
     *
     * @param tag the tag indicating which date the user wants to select.
     */
    private void showDatePickerDialog(String tag) {
        if (datePicker != null && datePicker.isVisible()) {
            return;
        }
        MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();

        String titleText;
        switch (tag) {
            case BOUGHT_DATE_PICKER_TAG  -> titleText = getString(R.string.select_bought_date_title);
            case EXPIRES_DATE_PICKER_TAG -> titleText = getString(R.string.select_expiry_date_title);
            case USE_ON_DATE_TAG         -> titleText = getString(R.string.select_use_date_title);
            default                      -> titleText = getString(R.string.select_date_default_title);
        }

        datePickerBuilder.setTitleText(titleText);
        datePickerBuilder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        datePicker = datePickerBuilder.build();
        datePicker.addOnPositiveButtonClickListener(aLong -> setButtonWithDate(tag, new Date(aLong)));
        if (tag.equals(USE_ON_DATE_TAG)) {
            datePicker.addOnPositiveButtonClickListener(a -> showTimePickerDialog());
        }
        datePicker.show(requireActivity().getSupportFragmentManager(), tag);
    }

    /**
     * Show a alert dialog that allows user to select a 24h time
     * @author u7648367 Ruixian Wu
     */
    private void showTimePickerDialog() {
        if (timePicker != null && timePicker.isVisible()) {
            return;
        }
        MaterialTimePicker.Builder timePickerBuilder = new MaterialTimePicker.Builder();
        timePickerBuilder.setTitleText("Select time");

        timePicker = timePickerBuilder.build();
        timePicker.addOnPositiveButtonClickListener(i -> {
            scheduledUseDate.set(Calendar.HOUR_OF_DAY,timePicker.getHour());
            scheduledUseDate.set(Calendar.MINUTE,timePicker.getMinute());
            scheduledUseDate.set(Calendar.SECOND,0);
            scheduledUseDate.set(Calendar.MILLISECOND,0);
            useOnBtn.setText(calendarToBtnString(scheduledUseDate));
        });
        timePicker.show(requireActivity().getSupportFragmentManager(),"");
    }
    /**
     * With the corresponding tag, set the button text with the given Date.
     *
     * @param tag tag of the date picker.
     * @param date the date selected.
     */
    public void setButtonWithDate(String tag, Date date) {
        String dateText = convertDateToString(date);

        if (tag.equals(BOUGHT_DATE_PICKER_TAG)) {
            boughtOnBtn.setText(dateText);
            boughtDate = date;
        } else if (tag.equals(EXPIRES_DATE_PICKER_TAG)) {
            expiresOnBtn.setText(dateText);
            expiryDate = date;
        }
        else if (tag.equals(USE_ON_DATE_TAG)) {
            int hour = scheduledUseDate.get(Calendar.HOUR_OF_DAY);
            int minute = scheduledUseDate.get(Calendar.MINUTE);
            scheduledUseDate.setTime(date);
            scheduledUseDate.set(Calendar.HOUR_OF_DAY, hour);
            scheduledUseDate.set(Calendar.MINUTE, minute);
            useOnBtn.setText(calendarToBtnString(scheduledUseDate));
        }
    }

    /**
     * Convert a given Date to a String.
     *
     * @param date a Date to convert to string.
     * @return the Date as a String.
     */
    private String convertDateToString(Date date) {
        return DateFormat.getDateInstance().format(date);
    }

    /**
     * Show a popup menu depending on which 'category' button is clicked.
     * It is either a food group or storage location menu.
     *
     * @param button the button to anchor the popup menu to.
     */
    private void showCategoryPopupMenu(Button button) {
        String[] menuItems;

        if (button.equals(foodGroupBtn)) {
            menuItems = new String[]{"Dairy", "Fruit", "Grain", "Protein", "Vegetable", "Other"};
        } else if (button.equals(storageLocationBtn)) {
            menuItems = new String[]{"Freezer", "Fridge", "Pantry", "Other"};
        } else {
            return;
        }

        // Set up a popup menu that appears next to the clicked button
        categoryPopupMenu = new PopupMenu(getActivity(), button);

        for (int i = 0; i < menuItems.length; i++) {
            categoryPopupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, menuItems[i]);
        }

        categoryPopupMenu.setOnMenuItemClickListener(item -> {
            int itemIndex = item.getItemId();
            if (itemIndex >= 0 && itemIndex < menuItems.length) {
                button.setText(menuItems[itemIndex]);
                return true;
            } else {
                return false;
            }
        });

        categoryPopupMenu.show();
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        ExpandableListView.ExpandableListContextMenuInfo listMenuInfo = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        if (listMenuInfo == null) {
            return;
        }

        if (ExpandableListView.getPackedPositionType(listMenuInfo.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            super.onCreateContextMenu(menu, v, menuInfo);
            menu.add(ContextMenu.NONE, ContextMenuItems.USE_ITEM.id, ContextMenu.NONE, getString(R.string.use_item_menu_item));
            menu.add(ContextMenu.NONE, ContextMenuItems.EDIT_ITEM.id, ContextMenu.NONE, getString(R.string.edit_item_menu_item));
            menu.add(ContextMenu.NONE, ContextMenuItems.VIEW_SCHEDULES.id, ContextMenu.NONE, getString(R.string.view_schedules_menu_item));
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo listMenuInfo = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        if (listMenuInfo == null) {
            return super.onContextItemSelected(item);
        }

        int groupPosition = ExpandableListView.getPackedPositionGroup(listMenuInfo.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(listMenuInfo.packedPosition);

        if (item.getItemId() == ContextMenuItems.USE_ITEM.id) {
            showRemoveItemDialog(groupPosition, childPosition);
            return true;
        } else if (item.getItemId() == ContextMenuItems.EDIT_ITEM.id) {
            Item foodItem = pageViewModel.getItem(pageViewModel.getUser(), groupPosition, childPosition);
            showAddOrEditItemAlertDialog(foodItem);
            return true;
        } else if (item.getItemId() == ContextMenuItems.VIEW_SCHEDULES.id) {
            showScheduleItemUseDialog(groupPosition, childPosition);
            return true;
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Convert Calendar value to a string
     * @author u7648367 Ruixian Wu
     */
    public static String calendarToBtnString(Calendar c) {
        String s = intToMonth(c.get(Calendar.MONTH)) + " "
                + c.get(Calendar.DATE) + ", "
                + c.get(Calendar.YEAR) + "\n";
        int hourInt = c.get(Calendar.HOUR);
        String hour = hourInt == 0 ? "12" : hourInt + "";
        s = s + hour + ":";
        int minInt = c.get(Calendar.MINUTE);
        String AMPM = (c.get(Calendar.HOUR_OF_DAY) > 11 ? "pm" : "am");
        String min = (minInt < 10 ? "0" + minInt : "" + minInt);
        return s + min + AMPM;
    }

    /**
     * Convert Calendar.MONTH value to a month string
     * @author u7648367 Ruixian Wu
     */
    public static String intToMonth(int month) {
        return switch (month) {
            case 0 -> "Jan";
            case 1 -> "Feb";
            case 2 -> "Mar";
            case 3 -> "Apr";
            case 4 -> "May";
            case 5 -> "Jun";
            case 6 -> "Jul";
            case 7 -> "Aug";
            case 8 -> "Sep";
            case 9 -> "Oct";
            case 10 -> "Nov";
            case 11 -> "Dec";
            default -> "invalid month";
        };
    }

    /**
     * This generates the observer of the ui state
     * @return the observe of a ui state
     */
    private Observer<OverviewViewModel.OverviewUIState> uiStateObserver() {
        return overviewUIState -> {
            int expandedGroup = -1;

            if(expandableListAdapter != null) {
                for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
                    if(expandableListView.isGroupExpanded(i)) {
                        expandedGroup = i;
                        break;
                    }
                }
            }

            expandableListAdapter = new MyExpandableListAdapter(
                    getContext(),
                    overviewUIState.getItemCollection(),
                    overviewUIState.getItemExpiryDate(),
                    overviewUIState.getItemBoughtDate(),
                    overviewUIState.getItemQuantity(),
                    overviewUIState.getItemLocation(),
                    overviewUIState.getItemSchedule(),
                    overviewUIState.getGroupList()
            );

            expandableListView.setAdapter(expandableListAdapter);
            ((BaseExpandableListAdapter) expandableListAdapter).notifyDataSetChanged();
            if(expandedGroup != -1) {
                expandableListView.expandGroup(expandedGroup);
            }
            Log.i("DEBUG", "Observed Changes");
        };
    }
}