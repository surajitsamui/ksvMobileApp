package com.temple.donation.model;

/**
 * Temple expenditure model.
 */
public class Expense {
    private long id;
    private String financialYear; // e.g. "2026-27"
    private String month;         // e.g. "October"
    private String description;   // e.g. "Electricity", "Flower", "Prasad"
    private double amount;        // e.g. 2800.0
    private String expenseDate;   // e.g. "06-10-2026"
    private String remarks;

    public Expense() {}

    public Expense(long id, String financialYear, String month, String description,
                   double amount, String expenseDate, String remarks) {
        this.id = id;
        this.financialYear = financialYear;
        this.month = month;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.remarks = remarks;
    }

    public Expense(String financialYear, String month, String description,
                   double amount, String expenseDate, String remarks) {
        this.financialYear = financialYear;
        this.month = month;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.remarks = remarks;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getExpenseDate() { return expenseDate; }
    public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
