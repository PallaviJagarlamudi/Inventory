package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventory.R;
import com.example.android.inventory.data.InventoryContract.OrdersEntry;
import com.example.android.inventory.data.InventoryContract.ProductEntry;
import com.example.android.inventory.data.InventoryContract.SalesEntry;
import com.example.android.inventory.data.InventoryContract.SupplierEntry;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Pallavi J on 21-06-2017.
 */

public class InventoryProvider extends ContentProvider {


    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the tableS in inventory db */
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final int SUPPLIERS = 102;
    private static final int SUPPLIER_ID = 103;
    private static final int SALES = 104;
    private static final int SALE_ID = 105;
    private static final int ORDERS = 106;
    private static final int ORDER_ID = 107;
    private static final int ORDER_PRODUCT_SUPPLIER_JOIN = 108;
    private static final int ORDER_PRODUCT_SUPPLIER_JOIN_ID = 109;
    private static final int PRODUCT_QUANTITY_ID = 110;
    private static final int SALES_PRODUCT_JOIN = 111;
    private static final int SALES_PRODUCT_JOIN_ID = 112;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT, PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT + "/#", PRODUCT_ID);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT_QUANTITY + "/#", PRODUCT_QUANTITY_ID);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SUPPLIER, SUPPLIERS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SUPPLIER + "/#", SUPPLIER_ID);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SALES, SALES);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SALES + "/#", SALE_ID);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SALES_JOIN, SALES_PRODUCT_JOIN);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SALES_JOIN + "/#",SALES_PRODUCT_JOIN_ID);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ORDERS, ORDERS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ORDERS + "/#", ORDER_ID);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ORDER_JOIN, ORDER_PRODUCT_SUPPLIER_JOIN);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_ORDER_JOIN + "/#",ORDER_PRODUCT_SUPPLIER_JOIN_ID);
    }

    private InventoryDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor=null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        SQLiteQueryBuilder mQueryBuilder = new SQLiteQueryBuilder();
        HashMap<String, String> columnMap;
        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table with given arguments. The cursor may contain multiple rows
                cursor = database.query(ProductEntry.TABLE_NAME, projection, null, null,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI, and use in selection argument
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the products table where the _id equals id passed in uri to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SUPPLIERS:
                // For the PRODUCTS code, query the supplier table with given arguments. The cursor may contain multiple rows
                cursor = database.query(SupplierEntry.TABLE_NAME, projection, null, null,
                        null, null, sortOrder);
                break;
            case SUPPLIER_ID:
                // For the SUPPLIER_ID code, extract out the ID from the URI, and use in selection argument for _id
                selection = SupplierEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(SupplierEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SALES:
                // For the SALES code, query the sales table with given arguments. The cursor may contain multiple rows
                cursor = database.query(SalesEntry.TABLE_NAME, projection, null, null,
                        null, null, sortOrder);
                break;
            case SALE_ID:
                // For the SALE_ID code, extract out the ID from the URI, and use in selection argument for _id
                selection = SalesEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(SalesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SALES_PRODUCT_JOIN:
                mQueryBuilder = new SQLiteQueryBuilder();
                columnMap = new HashMap<String, String>();
                columnMap.put(SalesEntry._ID,SalesEntry.TABLE_NAME+"."+SalesEntry._ID);
                columnMap.put(SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID,SalesEntry.TABLE_NAME+"."+SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID);
                columnMap.put(SalesEntry.COLUMN_SALES_ITEM_PRICE,SalesEntry.TABLE_NAME+"."+SalesEntry.COLUMN_SALES_ITEM_PRICE);
                columnMap.put(SalesEntry.COLUMN_SALES_ITEM_QUANTITY,SalesEntry.TABLE_NAME+"."+SalesEntry.COLUMN_SALES_ITEM_QUANTITY);
                columnMap.put(SalesEntry.COLUMN_SALES_ITEM_TIMESTAMP,SalesEntry.TABLE_NAME+"."+SalesEntry.COLUMN_SALES_ITEM_TIMESTAMP);
                columnMap.put(SalesEntry.ALIAS_COLUMN_SALES_PRODUCT_NAME,ProductEntry.TABLE_NAME+"."+ProductEntry.COLUMN_PRODUCT_NAME
                                +" as " + SalesEntry.ALIAS_COLUMN_SALES_PRODUCT_NAME);

                mQueryBuilder.setTables(SalesEntry.TABLE_NAME +
                        " LEFT OUTER JOIN " + ProductEntry.TABLE_NAME + " ON " +
                        SalesEntry.TABLE_NAME + "." + SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID + " = " +
                        ProductEntry.TABLE_NAME + "." + ProductEntry._ID
                );
                mQueryBuilder.setProjectionMap(columnMap);
                cursor = mQueryBuilder.query(database, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SALES_PRODUCT_JOIN_ID:
                mQueryBuilder = new SQLiteQueryBuilder();
                columnMap = new HashMap<String, String>();
                columnMap.put(SalesEntry._ID,SalesEntry.TABLE_NAME+"."+SalesEntry._ID);
                columnMap.put(SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID,SalesEntry.TABLE_NAME+"."+SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID);
                columnMap.put(SalesEntry.COLUMN_SALES_ITEM_PRICE,SalesEntry.TABLE_NAME+"."+SalesEntry.COLUMN_SALES_ITEM_PRICE);
                columnMap.put(SalesEntry.COLUMN_SALES_ITEM_QUANTITY,SalesEntry.TABLE_NAME+"."+SalesEntry.COLUMN_SALES_ITEM_QUANTITY);
                columnMap.put(SalesEntry.COLUMN_SALES_ITEM_TIMESTAMP,SalesEntry.TABLE_NAME+"."+SalesEntry.COLUMN_SALES_ITEM_TIMESTAMP);
                columnMap.put(SalesEntry.ALIAS_COLUMN_SALES_PRODUCT_NAME,ProductEntry.TABLE_NAME+"."+ProductEntry.COLUMN_PRODUCT_NAME
                        +" as " + SalesEntry.ALIAS_COLUMN_SALES_PRODUCT_NAME);

                mQueryBuilder.setTables(SalesEntry.TABLE_NAME +
                        " LEFT OUTER JOIN " + ProductEntry.TABLE_NAME + " ON " +
                        SalesEntry.TABLE_NAME + "." + SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID + " = " +
                        ProductEntry.TABLE_NAME + "." + ProductEntry._ID
                );
                mQueryBuilder.setProjectionMap(columnMap);
                selection = SalesEntry.TABLE_NAME + "." + SalesEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = mQueryBuilder.query(database, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ORDERS:
                // For the SALES code, query the sales table with given arguments. The cursor may contain multiple rows
                cursor = database.query(OrdersEntry.TABLE_NAME, projection, null, null,
                        null, null, sortOrder);
                break;
            case ORDER_ID:
                // For the SALE_ID code, extract out the ID from the URI, and use in selection argument for _id
                selection = OrdersEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(OrdersEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ORDER_PRODUCT_SUPPLIER_JOIN:
                mQueryBuilder = new SQLiteQueryBuilder();
                columnMap = new HashMap<String, String>();
                columnMap.put(OrdersEntry._ID,OrdersEntry.TABLE_NAME+"."+OrdersEntry._ID);
                columnMap.put(OrdersEntry.COLUMN_ORDER_PRODUCT_ID,OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_PRODUCT_ID);
                columnMap.put(OrdersEntry.COLUMN_ORDER_QUANTITY,OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_QUANTITY);
                columnMap.put(OrdersEntry.COLUMN_ORDER_STATUS,OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_STATUS);
                columnMap.put(OrdersEntry.COLUMN_ORDER_SUPPLIER_ID,OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_SUPPLIER_ID);
                columnMap.put(OrdersEntry.COLUMN_ORDER_TIMESTAMP,OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_TIMESTAMP);
                columnMap.put(OrdersEntry.ALIAS_COLUMN_ORDER_PRODUCT_NAME,
                        ProductEntry.TABLE_NAME+"."+ProductEntry.COLUMN_PRODUCT_NAME
                                +" as " + OrdersEntry.ALIAS_COLUMN_ORDER_PRODUCT_NAME);
                columnMap.put(OrdersEntry.ALIAS_COLUMN_ORDER_SUPPLIER_NAME,
                        SupplierEntry.TABLE_NAME+"."+SupplierEntry.COLUMN_SUPPLIER_NAME
                                + " as " + OrdersEntry.ALIAS_COLUMN_ORDER_SUPPLIER_NAME);

                mQueryBuilder.setTables(OrdersEntry.TABLE_NAME +
                    " LEFT OUTER JOIN " + ProductEntry.TABLE_NAME + " ON " +
                        OrdersEntry.TABLE_NAME + "." + OrdersEntry.COLUMN_ORDER_PRODUCT_ID+ " = " +
                        ProductEntry.TABLE_NAME + "." + ProductEntry._ID +
                        " LEFT OUTER JOIN " + SupplierEntry.TABLE_NAME + " ON " +
                        OrdersEntry.TABLE_NAME + "." + OrdersEntry.COLUMN_ORDER_SUPPLIER_ID+ " = " +
                        SupplierEntry.TABLE_NAME + "." + SupplierEntry._ID
                );
                mQueryBuilder.setProjectionMap(columnMap);
                cursor = mQueryBuilder.query(database, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ORDER_PRODUCT_SUPPLIER_JOIN_ID:
                mQueryBuilder = new SQLiteQueryBuilder();
                columnMap = new HashMap<String, String>();
                columnMap.put(OrdersEntry._ID, OrdersEntry.TABLE_NAME+"."+OrdersEntry._ID);
                columnMap.put(OrdersEntry.COLUMN_ORDER_PRODUCT_ID,OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_PRODUCT_ID);
                columnMap.put(OrdersEntry.COLUMN_ORDER_SUPPLIER_ID,
                        OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_SUPPLIER_ID);
                columnMap.put(OrdersEntry.COLUMN_ORDER_QUANTITY,OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_QUANTITY);
                columnMap.put(OrdersEntry.COLUMN_ORDER_STATUS,OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_STATUS);
                columnMap.put(OrdersEntry.COLUMN_ORDER_TIMESTAMP,OrdersEntry.TABLE_NAME+"."+OrdersEntry.COLUMN_ORDER_TIMESTAMP);
                columnMap.put(OrdersEntry.ALIAS_COLUMN_ORDER_PRODUCT_NAME,
                        ProductEntry.TABLE_NAME+"."+ProductEntry.COLUMN_PRODUCT_NAME
                        +" as " + OrdersEntry.ALIAS_COLUMN_ORDER_PRODUCT_NAME);
                columnMap.put(OrdersEntry.ALIAS_COLUMN_ORDER_SUPPLIER_NAME,
                        SupplierEntry.TABLE_NAME+"."+SupplierEntry.COLUMN_SUPPLIER_NAME
                                + " as " + OrdersEntry.ALIAS_COLUMN_ORDER_SUPPLIER_NAME);

                mQueryBuilder.setTables(OrdersEntry.TABLE_NAME +
                        " LEFT OUTER JOIN " + ProductEntry.TABLE_NAME + " ON " +
                        OrdersEntry.TABLE_NAME + "." + OrdersEntry.COLUMN_ORDER_PRODUCT_ID+ " = " +
                        ProductEntry.TABLE_NAME + "." + ProductEntry._ID +
                        " LEFT OUTER JOIN " + SupplierEntry.TABLE_NAME + " ON " +
                        OrdersEntry.TABLE_NAME + "." + OrdersEntry.COLUMN_ORDER_SUPPLIER_ID+ " = " +
                        SupplierEntry.TABLE_NAME + "." + SupplierEntry._ID
                );
                mQueryBuilder.setProjectionMap(columnMap);
                selection = OrdersEntry.TABLE_NAME + "." + OrdersEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = mQueryBuilder.query(database, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            case SUPPLIERS:
                return insertSupplier(uri, contentValues);
            case SALES:
                return insertSaleEntry(uri, contentValues);
            case ORDERS:
                return insertOrder(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_product_name_manditory));
        }

        if( name.isEmpty()){
            throw new IllegalArgumentException(getContext().getString(R.string.err_product_name_blank));
        }

        Integer quantityInStock = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK);
        if (quantityInStock!=null && quantityInStock < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_product_stock_quantity_invalid));
        }

        Integer quantitySoldOut = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD);
        if (quantitySoldOut!=null && quantitySoldOut < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_product_sold_quantity_invalid));
        }

        Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price!=null && price < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_product_price_invalid));
        }

        /* Integer supplier_id = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_SUPPLIER_ID);
        if (supplier_id!=null && supplier_id < 1) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_product_supplier_invalid));
        }*/

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        long id=database.insert(ProductEntry.TABLE_NAME, null, values);

        if ( id == -1 ){
            return null;
        }

        //Notify all the listeners that data has been changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * Insert a supplier into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertSupplier(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_NAME);
        if (name == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_name_manditory));
        }

        if( name.isEmpty()){
            throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_name_blank));
        }

        // Check that the email is not null
        String mail = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_EMAIL);
        if (mail == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_mail_manditory));
        }

        if( mail.isEmpty()){
            throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_mail_blank));
        }

        if(!SupplierEntry.isValidEmail(mail)){
            throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_mail_invalid));
        }

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        long id=database.insert(SupplierEntry.TABLE_NAME, null, values);
        if ( id == -1 ){
            return null;
        }

        //Notify all the listeners that data has been changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Insert a sale entry into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertSaleEntry(Uri uri, ContentValues values) {

        Integer product_id = values.getAsInteger(SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID);
        if (product_id!=null && product_id < 1) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_sales_product_id_invalid));
        }

        Integer quantity = values.getAsInteger(SalesEntry.COLUMN_SALES_ITEM_QUANTITY);
        if (quantity!=null && quantity < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_sales_quantity_invalid));
        }

        Double price = values.getAsDouble(SalesEntry.COLUMN_SALES_ITEM_PRICE);
        if (price!=null && price < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_sales_price_invalid));
        }

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        long id=database.insert(SalesEntry.TABLE_NAME, null, values);
        if ( id == -1 ){
            return null;
        }

        //Notify all the listeners that data has been changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Insert a sale entry into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertOrder(Uri uri, ContentValues values) {
        Integer product_id = values.getAsInteger(OrdersEntry.COLUMN_ORDER_PRODUCT_ID);
        if (product_id!=null && product_id < 1) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_order_product_id_invalid));
        }

        Integer supplier_id = values.getAsInteger(OrdersEntry.COLUMN_ORDER_PRODUCT_ID);
        if (supplier_id!=null && supplier_id < 1) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_order_supplier_id_invalid));
        }

        Integer quantity = values.getAsInteger(OrdersEntry.COLUMN_ORDER_QUANTITY);
        if (quantity!=null && quantity < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_order_quantity_invalid));
        }

        Integer status = values.getAsInteger(OrdersEntry.COLUMN_ORDER_STATUS);
        if (status == null || !OrdersEntry.isValidStatus(status)) {
            throw new IllegalArgumentException(getContext().getString(R.string.err_order_status_invalid));
        }

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        long id=database.insert(OrdersEntry.TABLE_NAME, null, values);
        if ( id == -1 ){
            return null;
        }

        //Notify all the listeners that data has been changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_QUANTITY_ID:
                // For the SUPPLIER_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProductQuantity(uri, contentValues, selection, selectionArgs);
            case SUPPLIERS:
                return updateSupplier(uri, contentValues, selection, selectionArgs);
            case SUPPLIER_ID:
                // For the SUPPLIER_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateSupplier(uri, contentValues, selection, selectionArgs);
            case SALES:
                return updateSalesEntry(uri, contentValues, selection, selectionArgs);
            case SALE_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateSalesEntry(uri, contentValues, selection, selectionArgs);
            case ORDERS:
                return updateOrder(uri, contentValues, selection, selectionArgs);
            case ORDER_ID:
                // For the SUPPLIER_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateOrder(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ProductEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_product_name_manditory));
            }

            if( name.isEmpty()){
                throw new IllegalArgumentException(getContext().getString(R.string.err_product_name_blank));
            }
        }


        // If the {@link ProductEntry#COLUMN_PRODUCT_QUANTITY_IN_STOCK} key is present,
        // check that the gender value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK)) {
            Integer quantityInStock = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK);
            if (quantityInStock!=null && quantityInStock < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_product_stock_quantity_invalid));
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_QUANTITY_SOLD} key is present,
        // check that the gender value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD)) {
            Integer quantitySoldOut = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD);
            if (quantitySoldOut!=null && quantitySoldOut < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_product_sold_quantity_invalid));
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_PRICE} key is present,
        // check that the weight value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price!=null && price < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_product_price_invalid));
            }
        }

        if (values.size()==0){
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Perform the update on the database and get the number of rows aff
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated > 0){
            //Notify all the listeners that data has been changed for the given content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }


    private int updateProductQuantity(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size()==0){
            return 0;
        }

        Integer stockIncrement;
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK)) {
            stockIncrement = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK);
            if (stockIncrement!=null && stockIncrement <= 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_product_stock_increment_value_invalid));
            }
        }else{
            stockIncrement = 0;
        }

        Integer soldQuantityIncrement ;
        // If the {@link ProductEntry#COLUMN_PRODUCT_QUANTITY_SOLD} key is present,
        // check that the gender value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD)) {
            soldQuantityIncrement = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD);
            if (soldQuantityIncrement!=null && soldQuantityIncrement <= 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_product_sold_increment_value_invalid));
            }
        }else{
            soldQuantityIncrement = 0;
        }

        if (stockIncrement==0 && soldQuantityIncrement==0){
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        String query = "UPDATE " + ProductEntry.TABLE_NAME + " SET "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK + " = " + ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK
                + " + " + stockIncrement + ","
                + ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD + " = " + ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD
                + " + " + soldQuantityIncrement
                + " where " + ProductEntry._ID + " = " + Integer.parseInt(String.valueOf(ContentUris.parseId(uri)));

        // Perform the update on the database and get the number of rows aff
        database.execSQL(query);
        int rowsEffetced = 0;
        Cursor cursor = null;
        try{
            cursor = database.rawQuery("SELECT changes() AS affected_row_count", null);
            if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()){
                rowsEffetced = cursor.getInt(cursor.getColumnIndex("affected_row_count"));
            }
        }
        finally{
            if(cursor != null){
                cursor.close();
            }
        }

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsEffetced > 0){
            //Notify all the listeners that data has been changed for the given content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsEffetced;
    }

    // Get the count of rows effected by last DML statement
    public int getChangesCount() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteStatement statement = db.compileStatement("SELECT changes()");
        return statement.executeUpdateDelete();
    }

    /**
     * Update supplier in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateSupplier(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size()==0){
            return 0;
        }

        // If the {@link SupplierEntry#COLUMN_SUPPLIER_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(SupplierEntry.COLUMN_SUPPLIER_NAME)) {
            String name = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_NAME);
            if (name == null) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_name_manditory));
            }

            if( name.isEmpty()){
                throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_name_blank));
            }
        }

        // If the {@link SupplierEntry#COLUMN_SUPPLIER_EMAIL} key is present,
        // check that the name value is not null.
        if (values.containsKey(SupplierEntry.COLUMN_SUPPLIER_EMAIL)) {
            String mail = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_EMAIL);
            if (mail == null) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_mail_manditory));
            }

            if( mail.isEmpty()){
                throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_mail_blank));
            }

            if(!SupplierEntry.isValidEmail(mail)){
                throw new IllegalArgumentException(getContext().getString(R.string.err_supplier_mail_invalid));
            }
        }
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Perform the update on the database and get the number of rows aff
        int rowsUpdated = database.update(SupplierEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated > 0){
            //Notify all the listeners that data has been changed for the given content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Update sales in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateSalesEntry(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size()==0){
            return 0;
        }

        // If the {@link SalesEntry#COLUMN_SALES_ITEM_QUANTITY} key is present,
        // check that the gender value is valid.
        if (values.containsKey(SalesEntry.COLUMN_SALES_ITEM_QUANTITY)) {
            Integer quantity = values.getAsInteger(SalesEntry.COLUMN_SALES_ITEM_QUANTITY);
            if (quantity!=null && quantity < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_sales_quantity_invalid));
            }
        }

        // If the {@link SalesEntry#COLUMN_SALES_ITEM_PRICE} key is present,
        // check that the weight value is valid.
        if (values.containsKey(SalesEntry.COLUMN_SALES_ITEM_PRICE)) {
            Double price = values.getAsDouble(SalesEntry.COLUMN_SALES_ITEM_PRICE);
            if (price!=null && price < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_sales_price_invalid));
            }
        }
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Perform the update on the database and get the number of rows aff
        int rowsUpdated = database.update(SupplierEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated > 0){
            //Notify all the listeners that data has been changed for the given content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Update orders in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateOrder(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Exit if no vaues to update
        if (values.size()==0){
            return 0;
        }

        // If the {@link OrderEntry#COLUMN_ORDER_QUANTITY} key is present,
        // check that the gender value is valid.
        if (values.containsKey(OrdersEntry.COLUMN_ORDER_QUANTITY)) {
            Integer quantity = values.getAsInteger(OrdersEntry.COLUMN_ORDER_QUANTITY);
            if (quantity!=null && quantity < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_order_quantity_invalid));
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_PRICE} key is present,
        // check that the weight value is valid.
        if (values.containsKey(OrdersEntry.COLUMN_ORDER_STATUS)) {
            Integer status = values.getAsInteger(OrdersEntry.COLUMN_ORDER_STATUS);
            if (status == null || !OrdersEntry.isValidStatus(status)) {
                throw new IllegalArgumentException(getContext().getString(R.string.err_order_status_invalid));
            }
        }

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Perform the update on the database and get the number of rows aff
        int rowsUpdated = database.update(OrdersEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated > 0){
            //Notify all the listeners that data has been changed for the given content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Track the number of rows that were deleted
        int rowsDeleted=0;
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUPPLIERS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(SupplierEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUPPLIER_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SupplierEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SALES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(SalesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SALE_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SalesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ORDERS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(OrdersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ORDER_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(OrdersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted > 0){
            //Notify all the listeners that data has been changed for the pert content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_PRODUCT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_PRODUCT_ITEM_TYPE;
            case SUPPLIERS:
                return SupplierEntry.CONTENT_SUPPLIER_LIST_TYPE;
            case SUPPLIER_ID:
                return SupplierEntry.CONTENT_SUPPLIER_ITEM_TYPE;
            case SALES:
                return SalesEntry.CONTENT_SALES_LIST_TYPE;
            case SALE_ID:
                return SalesEntry.CONTENT_SALES_ITEM_TYPE;
            case ORDER_PRODUCT_SUPPLIER_JOIN:
                return OrdersEntry.CONTENT_ORDER_JOIN_LIST_TYPE;
            case ORDER_PRODUCT_SUPPLIER_JOIN_ID:
                return OrdersEntry.CONTENT_ORDER_JOIN_ITEM_TYPE;
            case ORDERS:
                return OrdersEntry.CONTENT_ORDER_LIST_TYPE;
            case ORDER_ID:
                return OrdersEntry.CONTENT_ORDER_ITEM_TYPE;
            case SALES_PRODUCT_JOIN:
                return SalesEntry.CONTENT_SALES_JOIN_LIST_TYPE;
            case SALES_PRODUCT_JOIN_ID:
                return SalesEntry.CONTENT_SALES_JOIN_ITEM_TYPE;
            case PRODUCT_QUANTITY_ID:
                return ProductEntry.CONTENT_PRODUCT_QUANTITY_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }
}
