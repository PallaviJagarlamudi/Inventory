package com.example.android.inventory;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.SupplierEntry;

public class SupplierActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private InventoryCursorAdapter mCursorAdapter;
    private static final int OPTION_SUPPLIER = 2 ;
    private static final int SUPPLIER_LOADER = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SupplierActivity.this, EditSupplierActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the supplier data
        ListView supplierListView = (ListView) findViewById(R.id.list_supplier);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        supplierListView.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null, OPTION_SUPPLIER);

        //Setup adapter to create list item  for each row in the cursor
        supplierListView.setAdapter(mCursorAdapter);
        supplierListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Uri currentSupplierUri = ContentUris.withAppendedId(SupplierEntry.CONTENT_URI, id);

                Intent intent = new Intent(SupplierActivity.this, EditSupplierActivity.class);
                intent.setData(currentSupplierUri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(SUPPLIER_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_list_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If there are list is null or no items in the list, hide this delete menu item
        if (mCursorAdapter == null || mCursorAdapter.isEmpty()) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_all_entries);
            menuItem.setVisible(false);
        }else{
            MenuItem menuItem = menu.findItem(R.id.action_delete_all_entries);
            menuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // we care about
        String[] projection = {
                SupplierEntry._ID,
                SupplierEntry.COLUMN_SUPPLIER_NAME,
                SupplierEntry.COLUMN_SUPPLIER_EMAIL };

        // This Loader will execute the ContentProvider's query method on background thread.
        return new CursorLoader(this,   //parent activity context
                SupplierEntry.CONTENT_URI,   // Provider Content URI to query
                projection,             // The columns to include in resulting cursor
                null,                   // No Selection clause
                null,                   // No Selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(data);
        invalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_suppliers_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the supplier.
                deleteAllSuppliers();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the suppplier.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllSuppliers() {
        if (mCursorAdapter != null && !mCursorAdapter.isEmpty()) {
            // Delete the current pet, returning the number of rows deleted
            int rowsAffected = getContentResolver().delete(SupplierEntry.CONTENT_URI, null, null);
            // Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected == 0) {
                // If the rows deleted is <= 0, then there was an error with deletion.
                Toast.makeText(this, getString(R.string.editor_delete_supplier_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the deletion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_all_suppliers_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            // Otherwise, the deletion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.warn_nothing_to_clear),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
