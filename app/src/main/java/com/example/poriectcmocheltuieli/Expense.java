package com.example.poriectcmocheltuieli;

public class Expense {
    private String date;
    private String description;
    private double amount;

    public Expense(String date, String description, double amount) {
        this.date = date;
        this.description = description;
        this.amount = amount;
    }

    // Getter pentru data
    public String getDate() {
        return date;
    }

    // Setter pentru data (opțional, doar dacă este necesar)
    public void setDate(String date) {
        this.date = date;
    }

    // Getter pentru descriere
    public String getDescription() {
        return description;
    }

    // Setter pentru descriere (opțional, doar dacă este necesar)
    public void setDescription(String description) {
        this.description = description;
    }

    // Getter pentru sumă
    public double getAmount() {
        return amount;
    }

    // Setter pentru sumă
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
