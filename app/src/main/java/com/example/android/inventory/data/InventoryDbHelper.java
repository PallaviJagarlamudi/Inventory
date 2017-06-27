package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventory.data.InventoryContract.OrdersEntry;
import com.example.android.inventory.data.InventoryContract.ProductEntry;
import com.example.android.inventory.data.InventoryContract.SalesEntry;
import com.example.android.inventory.data.InventoryContract.SupplierEntry;

/**
 * Created by Pallavi J on 21-06-2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {
    public final static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ProductEntry.SQL_CREATE_PRODUCTS_TABLE);
        db.execSQL(SupplierEntry.SQL_CREATE_SUPPLIER_TABLE);
        db.execSQL(SalesEntry.SQL_CREATE_SALES_TABLE);
        db.execSQL(OrdersEntry.SQL_CREATE_ORDERS_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(ProductEntry.SQL_DELETE_PRODUCTS_TABLE);
        db.execSQL(SupplierEntry.SQL_DELETE_SUPPLIER_TABLE);
        db.execSQL(SalesEntry.SQL_DELETE_SALES_TABLE);
        db.execSQL(OrdersEntry.SQL_DELETE_ORDERS_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
