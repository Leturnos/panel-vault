package io.github.leturnos.panelvault.dto;

public record StatsResponseDTO(
        long totalWorks,
        long totalVolumes,
        long completedWorks,
        long onGoingWorks,
        long wishlistItems,
        long totalMangas,
        long totalComics,
        long totalGraphicNovels,
        long totalManhwas) {
}
