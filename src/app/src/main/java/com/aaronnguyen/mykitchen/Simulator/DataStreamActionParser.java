package com.aaronnguyen.mykitchen.Simulator;

import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;

public interface DataStreamActionParser {
    void parse() throws InvalidQuantityException;
}
