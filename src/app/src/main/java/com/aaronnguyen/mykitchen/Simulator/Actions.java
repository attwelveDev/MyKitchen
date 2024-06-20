package com.aaronnguyen.mykitchen.Simulator;

import android.content.Context;
import android.util.Log;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemFactory;
import com.aaronnguyen.mykitchen.model.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Actions {

    ActionSimulator.TESTCODE testcode;
    Context currentContext;

    public Actions(ActionSimulator.TESTCODE code, Context currentContext) {
        this.testcode = code;
        this.currentContext = currentContext;
        if(code.equals(ActionSimulator.TESTCODE.MainStream)) {
            loadData();
        }
    }

    private void loadData() {
        BufferedReader bufferedReader;

        try{
            bufferedReader = new BufferedReader(new InputStreamReader(currentContext.getResources().openRawResource(R.raw.main_data_stream_item), StandardCharsets.UTF_8));
            String line;

            if(bufferedReader.readLine() == null)
                return;

            while((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(",");
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, Integer.parseInt(tokens[2]));
                Item item = ItemFactory.createItem(
                        tokens[0],
                        tokens[1],
                        calendar.getTime(),
                        new Date(),
                        ActionSimulator.DUMMY1,
                        Integer.parseInt(tokens[4]),
                        tokens[3],
                        new ArrayList<>()
                );

                simulationItem.add(item);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ArrayList<Item> simulationItem = new ArrayList<>();

    private int currentLinePos = 0;

    public boolean hasNext() {
        if (Objects.requireNonNull(testcode) == ActionSimulator.TESTCODE.MainStream) {
            return currentLinePos != simulationItem.size();
        }
        return false;
    }

    public Item nextItem() {
        Log.i(ActionSimulator.SIMULATION_CHANNEL, simulationItem.get(currentLinePos).toString());
        return simulationItem.get(currentLinePos++);
    }
}
