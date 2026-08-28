package com.artillexstudios.axvaults.utils;

import java.util.List;

public class SimpleRegex {

    public static boolean matches(List<String> list, String txt) {
        for (String string : list) {
            if (matches(string, txt)) return true;
        }
        return false;
    }

    public static boolean matches(String string, String txt) {
        if (string.equals("*")) return true;
        if (string.length() < 2) return string.equals(txt);
        RegexType regexType = RegexType.EQUALS;
        boolean starts = string.charAt(0) == '*';
        boolean ends = string.charAt(string.length() - 1) == '*';
        if (starts && ends) {
            string = string.substring(1, string.length() - 1);
            regexType = RegexType.CONTAINS;
        }
        else if (starts) {
            string = string.substring(1);
            regexType = RegexType.STARTS_WITH;
        }
        else if (ends) {
            string = string.substring(0, string.length() - 1);
            regexType = RegexType.ENDS_WITH;
        }

        boolean result = switch (regexType) {
            case CONTAINS -> txt.contains(string);
            case STARTS_WITH -> txt.endsWith(string);
            case ENDS_WITH -> txt.startsWith(string);
            case EQUALS -> string.equals(txt);
        };
        return result;
    }

    private enum RegexType {
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        EQUALS
    }
}
