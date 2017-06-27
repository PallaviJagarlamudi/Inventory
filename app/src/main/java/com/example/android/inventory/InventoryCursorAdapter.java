package com.example.android.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.inventory.data.InventoryContract;
import com.example.android.inventory.data.InventoryContract.OrdersEntry;
import com.example.android.inventory.data.InventoryContract.ProductEntry;
import com.example.android.inventory.data.InventoryContract.SalesEntry;
import com.example.android.inventory.data.InventoryContract.SupplierEntry;

import java.text.NumberFormat;
import java.util.ArrayList;


/**
 * Created by Pallavi J on 21-06-2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private static final int OPTION_PRODUCT = 1;
    private static final int OPTION_SUPPLIER = 2;
    private static final int OPTION_SALES = 3;
    private static final int OPTION_ORDERS = 4;
    private int mOptionId;

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context  The context
     * @param c        The cursor from which to get the data.
     * @param optionId The optionId to diffentiate cursor to process.
     */
    public InventoryCursorAdapter(Context context, Cursor c, int optionId) {
        super(context, c, 0 /* flags */);
        mOptionId = optionId;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (mOptionId == OPTION_PRODUCT) {
            return LayoutInflater.from(context).inflate(R.layout.product_list_item, parent, false);
        } else if (mOptionId == OPTION_SUPPLIER) {
            return LayoutInflater.from(context).inflate(R.layout.supplier_list_item, parent, false);
        } else if (mOptionId == OPTION_SALES) {
            return LayoutInflater.from(context).inflate(R.layout.sales_list_item, parent, false);
        } else if (mOptionId == OPTION_ORDERS) {
            return LayoutInflater.from(context).inflate(R.layout.orders_list_item, parent, false);
        } else {
            throw new IllegalStateException("Unknown curosr type : " + mOptionId);
        }
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        if (mOptionId == OPTION_PRODUCT) {
            // Find individual views that we want to modify in the list item layout
            ImageView imageView = (ImageView) view.findViewById(R.id.product_image);
            TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
            TextView priceTextView = (TextView) view.findViewById(R.id.product_price);
            TextView quantityInStockTextView = (TextView) view.findViewById(R.id.product_quantity_in_Stock);
            TextView quantitySoldTextView = (TextView) view.findViewById(R.id.product_quantity_sold);
            TextView soldOutMsgTextView = (TextView) view.findViewById(R.id.text_warn_sold_out);
            Button salebutton = (Button) view.findViewById(R.id.sale_button);

            // Read the pet attributes from the Cursor for the current pet
            String name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
            String imageUri = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
            final double price = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
            final int quantityInStock = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK));
            final int quantitySoldOut = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD));
            final int productId = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));

            // Update the TextViews with the attributes for the current pet
            nameTextView.setText(name);
            if (imageUri != null) {
                Glide.with(mContext).load(imageUri).centerCrop().into(imageView);
            } else {
                Glide.with(mContext).load(R.drawable.no_image).centerCrop().into(imageView);
            }
            priceTextView.setText(String.valueOf(NumberFormat.getCurrencyInstance().format(price)));
            quantityInStockTextView.setText(String.valueOf(quantityInStock));
            quantitySoldTextView.setText(String.valueOf(quantitySoldOut));
            if (quantityInStock <= 0) {
                salebutton.setVisibility(View.GONE);
                soldOutMsgTextView.setVisibility(View.VISIBLE);
            } else {
                salebutton.setVisibility(View.VISIBLE);
                soldOutMsgTextView.setVisibility(View.GONE);
            }
            salebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quantityInStock <= 0) {
                        return;
                    }
                    ContentResolver resolver = v.getContext().getContentResolver();

                    // Create a new map of values, where column names are the keys
                    ContentValues salesEntryValues = new ContentValues();
                    salesEntryValues.put(SalesEntry.COLUMN_SALES_ITEM_PRODCUT_ID, productId);
                    salesEntryValues.put(SalesEntry.COLUMN_SALES_ITEM_PRICE, price);
                    salesEntryValues.put(SalesEntry.COLUMN_SALES_ITEM_QUANTITY, 1);

                    ContentValues productValues = new ContentValues();
                    productValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK, quantityInStock - 1);
                    productValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD, quantitySoldOut + 1);
                    Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);

                    /*   Set the series of update to  be done */
                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                    //Add operation for inserting of entry in sales table
                    ops.add(ContentProviderOperation.newInsert(SalesEntry.CONTENT_URI)
                            .withValues(salesEntryValues)
                            .build());

                    //Add operation for  update of product quantity
                    ops.add(ContentProviderOperation.newUpdate(currentProductUri)
                            .withValues(productValues)
                            .build());

                    try {
                        resolver.applyBatch(InventoryContract.CONTENT_AUTHORITY, ops);
                        //the updation was successful and we can display a toast.
                        Toast.makeText(context, context.getString(R.string.editor_insert_sale_successful),
                                Toast.LENGTH_SHORT).show();
                        //resolver.notifyChange(ProductEntry.CONTENT_URI, null);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Toast.makeText(context, context.getString(R.string.editor_insert_sale_failed),
                                Toast.LENGTH_SHORT).show();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                        Toast.makeText(context, context.getString(R.string.editor_insert_sale_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else if (mOptionId == OPTION_SUPPLIER) {
            // Find individual views that we want to modify in the list item layout
            TextView nameTextView = (TextView) view.findViewById(R.id.name);
            TextView emailTextView = (TextView) view.findViewById(R.id.email);

            // Read the pet attributes from the Cursor for the current pet
            String name = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_NAME));
            String email = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_EMAIL));

            // Update the TextViews with the attributes for the current pet
            nameTextView.setText(name);
            emailTextView.setText(email);

        } else if (mOptionId == OPTION_SALES) {
            // Find individual views that we want to modify in the list item layout
            TextView productTextView = (TextView) view.findViewById(R.id.sales_entry_product_name);
            TextView priceTextView = (TextView) view.findViewById(R.id.sales_entry_product_price);
            TextView quantityTextView = (TextView) view.findViewById(R.id.sales_entry_sold_quantity);
            TextView timeTextView = (TextView) view.findViewById(R.id.sales_entry_timestamp);

            // Read the pet attributes from the Cursor for the current pet
            String productName = cursor.getString(cursor.getColumnIndex(SalesEntry.ALIAS_COLUMN_SALES_PRODUCT_NAME));
            double price = cursor.getDouble(cursor.getColumnIndex(SalesEntry.COLUMN_SALES_ITEM_PRICE));
            int quantity = cursor.getInt(cursor.getColumnIndex(SalesEntry.COLUMN_SALES_ITEM_QUANTITY));
            String saleTimestamp = cursor.getString(cursor.getColumnIndex(SalesEntry.COLUMN_SALES_ITEM_TIMESTAMP));

            // Update the TextViews with the attributes for the current order
            productTextView.setText(productName);
            priceTextView.setText(String.valueOf(NumberFormat.getCurrencyInstance().format(price)));
            quantityTextView.setText(String.valueOf(quantity));
            timeTextView.setText(saleTimestamp);

        } else if (mOptionId == OPTION_ORDERS) {
            // Find individual views that we want to modify in the list item layout
            TextView productTextView = (TextView) view.findViewById(R.id.order_product_name);
            TextView supplierTextView = (TextView) view.findViewById(R.id.order_supplier_name);
            TextView statusTextView = (TextView) view.findViewById(R.id.order_product_status);
            TextView quantityTextView = (TextView) view.findViewById(R.id.order_quantity);
            TextView timeTextView = (TextView) view.findViewById(R.id.order_timestamp);

            // Read the pet attributes from the Cursor for the current pet
            String productName = cursor.getString(cursor.getColumnIndex(OrdersEntry.ALIAS_COLUMN_ORDER_PRODUCT_NAME));
            String supplierName = cursor.getString(cursor.getColumnIndex(OrdersEntry.ALIAS_COLUMN_ORDER_SUPPLIER_NAME));
            int status = cursor.getInt(cursor.getColumnIndex(OrdersEntry.COLUMN_ORDER_STATUS));
            int quantity = cursor.getInt(cursor.getColumnIndex(OrdersEntry.COLUMN_ORDER_QUANTITY));
            String orderTimestamp = cursor.getString(cursor.getColumnIndex(OrdersEntry.COLUMN_ORDER_TIMESTAMP));

            String statusString = "";
            productTextView.setText(productName);
            switch (status) {
                case OrdersEntry.STATUS_ORDERED:
                    statusString = context.getString(R.string.order_status_ordered);
                    break;
                case OrdersEntry.STATUS_DELIVERED:
                    statusString = context.getString(R.string.order_status_delivered);
                    break;
                case OrdersEntry.STATUS_CANCELLED:
                    statusString = context.getString(R.string.order_status_cancelled);
                    break;
                default:
                    statusString = context.getString(R.string.order_status_unknown);
            }
            // Update the TextViews with the attributes for the current order
            statusTextView.setText(statusString);
            supplierTextView.setText(supplierName);
            quantityTextView.setText(String.valueOf(quantity));
            timeTextView.setText(orderTimestamp);

        } else {
            throw new IllegalStateException("Unknown cursor type : " + mOptionId);
        }
    }
}
