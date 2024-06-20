package com.aaronnguyen.mykitchen.Simulator;

import android.content.Context;
import android.util.Log;

import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.user.User;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class ActionSimulator {
    public enum TESTCODE {MainStream}

    public static User DUMMY1 = new User("Dummy 1", "Sample User 1", "dummy1@dummy.com", new ArrayList<>());
    public static User DUMMY2 = new User("Dummy 2", "Sample User 2", "dummy2@dummy.com", new ArrayList<>());

    public final static String SIMULATION_CHANNEL = "SIMULATION";
    private static ActionSimulator INSTANCE;
    private DataStreamActionParser actionParser;
    private TimerTask currentTask;
    private boolean hasStarted = false;

    public static ActionSimulator getInstance() {
        if(INSTANCE == null)
            INSTANCE = new ActionSimulator();
        return INSTANCE;
    }

    private static final int CLOCK_LENGTH = 30 * 1000;

    public void startSimulation(TESTCODE testcode, Kitchen kitchen, Context context) {
        if(hasStarted) {
            Log.i(SIMULATION_CHANNEL, "We already have one running");
            return;
        }

        hasStarted = true;

        final Actions actions = new Actions(testcode, context);

        Log.i(SIMULATION_CHANNEL, "Start simulating data stream");

        Timer timer = new Timer();
        currentTask = new TimerTask() {
            @Override
            public void run() {
            if(!actions.hasNext()) {
                Log.i(SIMULATION_CHANNEL, "Finished calling simulation");
                timer.cancel();
            } else {
                Item item = actions.nextItem();
                actionParser = new ItemActionParser(item, kitchen);
                try {
                    actionParser.parse();
                } catch (InvalidQuantityException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    };
        timer.schedule(currentTask, 100, CLOCK_LENGTH);
    }

    public void stopSimulation() {
        Log.i(SIMULATION_CHANNEL, "Stop simulation");
        hasStarted = false;
        if(currentTask != null)
            currentTask.cancel();
    }
}
