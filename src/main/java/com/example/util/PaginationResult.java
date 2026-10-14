package com.example.util;

import java.util.List;

public class PaginationResult<T> {

    private List<T> items;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalItems;

    public PaginationResult(
            List<T> items,
            int currentPage,
            int pageSize,
            long totalItems) {

        if (items == null) {
            throw new IllegalArgumentException(
                "La liste des resultats ne peut pas etre null."
            );
        }

        if (currentPage < 1 || pageSize < 1 || totalItems < 0) {
            throw new IllegalArgumentException(
                "Les parametres de pagination sont invalides."
            );
        }

        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;

        this.totalPages =
            (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }
}
