package com.example.android.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.SupplierEntry;

public class EditSupplierActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the pet data loader */
    private static final int SINGLE_SUPPLIER_LOADER = 1;

    /** Content URI for the existing pet (null if it's a new pet) */
    private Uri mCurrentSupplierUri;

    /**
     * EditText field to enter the supplier's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the supplier EMAIL
     */
    private EditText mEmailEditText;


    /** Boolean flag that keeps track of whether the supplied has been edited (true) or not (false) */
    private boolean mSuppliedHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mSuppliedHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_supplier);

        Intent intent = getIntent();
        mCurrentSupplierUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);

        if (mCurrentSupplierUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_supplier));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_supplier));

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getSupportLoaderManager().initLoader(SINGLE_SUPPLIER_LOADER, null, this);
        }

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
    }

    private void saveSupplier() {

        String nameString = mNameEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(SupplierEntry.COLUMN_SUPPLIER_NAME, nameString);
        values.put(SupplierEntry.COLUMN_SUPPLIER_EMAIL, emailString);

        if(mCurrentSupplierUri == null ){
            Uri newUri = null;
            // Insert the new row, returning the primary key value of the new row
            try{
                newUri = getContentResolver().insert(SupplierEntry.CONTENT_URI, values);
            }catch (IllegalArgumentException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion..
                Toast.makeText(this, getString(R.string.editor_insert_supplier_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast
                Toast.makeText(this, getString(R.string.editor_insert_supplier_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }else{
            int rowsAffected=0;
            // Update the current pet, returning the number of rows updated
            try{
                rowsAffected = getContentResolver().update(mCurrentSupplierUri, values, null, null);
            }catch (IllegalArgumentException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            // Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected == 0) {
                // If the rows updated is <= 0, then there was an error with updation.
                Toast.makeText(this, getString(R.string.editor_update_supplier_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the updation was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_supplier_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void deleteSupplier() {
        if (mCurrentSupplierUri != null) {
            // Delete the current pet, returning the number of rows deleted
            int rowsAffected = getContentResolver().delete(mCurrentSupplierUri, null, null);
            // Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected == 0) {
                // If the rows deleted is <= 0, then there was an error with deletion.
                Toast.makeText(this, getString(R.string.editor_delete_supplier_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the deletion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_supplier_successful),
                        Toast.LENGTH_SHORT).show();
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
        if (mCurrentSupplierUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
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
                saveSupplier();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mSuppliedHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditSupplierActivity.this);
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
                                NavUtils.navigateUpFromSameTask(EditSupplierActivity.this);
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
        if (!mSuppliedHasChanged) {
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
                SupplierEntry._ID,
                SupplierEntry.COLUMN_SUPPLIER_NAME,
                SupplierEntry.COLUMN_SUPPLIER_EMAIL};

        // This Loader will execute the ContentProvider's query method on background thread.
        return new CursorLoader(this,   //parent activity context
                mCurrentSupplierUri,   // Provider Content URI to query
                projection,             // The columns to include in resulting cursor
                null,                   // No Selection clause
                null,                   // No Selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if ( data == null || data.getCount() != 1){
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {

            // Read the pet attributes from the Cursor for the current pet
            String name = data.getString(data.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_NAME));
            String breed = data.getString(data.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_EMAIL));
            // Update the TextViews with the attributes for the current pet
            mNameEditText.setText(name);
            mEmailEditText.setText(breed);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Find all relevant views that we will need to read user input from
        mNameEditText.setText("");
        mEmailEditText.setText("");
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
                deleteSupplier();
                finish();
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
}
