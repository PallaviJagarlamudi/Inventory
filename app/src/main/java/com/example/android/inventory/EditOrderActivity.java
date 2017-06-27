package com.example.android.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;
import com.example.android.inventory.data.InventoryContract.OrdersEntry;
import com.example.android.inventory.data.InventoryContract.ProductEntry;

import java.util.ArrayList;

public class EditOrderActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int SINGLE_ORDER_LOADER = 1;

    /**
     * Content URI for the existing pet (null if it's a new pet)
     */
    private Uri mCurrentOrderUri;

    /**
     * Fields to be edited
     */
    private TextView mOrderProductText;
    private TextView mOrderSupplierText;
    private TextView mOrderQuantityText;
    private NDSpinner mStatusSpinner;


    /**
     * Boolean flag that keeps track of whether the order has been edited (true) or not (false)
     */
    private boolean mOrderHasChanged = false;
    private boolean mSpinnerTouched = false;
    private boolean mIsStatusChangeable = false;
    private int mOrderOldStatus = -1;
    private int mOrderCurrentStatus = -1;
    private int mOrderProductId = -1;

    private ArrayAdapter mStatusSpinnerAdapter;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mSpinnerTouched = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order);

        Intent intent = getIntent();
        mCurrentOrderUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mOrderProductText = (TextView) findViewById(R.id.text_order_product_name);
        mOrderSupplierText = (TextView) findViewById(R.id.text_order_supplier_name);
        mOrderQuantityText = (TextView) findViewById(R.id.text_order_quantity);
        mStatusSpinner = (NDSpinner) findViewById(R.id.spinner_status);
        setupSpinner();

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(SINGLE_ORDER_LOADER, null, this);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mStatusSpinner.setOnTouchListener(mTouchListener);
    }
    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        mStatusSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_status_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        mStatusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mStatusSpinner.setAdapter(mStatusSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mSpinnerTouched){
                    String selection = (String) parent.getItemAtPosition(position);
                    if (!TextUtils.isEmpty(selection)) {
                        if (selection.equals(getString(R.string.order_status_ordered))) {
                            mOrderCurrentStatus = OrdersEntry.STATUS_ORDERED;
                        } else if (selection.equals(getString(R.string.order_status_delivered))) {
                            mOrderCurrentStatus = OrdersEntry.STATUS_DELIVERED;
                        } else if (selection.equals(getString(R.string.order_status_cancelled))) {
                            mOrderCurrentStatus = OrdersEntry.STATUS_CANCELLED;
                        }
                    }
                    if(mOrderCurrentStatus == mOrderOldStatus){
                        mOrderHasChanged = false;
                    }else {
                        mOrderHasChanged = true;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mOrderCurrentStatus = -1; // Unknown
            }
        });
    }

    private void saveOrder() {
        if( !mOrderHasChanged){
            Toast.makeText(this, getString(R.string.warn_order_unchnaged),Toast.LENGTH_SHORT).show();
            return;
        }
        int orderQuantity = Integer.parseInt(mOrderQuantityText.getText().toString().trim());
        Uri newProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI_QUANTITY, mOrderProductId);

        // Create a new map of values, where column names are the keys
        ContentValues orderValues = new ContentValues();
        orderValues.put(OrdersEntry.COLUMN_ORDER_STATUS, mOrderCurrentStatus);
        ContentValues orderOldValues = new ContentValues();
        orderOldValues.put(OrdersEntry.COLUMN_ORDER_STATUS, mOrderOldStatus);

        ContentValues productValues = new ContentValues();
        productValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK, orderQuantity);


        if (mCurrentOrderUri != null) {
            /* Set the series of update to  be done
            }*/
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            //Add operation for upadte of order status
            ops.add(ContentProviderOperation.newUpdate(mCurrentOrderUri)
                    .withValues(orderValues)
                    .build());

            //ADd opeation for  update of product quantity
            if(mOrderCurrentStatus == OrdersEntry.STATUS_DELIVERED){
                ops.add(ContentProviderOperation.newUpdate(newProductUri)
                        .withValues(productValues)
                        .build());
            }

            try {
                getContentResolver().applyBatch(InventoryContract.CONTENT_AUTHORITY, ops);
                //the updation was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_order_successful),
                        Toast.LENGTH_SHORT).show();
                setUpReturnIntentData();

            } catch (RemoteException e) {
                Toast.makeText(this, getString(R.string.editor_update_order_failed),
                        Toast.LENGTH_SHORT).show();
            } catch (OperationApplicationException e) {
                Toast.makeText(this, getString(R.string.editor_update_order_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void deleteOrder() {
        if (mCurrentOrderUri != null) {
            // Delete the current pet, returning the number of rows deleted
            int rowsAffected = getContentResolver().delete(mCurrentOrderUri, null, null);
            // Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected == 0) {
                // If the rows deleted is <= 0, then there was an error with deletion.
                Toast.makeText(this, getString(R.string.editor_delete_supplier_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                getContentResolver().notifyChange(mCurrentOrderUri,null);
                // Otherwise, the deletion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_supplier_successful),
                        Toast.LENGTH_SHORT).show();
                setUpReturnIntentData();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (!mIsStatusChangeable) {
            MenuItem menuItem = menu.findItem(R.id.action_save);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Do nothing for now
                saveOrder();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mOrderHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditOrderActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditOrderActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mOrderHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies which columns from the database
        // we care about
        String[] projection = {
                OrdersEntry._ID,
                OrdersEntry.COLUMN_ORDER_PRODUCT_ID,
                OrdersEntry.COLUMN_ORDER_SUPPLIER_ID,
                OrdersEntry.COLUMN_ORDER_QUANTITY,
                OrdersEntry.COLUMN_ORDER_STATUS,
                OrdersEntry.COLUMN_ORDER_TIMESTAMP,
                OrdersEntry.ALIAS_COLUMN_ORDER_PRODUCT_NAME,
                OrdersEntry.ALIAS_COLUMN_ORDER_SUPPLIER_NAME
        };

        Uri uri = ContentUris.withAppendedId(OrdersEntry.CONTENT_URI_JOIN,
                Integer.parseInt(String.valueOf(ContentUris.parseId(mCurrentOrderUri))));

        // This Loader will execute the ContentProvider's query method on background thread.
        return new CursorLoader(this,   //parent activity context
                uri,                    // Provider Content URI to query
                projection,             // The columns to include in resulting cursor
                null,                   // No Selection clause
                null,                   // No Selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Read the pet attributes from the Cursor for the current pet
            String productName = data.getString(data.getColumnIndex(OrdersEntry.ALIAS_COLUMN_ORDER_PRODUCT_NAME));
            String supplierName = data.getString(data.getColumnIndex(OrdersEntry.ALIAS_COLUMN_ORDER_SUPPLIER_NAME));
            int quantity = data.getInt(data.getColumnIndex(OrdersEntry.COLUMN_ORDER_QUANTITY));
            int status = data.getInt(data.getColumnIndex(OrdersEntry.COLUMN_ORDER_STATUS));
            mOrderProductId = data.getInt(data.getColumnIndex(OrdersEntry.COLUMN_ORDER_PRODUCT_ID));

            // Update the TextViews with the attributes for the current pet
            mOrderProductText.setText(productName);
            mOrderSupplierText.setText(supplierName);
            mOrderQuantityText.setText(String.valueOf(quantity));
            switch (status) {
                case OrdersEntry.STATUS_ORDERED:
                    mStatusSpinner.setSelection(mStatusSpinnerAdapter.getPosition(getString(R.string.order_status_ordered)));
                    break;
                case OrdersEntry.STATUS_DELIVERED:
                    mStatusSpinner.setSelection(mStatusSpinnerAdapter.getPosition(getString(R.string.order_status_delivered)));
                    break;
                case OrdersEntry.STATUS_CANCELLED:
                    mStatusSpinner.setSelection(mStatusSpinnerAdapter.getPosition(getString(R.string.order_status_cancelled)));
                    break;
            }
            mOrderCurrentStatus=status;
            mOrderOldStatus=status;
            if(status == OrdersEntry.STATUS_DELIVERED || status == OrdersEntry.STATUS_CANCELLED){
                mIsStatusChangeable = false;
                mStatusSpinner.setEnabled(false);
                invalidateOptionsMenu();
            }else{
                mIsStatusChangeable = true;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Find all relevant views that we will need to read user input from
        mOrderProductText.setText("");
        mOrderSupplierText.setText("");
        mOrderQuantityText.setText("");
        mStatusSpinner.setSelection(1);
        mOrderProductId =-1;
        mIsStatusChangeable = false;
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_supplier_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteOrder();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setUpReturnIntentData(){
        Intent data = new Intent();
        data.putExtra(getString(R.string.cmd_refresh), "");
        setResult(RESULT_OK, data);
        finish();
    }
}
