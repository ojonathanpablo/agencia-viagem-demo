package dev.ia.dto;

public record TravelPackage(
        String destino,
        String categoria,
        String dataInicio,
        String dataFim,
        String status
) {}

