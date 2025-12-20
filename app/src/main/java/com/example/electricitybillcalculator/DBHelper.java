package com.example.electricitybillcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ElectricityBill.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_BILLS = "bills";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_UNITS = "units";
    public static final String COLUMN_REBATE = "rebate";
    public static final String COLUMN_TOTAL_CHARGES = "total_charges";
    public static final String COLUMN_FINAL_COST = "final_cost";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_BILLS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MONTH + " TEXT NOT NULL, " +
                    COLUMN_UNITS + " REAL NOT NULL, " +
                    COLUMN_REBATE + " REAL NOT NULL, " +
                    COLUMN_TOTAL_CHARGES + " REAL NOT NULL, " +
                    COLUMN_FINAL_COST + " REAL NOT NULL);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    /**
     * Add a new bill record to the database
     * @param month The month of the bill
     * @param units Electricity units used
     * @param rebate Rebate percentage (0-5)
     * @param totalCharges Total charges before rebate
     * @param finalCost Final cost after rebate
     * @return The row ID of the newly inserted row, or -1 if an error occurred
     */
    public long addBill(String month, double units, double rebate,
                        double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_FINAL_COST, finalCost);

        long id = db.insert(TABLE_BILLS, null, values);
        db.close();
        return id;
    }

    /**
     * Retrieve all bills from the database
     * @return ArrayList of Bill objects ordered by ID in descending order (newest first)
     */
    public ArrayList<Bill> getAllBills() {
        ArrayList<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_ID, COLUMN_MONTH, COLUMN_UNITS,
                COLUMN_REBATE, COLUMN_TOTAL_CHARGES, COLUMN_FINAL_COST};

        Cursor cursor = db.query(TABLE_BILLS, columns, null, null,
                null, null, COLUMN_ID + " DESC");

        if (cursor != null) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int monthIndex = cursor.getColumnIndex(COLUMN_MONTH);
            int unitsIndex = cursor.getColumnIndex(COLUMN_UNITS);
            int rebateIndex = cursor.getColumnIndex(COLUMN_REBATE);
            int chargesIndex = cursor.getColumnIndex(COLUMN_TOTAL_CHARGES);
            int costIndex = cursor.getColumnIndex(COLUMN_FINAL_COST);

            if (cursor.moveToFirst()) {
                do {
                    // Check if indices are valid (not -1)
                    if (idIndex >= 0 && monthIndex >= 0 && unitsIndex >= 0 &&
                            rebateIndex >= 0 && chargesIndex >= 0 && costIndex >= 0) {

                        Bill bill = new Bill(
                                cursor.getInt(idIndex),
                                cursor.getString(monthIndex),
                                cursor.getDouble(unitsIndex),
                                cursor.getDouble(rebateIndex),
                                cursor.getDouble(chargesIndex),
                                cursor.getDouble(costIndex)
                        );
                        billList.add(bill);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return billList;
    }

    /**
     * Retrieve a specific bill by its ID
     * @param id The ID of the bill to retrieve
     * @return Bill object if found, null otherwise
     */
    public Bill getBillById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Bill bill = null;

        String[] columns = {COLUMN_ID, COLUMN_MONTH, COLUMN_UNITS,
                COLUMN_REBATE, COLUMN_TOTAL_CHARGES, COLUMN_FINAL_COST};

        String selection = COLUMN_ID + "=?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = db.query(TABLE_BILLS, columns, selection,
                selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            bill = new Bill(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_MONTH)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_UNITS)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_REBATE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_TOTAL_CHARGES)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_FINAL_COST))
            );
            cursor.close();
        }
        db.close();
        return bill;
    }

    /**
     * Update an existing bill record
     * @param id The ID of the bill to update
     * @param month Updated month
     * @param units Updated units used
     * @param rebate Updated rebate percentage
     * @param totalCharges Updated total charges
     * @param finalCost Updated final cost
     * @return true if update successful, false otherwise
     */
    public boolean updateBill(int id, String month, double units, double rebate,
                              double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_FINAL_COST, finalCost);

        int rowsAffected = db.update(TABLE_BILLS, values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Delete a bill record from the database
     * @param id The ID of the bill to delete
     * @return true if delete successful, false otherwise
     */
    public boolean deleteBill(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_BILLS,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Get the total number of bills in the database
     * @return Count of bills
     */
    public int getBillsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT * FROM " + TABLE_BILLS;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    /**
     * Delete all bills from the database
     * @return Number of rows deleted
     */
    public int deleteAllBills() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_BILLS, null, null);
        db.close();
        return rowsDeleted;
    }

    /**
     * Search for bills by month
     * @param month The month to search for
     * @return ArrayList of Bill objects for the specified month
     */
    public ArrayList<Bill> getBillsByMonth(String month) {
        ArrayList<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_ID, COLUMN_MONTH, COLUMN_UNITS,
                COLUMN_REBATE, COLUMN_TOTAL_CHARGES, COLUMN_FINAL_COST};

        String selection = COLUMN_MONTH + "=?";
        String[] selectionArgs = { month };

        Cursor cursor = db.query(TABLE_BILLS, columns, selection, selectionArgs,
                null, null, COLUMN_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Bill bill = new Bill(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_MONTH)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_UNITS)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_REBATE)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_TOTAL_CHARGES)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_FINAL_COST))
                );
                billList.add(bill);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return billList;
    }
}