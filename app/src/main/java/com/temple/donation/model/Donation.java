package com.temple.donation.model;

/**
 * Actual donation record for a specific devotee, financial year and month.
 */
public class Donation {
    private long id;
    private long devoteeId;
    private String devoteeName; // Helper for display
    private String financialYear; // e.g. "2026-27"
    private String month;         // e.g. "October"
    private double amount;        // Actual amount paid (e.g. 700)
    private String donationDate;  // e.g. "08-10-2026"
    private String paymentMode;   // "Cash", "UPI", "Bank Transfer", "Cheque"
    private String remarks;

    public Donation() {}

    public Donation(long id, long devoteeId, String financialYear, String month,
                    double amount, String donationDate, String paymentMode, String remarks) {
        this.id = id;
        this.devoteeId = devoteeId;
        this.financialYear = financialYear;
        this.month = month;
        this.amount = amount;
        this.donationDate = donationDate;
        this.paymentMode = paymentMode;
        this.remarks = remarks;
    }

    public Donation(long devoteeId, String financialYear, String month,
                    double amount, String donationDate, String paymentMode, String remarks) {
        this.devoteeId = devoteeId;
        this.financialYear = financialYear;
        this.month = month;
        this.amount = amount;
        this.donationDate = donationDate;
        this.paymentMode = paymentMode;
        this.remarks = remarks;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getDevoteeId() { return devoteeId; }
    public void setDevoteeId(long devoteeId) { this.devoteeId = devoteeId; }

    public String getDevoteeName() { return devoteeName; }
    public void setDevoteeName(String devoteeName) { this.devoteeName = devoteeName; }

    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDonationDate() { return donationDate; }
    public void setDonationDate(String donationDate) { this.donationDate = donationDate; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
