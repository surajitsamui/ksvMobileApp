package com.temple.donation.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.temple.donation.model.Devotee;
import com.temple.donation.model.Donation;
import com.temple.donation.model.Expense;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "temple_donation.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_DEVOTEE = "DEVOTEE";
    public static final String TABLE_DONATION = "DONATION";
    public static final String TABLE_EXPENSE = "EXPENSE";

    // DEVOTEE columns
    public static final String COL_DEV_ID = "id";
    public static final String COL_DEV_NAME = "name";
    public static final String COL_DEV_MOBILE = "mobile";
    public static final String COL_DEV_ADDRESS = "address";
    public static final String COL_DEV_MONTHLY_AMOUNT = "monthly_amount";
    public static final String COL_DEV_ACTIVE = "active";

    // DONATION columns
    public static final String COL_DON_ID = "id";
    public static final String COL_DON_DEVOTEE_ID = "devotee_id";
    public static final String COL_DON_FY = "financial_year";
    public static final String COL_DON_MONTH = "month";
    public static final String COL_DON_AMOUNT = "amount";
    public static final String COL_DON_DATE = "donation_date";
    public static final String COL_DON_PAYMENT_MODE = "payment_mode";
    public static final String COL_DON_REMARKS = "remarks";

    // EXPENSE columns
    public static final String COL_EXP_ID = "id";
    public static final String COL_EXP_FY = "financial_year";
    public static final String COL_EXP_MONTH = "month";
    public static final String COL_EXP_DESC = "description";
    public static final String COL_EXP_AMOUNT = "amount";
    public static final String COL_EXP_DATE = "expense_date";
    public static final String COL_EXP_REMARKS = "remarks";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create DEVOTEE table
        String createDevotee = "CREATE TABLE " + TABLE_DEVOTEE + " ("
                + COL_DEV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DEV_NAME + " TEXT NOT NULL, "
                + COL_DEV_MOBILE + " TEXT, "
                + COL_DEV_ADDRESS + " TEXT, "
                + COL_DEV_MONTHLY_AMOUNT + " REAL NOT NULL, "
                + COL_DEV_ACTIVE + " INTEGER DEFAULT 1);";

        // Create DONATION table (Unique per devotee + financial_year + month)
        String createDonation = "CREATE TABLE " + TABLE_DONATION + " ("
                + COL_DON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DON_DEVOTEE_ID + " INTEGER NOT NULL, "
                + COL_DON_FY + " TEXT NOT NULL, "
                + COL_DON_MONTH + " TEXT NOT NULL, "
                + COL_DON_AMOUNT + " REAL NOT NULL, "
                + COL_DON_DATE + " TEXT NOT NULL, "
                + COL_DON_PAYMENT_MODE + " TEXT, "
                + COL_DON_REMARKS + " TEXT, "
                + "FOREIGN KEY(" + COL_DON_DEVOTEE_ID + ") REFERENCES " + TABLE_DEVOTEE + "(" + COL_DEV_ID + "), "
                + "UNIQUE(" + COL_DON_DEVOTEE_ID + ", " + COL_DON_FY + ", " + COL_DON_MONTH + "));";

        // Create EXPENSE table
        String createExpense = "CREATE TABLE " + TABLE_EXPENSE + " ("
                + COL_EXP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_EXP_FY + " TEXT NOT NULL, "
                + COL_EXP_MONTH + " TEXT NOT NULL, "
                + COL_EXP_DESC + " TEXT NOT NULL, "
                + COL_EXP_AMOUNT + " REAL NOT NULL, "
                + COL_EXP_DATE + " TEXT NOT NULL, "
                + COL_EXP_REMARKS + " TEXT);";

        db.execSQL(createDevotee);
        db.execSQL(createDonation);
        db.execSQL(createExpense);

        // Seed initial sample data as requested
        seedSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DONATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVOTEE);
        onCreate(db);
    }

    private void seedSampleData(SQLiteDatabase db) {
        // Devotee 1: Mritunjay Kanthal (Default ₹500)
        long mritunjayId = insertDevoteeDirect(db, "Mritunjay Kanthal", "9876543210", "Howrah", 500, 1);
        // Devotee 2: Ranjit Samui (Default ₹500)
        long ranjitId = insertDevoteeDirect(db, "Ranjit Samui", "9830112233", "Kanduah", 500, 1);
        // Devotee 3: Sukla Samui (Default ₹300)
        long suklaId = insertDevoteeDirect(db, "Sukla Samui", "9748223344", "Sankrail", 300, 1);
        // Devotee 4: Debasish Banerjee (Default ₹1000)
        long debasishId = insertDevoteeDirect(db, "Debasish Banerjee", "9831998877", "Andul", 1000, 1);
        // Devotee 5: Subrata Mondal (Default ₹500)
        long subrataId = insertDevoteeDirect(db, "Subrata Mondal", "9123456789", "Domjur", 500, 1);
        // Devotee 6: Pranab Ghosh (Default ₹400)
        long pranabId = insertDevoteeDirect(db, "Pranab Ghosh", "9874561230", "Bally", 400, 1);

        // Mritunjay Kanthal 2026-27 records:
        // April: ₹500, May: ₹500, July: ₹700, September: ₹500, October: ₹700 (Master amount stays ₹500)
        insertDonationDirect(db, mritunjayId, "2026-27", "April", 500, "05-04-2026", "Cash", "Monthly donation");
        insertDonationDirect(db, mritunjayId, "2026-27", "May", 500, "07-05-2026", "UPI", "GPay");
        insertDonationDirect(db, mritunjayId, "2026-27", "July", 700, "12-07-2026", "Cash", "Special donation");
        insertDonationDirect(db, mritunjayId, "2026-27", "September", 500, "10-09-2026", "UPI", "Monthly");
        insertDonationDirect(db, mritunjayId, "2026-27", "October", 700, "08-10-2026", "Cash", "Puja special");

        // Ranjit Samui October ₹500
        insertDonationDirect(db, ranjitId, "2026-27", "October", 500, "04-10-2026", "Cash", "Monthly");
        // Sukla Samui October ₹300
        insertDonationDirect(db, suklaId, "2026-27", "October", 300, "06-10-2026", "Cash", "Monthly");
        // Debasish Banerjee October ₹1000
        insertDonationDirect(db, debasishId, "2026-27", "October", 1000, "02-10-2026", "Bank Transfer", "NEFT");
        // Subrata Mondal October ₹500
        insertDonationDirect(db, subrataId, "2026-27", "October", 500, "05-10-2026", "UPI", "Monthly");
        // Additional sponsor donation for October totaling ₹25,500
        insertDonationDirect(db, pranabId, "2026-27", "October", 22500, "07-10-2026", "Cheque", "Festival Sponsor");

        // Expenses for October 2026 (Total ₹12,300)
        // Electricity: ₹2,800
        insertExpenseDirect(db, "2026-27", "October", "Electricity", 2800, "06-10-2026", "WBSEDCL October Bill");
        // Flower: ₹500
        insertExpenseDirect(db, "2026-27", "October", "Flower", 500, "07-10-2026", "Daily Puja garlands");
        // Prasad: ₹1,200
        insertExpenseDirect(db, "2026-27", "October", "Prasad", 1200, "08-10-2026", "Bhog items & sweets");
        // Cleaning & Priest dakshina: ₹7,800
        insertExpenseDirect(db, "2026-27", "October", "Temple Maintenance & Sound", 7800, "05-10-2026", "Monthly maintenance");
    }

    private long insertDevoteeDirect(SQLiteDatabase db, String name, String mobile, String address, double amount, int active) {
        ContentValues cv = new ContentValues();
        cv.put(COL_DEV_NAME, name);
        cv.put(COL_DEV_MOBILE, mobile);
        cv.put(COL_DEV_ADDRESS, address);
        cv.put(COL_DEV_MONTHLY_AMOUNT, amount);
        cv.put(COL_DEV_ACTIVE, active);
        return db.insert(TABLE_DEVOTEE, null, cv);
    }

    private void insertDonationDirect(SQLiteDatabase db, long devoteeId, String fy, String month, double amount, String date, String mode, String remarks) {
        ContentValues cv = new ContentValues();
        cv.put(COL_DON_DEVOTEE_ID, devoteeId);
        cv.put(COL_DON_FY, fy);
        cv.put(COL_DON_MONTH, month);
        cv.put(COL_DON_AMOUNT, amount);
        cv.put(COL_DON_DATE, date);
        cv.put(COL_DON_PAYMENT_MODE, mode);
        cv.put(COL_DON_REMARKS, remarks);
        db.insert(TABLE_DONATION, null, cv);
    }

    private void insertExpenseDirect(SQLiteDatabase db, String fy, String month, String desc, double amount, String date, String remarks) {
        ContentValues cv = new ContentValues();
        cv.put(COL_EXP_FY, fy);
        cv.put(COL_EXP_MONTH, month);
        cv.put(COL_EXP_DESC, desc);
        cv.put(COL_EXP_AMOUNT, amount);
        cv.put(COL_EXP_DATE, date);
        cv.put(COL_EXP_REMARKS, remarks);
        db.insert(TABLE_EXPENSE, null, cv);
    }

    // ==========================================
    // DEVOTEE CRUD OPERATIONS
    // ==========================================

    public long insertDevotee(Devotee devotee) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DEV_NAME, devotee.getName());
        cv.put(COL_DEV_MOBILE, devotee.getMobile());
        cv.put(COL_DEV_ADDRESS, devotee.getAddress());
        cv.put(COL_DEV_MONTHLY_AMOUNT, devotee.getMonthlyAmount());
        cv.put(COL_DEV_ACTIVE, devotee.isActive() ? 1 : 0);
        return db.insert(TABLE_DEVOTEE, null, cv);
    }

    public int updateDevotee(Devotee devotee) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DEV_NAME, devotee.getName());
        cv.put(COL_DEV_MOBILE, devotee.getMobile());
        cv.put(COL_DEV_ADDRESS, devotee.getAddress());
        cv.put(COL_DEV_MONTHLY_AMOUNT, devotee.getMonthlyAmount());
        cv.put(COL_DEV_ACTIVE, devotee.isActive() ? 1 : 0);
        return db.update(TABLE_DEVOTEE, cv, COL_DEV_ID + " = ?", new String[]{String.valueOf(devotee.getId())});
    }

    public List<Devotee> getAllDevotees(boolean activeOnly) {
        List<Devotee> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DEVOTEE;
        if (activeOnly) {
            query += " WHERE " + COL_DEV_ACTIVE + " = 1";
        }
        query += " ORDER BY " + COL_DEV_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Devotee d = new Devotee();
                d.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DEV_ID)));
                d.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DEV_NAME)));
                d.setMobile(cursor.getString(cursor.getColumnIndexOrThrow(COL_DEV_MOBILE)));
                d.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COL_DEV_ADDRESS)));
                d.setMonthlyAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DEV_MONTHLY_AMOUNT)));
                d.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DEV_ACTIVE)) == 1);
                list.add(d);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Devotee getDevoteeById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DEVOTEE, null, COL_DEV_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Devotee d = new Devotee();
            d.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DEV_ID)));
            d.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DEV_NAME)));
            d.setMobile(cursor.getString(cursor.getColumnIndexOrThrow(COL_DEV_MOBILE)));
            d.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COL_DEV_ADDRESS)));
            d.setMonthlyAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DEV_MONTHLY_AMOUNT)));
            d.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DEV_ACTIVE)) == 1);
            cursor.close();
            return d;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public int toggleDevoteeStatus(long id, boolean active) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DEV_ACTIVE, active ? 1 : 0);
        return db.update(TABLE_DEVOTEE, cv, COL_DEV_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // ==========================================
    // DONATION OPERATIONS
    // ==========================================

    public long saveOrUpdateDonation(Donation donation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DON_DEVOTEE_ID, donation.getDevoteeId());
        cv.put(COL_DON_FY, donation.getFinancialYear());
        cv.put(COL_DON_MONTH, donation.getMonth());
        cv.put(COL_DON_AMOUNT, donation.getAmount());
        cv.put(COL_DON_DATE, donation.getDonationDate());
        cv.put(COL_DON_PAYMENT_MODE, donation.getPaymentMode());
        cv.put(COL_DON_REMARKS, donation.getRemarks());

        // Check if donation already exists for this devotee, FY, and month
        Cursor cursor = db.query(TABLE_DONATION, new String[]{COL_DON_ID},
                COL_DON_DEVOTEE_ID + " = ? AND " + COL_DON_FY + " = ? AND " + COL_DON_MONTH + " = ?",
                new String[]{String.valueOf(donation.getDevoteeId()), donation.getFinancialYear(), donation.getMonth()},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            long existingId = cursor.getLong(0);
            cursor.close();
            db.update(TABLE_DONATION, cv, COL_DON_ID + " = ?", new String[]{String.valueOf(existingId)});
            return existingId;
        }
        if (cursor != null) cursor.close();

        return db.insert(TABLE_DONATION, null, cv);
    }

    public Donation getDonation(long devoteeId, String fy, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DONATION, null,
                COL_DON_DEVOTEE_ID + " = ? AND " + COL_DON_FY + " = ? AND " + COL_DON_MONTH + " = ?",
                new String[]{String.valueOf(devoteeId), fy, month},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Donation don = mapDonation(cursor);
            cursor.close();
            return don;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Donation> getDevoteeDonationsForYear(long devoteeId, String fy) {
        List<Donation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DONATION, null,
                COL_DON_DEVOTEE_ID + " = ? AND " + COL_DON_FY + " = ?",
                new String[]{String.valueOf(devoteeId), fy},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(mapDonation(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Donation> getDonationsByMonth(String fy, String month) {
        List<Donation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT d.*, dev." + COL_DEV_NAME + " AS devotee_name "
                + "FROM " + TABLE_DONATION + " d "
                + "JOIN " + TABLE_DEVOTEE + " dev ON d." + COL_DON_DEVOTEE_ID + " = dev." + COL_DEV_ID + " "
                + "WHERE d." + COL_DON_FY + " = ? AND d." + COL_DON_MONTH + " = ? "
                + "ORDER BY dev." + COL_DEV_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{fy, month});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Donation don = mapDonation(cursor);
                don.setDevoteeName(cursor.getString(cursor.getColumnIndexOrThrow("devotee_name")));
                list.add(don);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public double getTotalDonation(String fy, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_DON_AMOUNT + ") FROM " + TABLE_DONATION
                + " WHERE " + COL_DON_FY + " = ? AND " + COL_DON_MONTH + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{fy, month});
        double total = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private Donation mapDonation(Cursor cursor) {
        Donation don = new Donation();
        don.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DON_ID)));
        don.setDevoteeId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DON_DEVOTEE_ID)));
        don.setFinancialYear(cursor.getString(cursor.getColumnIndexOrThrow(COL_DON_FY)));
        don.setMonth(cursor.getString(cursor.getColumnIndexOrThrow(COL_DON_MONTH)));
        don.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DON_AMOUNT)));
        don.setDonationDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DON_DATE)));
        don.setPaymentMode(cursor.getString(cursor.getColumnIndexOrThrow(COL_DON_PAYMENT_MODE)));
        don.setRemarks(cursor.getString(cursor.getColumnIndexOrThrow(COL_DON_REMARKS)));
        return don;
    }

    // ==========================================
    // EXPENSE OPERATIONS
    // ==========================================

    public long insertExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EXP_FY, expense.getFinancialYear());
        cv.put(COL_EXP_MONTH, expense.getMonth());
        cv.put(COL_EXP_DESC, expense.getDescription());
        cv.put(COL_EXP_AMOUNT, expense.getAmount());
        cv.put(COL_EXP_DATE, expense.getExpenseDate());
        cv.put(COL_EXP_REMARKS, expense.getRemarks());
        return db.insert(TABLE_EXPENSE, null, cv);
    }

    public List<Expense> getExpensesByMonth(String fy, String month) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSE, null,
                COL_EXP_FY + " = ? AND " + COL_EXP_MONTH + " = ?",
                new String[]{fy, month},
                null, null, COL_EXP_DATE + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expense exp = new Expense();
                exp.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_EXP_ID)));
                exp.setFinancialYear(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_FY)));
                exp.setMonth(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_MONTH)));
                exp.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DESC)));
                exp.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_AMOUNT)));
                exp.setExpenseDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)));
                exp.setRemarks(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_REMARKS)));
                list.add(exp);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public double getTotalExpense(String fy, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_EXP_AMOUNT + ") FROM " + TABLE_EXPENSE
                + " WHERE " + COL_EXP_FY + " = ? AND " + COL_EXP_MONTH + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{fy, month});
        double total = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public List<Donation> getDonationsByYear(String fy) {
        List<Donation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT d.*, dev." + COL_DEV_NAME + " AS devotee_name "
                + "FROM " + TABLE_DONATION + " d "
                + "JOIN " + TABLE_DEVOTEE + " dev ON d." + COL_DON_DEVOTEE_ID + " = dev." + COL_DEV_ID + " "
                + "WHERE d." + COL_DON_FY + " = ? "
                + "ORDER BY d." + COL_DON_ID + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{fy});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Donation don = mapDonation(cursor);
                don.setDevoteeName(cursor.getString(cursor.getColumnIndexOrThrow("devotee_name")));
                list.add(don);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Donation> getAllDonations() {
        List<Donation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT d.*, dev." + COL_DEV_NAME + " AS devotee_name "
                + "FROM " + TABLE_DONATION + " d "
                + "JOIN " + TABLE_DEVOTEE + " dev ON d." + COL_DON_DEVOTEE_ID + " = dev." + COL_DEV_ID + " "
                + "ORDER BY d." + COL_DON_ID + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Donation don = mapDonation(cursor);
                don.setDevoteeName(cursor.getString(cursor.getColumnIndexOrThrow("devotee_name")));
                list.add(don);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Expense> getExpensesByYear(String fy) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSE, null,
                COL_EXP_FY + " = ?",
                new String[]{fy},
                null, null, COL_EXP_DATE + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expense exp = new Expense();
                exp.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_EXP_ID)));
                exp.setFinancialYear(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_FY)));
                exp.setMonth(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_MONTH)));
                exp.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DESC)));
                exp.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_AMOUNT)));
                exp.setExpenseDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)));
                exp.setRemarks(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_REMARKS)));
                list.add(exp);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSE, null, null, null, null, null, COL_EXP_ID + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expense exp = new Expense();
                exp.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_EXP_ID)));
                exp.setFinancialYear(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_FY)));
                exp.setMonth(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_MONTH)));
                exp.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DESC)));
                exp.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_AMOUNT)));
                exp.setExpenseDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)));
                exp.setRemarks(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_REMARKS)));
                list.add(exp);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public int deleteExpense(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EXPENSE, COL_EXP_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteDevotee(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete all associated donations first
        db.delete(TABLE_DONATION, COL_DON_DEVOTEE_ID + " = ?", new String[]{String.valueOf(id)});
        return db.delete(TABLE_DEVOTEE, COL_DEV_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteDonation(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_DONATION, COL_DON_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DONATION, null, null);
        db.delete(TABLE_EXPENSE, null, null);
        db.delete(TABLE_DEVOTEE, null, null);
    }

    public void resetToDemoData() {
        SQLiteDatabase db = this.getWritableDatabase();
        clearAllData();
        seedInitialData(db);
    }

private void seedInitialData(SQLiteDatabase db) {
    ContentValues devotee = new ContentValues();
    devotee.put(COL_DEV_NAME, "Mritunjay Kanthal");

    // Add only the columns that actually exist in your table.
    long devoteeId = db.insert(TABLE_DEVOTEE, null, devotee);

    if (devoteeId == -1) {
        return;
    }
}
}
