package com.temple.donation.model;

/**
 * Devotee master entity.
 * Note: 'monthlyAmount' represents the DEFAULT monthly commitment.
 * Individual monthly payments can be higher or lower without modifying this default.
 */
public class Devotee {
    private long id;
    private String name;
    private String mobile;
    private String address;
    private double monthlyAmount; // Default monthly amount
    private boolean active;

    public Devotee() {
        this.active = true;
    }

    public Devotee(long id, String name, String mobile, String address, double monthlyAmount, boolean active) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.address = address;
        this.monthlyAmount = monthlyAmount;
        this.active = active;
    }

    public Devotee(String name, String mobile, String address, double monthlyAmount, boolean active) {
        this.name = name;
        this.mobile = mobile;
        this.address = address;
        this.monthlyAmount = monthlyAmount;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return name + " (" + mobile + ")";
    }
}
