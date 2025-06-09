package com.artillexstudios.axvaults.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IntRange implements Cloneable {
    private static final Random RANDOM = new Random();

    private final int min;
    private final int max;

    /**
     * @param min Minimum value (inclusive)
     * @param max Maximum value in range (inclusive)
     */
    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * @param value Minimum and maximum value (inclusive)
     */
    public IntRange(int value) {
        this.min = value;
        this.max = value;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public List<Integer> getValues() {
        List<Integer> list = new ArrayList<>();

        for (int i = min; i <= max; i++) {
            list.add(i);
        }

        return list;
    }

    public boolean contains(int number) {
        return number >= min && number <= max;
    }

    /**
     * @see IntRange#contains(int)
     */
    @Deprecated
    public boolean inRange(int number) {
        return contains(number);
    }

    public int next() {
        if (min == max) {
            return min;
        }

        return RANDOM.nextInt(min, max + 1);
    }

    public static IntRange parseIntRange(String string) throws NumberFormatException {
        if (string == null) {
            throw new NumberFormatException("Cannot parse null string");
        }

        String[] values = string.split("-");
        switch (values.length) {
            case 1 -> {
                int value = Integer.parseInt(values[0]);
                return new IntRange(value, value);
            }
            case 2 -> {
                return new IntRange(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
            }
            default -> throw new NumberFormatException("Cannot parse invalid range format");
        }
    }

    public static IntRange valueOf(Object object) throws NumberFormatException {
        String s;
        try {
            s = (String) object;
        } catch (ClassCastException e) {
            return new IntRange((int) object);
        }

        return parseIntRange(s);
    }

    @Override
    public String toString() {
        return min + "-" + max;
    }

    @Override
    public IntRange clone() {
        try {
            return (IntRange) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
