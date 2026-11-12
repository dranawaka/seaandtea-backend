package com.seaandtea.service;

import java.util.Comparator;
import java.util.List;

public final class Destinations {

    public static final List<String> ALL = List.of(
            "Colombo",
            "Kandy",
            "Sigiriya",
            "Ella",
            "Nuwara Eliya",
            "Galle",
            "Mirissa",
            "Bentota",
            "Yala",
            "Wilpattu",
            "Trincomalee",
            "Negombo"
    );

    private Destinations() {
    }

    public static String canonical(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Destination is required");
        }
        return ALL.stream()
                .filter(destination -> destination.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown destination: " + value.trim()));
    }

    public static String fromMeetingPoint(String meetingPoint) {
        if (meetingPoint == null || meetingPoint.isBlank()) {
            return "Colombo";
        }
        String lowered = meetingPoint.toLowerCase();
        return ALL.stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .filter(destination -> lowered.contains(destination.toLowerCase()))
                .findFirst()
                .orElse("Colombo");
    }
}
