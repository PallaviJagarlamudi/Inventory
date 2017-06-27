package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 * Created by Pallavi J on 21-06-2017.
 */

public class InventoryContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventory/stock/ is a valid path for
     * looking at stock data. content://com.example.android.inventory/jobs/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "jobs".
     */
    public static final String PATH_PRODUCT = "products";
    public static final String PATH_SUPPLIER = "supplier";
    public static final String PATH_SALES = "sales";
    public static final String PATH_ORDERS = "orders";
    public static final String PATH_ORDER_JOIN = "orders/joinDetails";
    public static final String PATH_SALES_JOIN = "sales/joinDetails";
    public static final String PATH_PRODUCT_QUANTITY = "products/quantity";

    private InventoryContract(){

    }

    //A new class for Stock table
    public static final class ProductEntry implements BaseColumns {
        /** The content URI to access the products data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);
        public static final Uri CONTENT_URI_QUANTITY = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT_QUANTITY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of product entries.
         */
        public static final String CONTENT_PRODUCT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product entry.
         */
        public static final String CONTENT_PRODUCT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product entry.
         */
        public static final String CONTENT_PRODUCT_QUANTITY_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT_QUANTITY;

        /** Name of database table for products */
        public final static String TABLE_NAME = "products";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "name";
        public final static String COLUMN_PRODUCT_IMAGE = "image_uri";
        public final static String COLUMN_PRODUCT_QUANTITY_IN_STOCK = "quantity_in_stock";
        public final static String COLUMN_PRODUCT_QUANTITY_SOLD = "quantity_sold_out";
        public final static String COLUMN_PRODUCT_PRICE = "price";

        // String that contains the SQL statement to create the products table
        public static final String SQL_CREATE_PRODUCTS_TABLE =  "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_IMAGE + " TEXT, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK + " INTEGER NOT NULL DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD + " INTEGER NOT NULL DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL "
                +");";
        // String to drop the products table
        public static final String SQL_DELETE_PRODUCTS_TABLE =
                "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;
    }

    //A new class for Supplier table
    public static final class SupplierEntry implements BaseColumns {

        /** The content URI to access the supplier data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SUPPLIER);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of supplier entries.
         */
        public static final String CONTENT_SUPPLIER_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLIER;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single supplier entry.
         */
        public static final String CONTENT_SUPPLIER_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLIER;


        // String to drop the supplier table
        public static final String SQL_DELETE_SUPPLIER_TABLE =
                "DROP TABLE IF EXISTS " + SupplierEntry.TABLE_NAME;

        /** Name of database table for supplier */
        public final static String TABLE_NAME = "supplier";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_SUPPLIER_NAME = "name";
        public final static String COLUMN_SUPPLIER_EMAIL = "email";

        // String that contains the SQL statement to create the supplier table
        public static final String SQL_CREATE_SUPPLIER_TABLE =  "CREATE TABLE " + SupplierEntry.TABLE_NAME + " ("
                + SupplierEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SupplierEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + SupplierEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL );";

        public static boolean isValidEmail(String email)
        {
            return email!=null && !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    //A new class for Sales table
    public static final class SalesEntry implements BaseColumns {

        /** The content URI to access the sales data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SALES);
        public static final Uri CONTENT_URI_JOIN = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SALES_JOIN);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of sales entries.
         */
        public static final String CONTENT_SALES_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SALES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single sales entry.
         */
        public static final String CONTENT_SALES_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SALES;

        public static final String CONTENT_SALES_JOIN_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SALES_JOIN;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single ordered entry.
         */
        public static final String CONTENT_SALES_JOIN_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SALES_JOIN;

        /** Name of database table for sales */
        public final static String TABLE_NAME = "sales";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_SALES_ITEM_PRODCUT_ID= "product_id";
        public final static String COLUMN_SALES_ITEM_QUANTITY = "quantity";
        public final static String COLUMN_SALES_ITEM_PRICE = "price";
        public final static String COLUMN_SALES_ITEM_TIMESTAMP = "timestamp";

        // String that contains the SQL statement to create the sales table
        public static final String SQL_CREATE_SALES_TABLE =  "CREATE TABLE " + SalesEntry.TABLE_NAME + " ("
                + SalesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID+ " INTEGER NOT NULL, "
                + SalesEntry.COLUMN_SALES_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + SalesEntry.COLUMN_SALES_ITEM_PRICE + " REAL NOT NULL, "
                + SalesEntry.COLUMN_SALES_ITEM_TIMESTAMP + " DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(" + SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID +") REFERENCES " + ProductEntry.TABLE_NAME + "("
                + ProductEntry._ID + ") ON DELETE CASCADE "
                +");";

        // String to drop the sales table
        public static final String SQL_DELETE_SALES_TABLE =
                "DROP TABLE IF EXISTS " + SalesEntry.TABLE_NAME;

        //Alias names of columns
        public final static String ALIAS_COLUMN_SALES_PRODUCT_NAME= "product_name";

    }

    //A new class for Orders table
    public static final class OrdersEntry implements BaseColumns {

        /** The content URI to access the ordered data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ORDERS);
        public static final Uri CONTENT_URI_JOIN = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ORDER_JOIN);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of ordered entries.
         */
        public static final String CONTENT_ORDER_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ORDERS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single ordered entry.
         */
        public static final String CONTENT_ORDER_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ORDERS;

        public static final String CONTENT_ORDER_JOIN_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ORDER_JOIN;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single ordered entry.
         */
        public static final String CONTENT_ORDER_JOIN_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ORDER_JOIN;


        /** Name of database table for orders */
        public final static String TABLE_NAME = "orders";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ORDER_PRODUCT_ID= "product_id";
        public final static String COLUMN_ORDER_SUPPLIER_ID= "supplier_id";
        public final static String COLUMN_ORDER_QUANTITY = "quantity";
        public final static String COLUMN_ORDER_STATUS = "status";
        public final static String COLUMN_ORDER_TIMESTAMP = "timestamp";

        // String that contains the SQL statement to create the order table
        public static final String SQL_CREATE_ORDERS_TABLE =  "CREATE TABLE " + OrdersEntry.TABLE_NAME + " ("
                + OrdersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + OrdersEntry.COLUMN_ORDER_PRODUCT_ID + " INTEGER NOT NULL, "
                + OrdersEntry.COLUMN_ORDER_SUPPLIER_ID + " INTEGER NOT NULL, "
                + OrdersEntry.COLUMN_ORDER_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + OrdersEntry.COLUMN_ORDER_STATUS + " INTEGER NOT NULL DEFAULT "+ OrdersEntry.STATUS_ORDERED + ", "
                + OrdersEntry.COLUMN_ORDER_TIMESTAMP + " DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(" + OrdersEntry.COLUMN_ORDER_PRODUCT_ID +") REFERENCES " + ProductEntry.TABLE_NAME
                + "(" + ProductEntry._ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY(" + OrdersEntry.COLUMN_ORDER_SUPPLIER_ID +") REFERENCES " +SupplierEntry.TABLE_NAME
                + "(" + SupplierEntry._ID + ") ON DELETE CASCADE"
                +");";

        // String to drop the orders table
        public static final String SQL_DELETE_ORDERS_TABLE =
                "DROP TABLE IF EXISTS " + OrdersEntry.TABLE_NAME;

        //Alias names of columns
        public final static String ALIAS_COLUMN_ORDER_PRODUCT_NAME= "product_name";
        public final static String ALIAS_COLUMN_ORDER_SUPPLIER_NAME= "supplier_name";

        /**
         * Possible values for the status of the order.
         */
        public final static int STATUS_ORDERED = 0;
        public final static int STATUS_DELIVERED = 1;
        public final static int STATUS_CANCELLED = 2;


        /**
         * Returns whether or not the given status is {@link #STATUS_DELIVERED}, {@link #STATUS_ORDERED},
         * or {@link #STATUS_CANCELLED}.
         */
        public static boolean isValidStatus(int status) {
            return (status ==  STATUS_ORDERED || status == STATUS_DELIVERED || status == STATUS_CANCELLED );
        }
    }
}
