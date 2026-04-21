package dev.ia.dto;

import java.util.List;

public record TravelPackageList(
        String categoria,
        List<TravelPackage> pacotes
) {}

