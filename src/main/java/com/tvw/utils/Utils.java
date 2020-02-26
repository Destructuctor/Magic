package com.tvw.utils;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static String color(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static List<String> color(List<String> strings) {
        return strings.stream().map(Utils::color).collect(Collectors.toList());
    }
}
