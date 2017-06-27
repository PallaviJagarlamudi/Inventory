package com.example.android.inventory;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.inventory.data.InventoryContract.OrdersEntry;
import com.example.android.inventory.data.InventoryContract.ProductEntry;
import com.example.android.inventory.data.InventoryContract.SupplierEntry;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static com.example.android.inventory.ImageUtils.PICK_IMAGE_REQUEST;
import static com.example.android.inventory.ImageUtils.REQUEST_IMAGE_CAPTURE;
import static com.example.android.inventory.ImageUtils.getBitmapFromUri;


public class EditProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int SINGLE_PRODUCT_LOADER = 1;
    private static final int SUPPLIER_LOADER = 2;
    private static final int M_MODE_INSERT = 1;
    private static final int M_MODE_ORDER = 2;
    private static final int M_MODE_EDIT = 3;
    private static final int MAX_QUANTITY = 20;
    private static final int VALUE_ZERO = 0;
    private static final int MIN_QUANTITY = 0;
    private static final int MAX_DECIMAL_PLACES = 2;

    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int SEND_MAIL_REQUEST = 100;

    /**
     * Content URI for the existing pet (null if it's a new pet)
     */
    private Uri mCurrentProductUri;

    /**
     * Fields of product editor
     */
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mSoldOutQuantityEditText;
    private EditText mRestockQuantityEditText;
    private NDSpinner mSupplierSpinner;
    private Button mOrderButton;
    private Button mIncrementButton;
    private Button mDecrementButton;
    private TextView mStockQuantityTextView;
    private View mOrderContainer;
    private View mPicFabContainer;
    private ImageView mProductImage;

    private ImageButton mFabTakePic;
    private ImageButton mFabSelectPic;


    private SimpleCursorAdapter mSupplierAdapter;
    private int mSupplierId = 0;
    private String mSupplierName = "";
    private String mSupplierEmail = "";

    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;
    private boolean mOrderDataHasChanged = false;
    private boolean mSpinnerTouched = false;
    private Uri mImageUri;
    private Uri mTempUri;
    private Bitmap mBitmap;
    private boolean mIsGalleryPicture = false;

    /**
     * Boolean flag that keeps track of whether the product editor is in edit or order mode
     */
    private int mCurrentMode;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            long viewId = view.getId();
            if (mCurrentMode == M_MODE_EDIT || mCurrentMode == M_MODE_INSERT) {
                if (viewId == R.id.edit_product_name || viewId == R.id.edit_product_price
                        || viewId == R.id.edit_product_sold_quantity) {
                    mProductHasChanged = true;
                }
            } else if (mCurrentMode == M_MODE_ORDER) {
                if (viewId == R.id.edit_product_restock_quantity || viewId == R.id.spinner_supplier) {
                    mOrderDataHasChanged = true;
                }
                if (viewId == R.id.spinner_supplier) {
                    mSpinnerTouched = true;
                }
            }
            return false;
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.inc_button:
                    incrementQuantity();
                    break;
                case R.id.dec_button:
                    decrementQuantity();
                    break;
                case R.id.action_order:
                    placeOrder();
                    break;
                case R.id.fab_take_pic:
                    takePicture();
                    break;
                case R.id.fab_select_pic:
                    openImageSelector();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSoldOutQuantityEditText = (EditText) findViewById(R.id.edit_product_sold_quantity);
        mRestockQuantityEditText = (EditText) findViewById(R.id.edit_product_restock_quantity);
        mStockQuantityTextView = (TextView) findViewById(R.id.product_quantity_in_stock);
        mSupplierSpinner = (NDSpinner) findViewById(R.id.spinner_supplier);
        mIncrementButton = (Button) findViewById(R.id.inc_button);
        mDecrementButton = (Button) findViewById(R.id.dec_button);
        mOrderButton = (Button) findViewById(R.id.action_order);
        mOrderContainer = findViewById(R.id.container_order);
        mPicFabContainer = findViewById(R.id.pic_fab_controls_container);
        mProductImage = (ImageView) findViewById(R.id.product_image);
        mFabSelectPic = (ImageButton) findViewById(R.id.fab_select_pic);
        mFabTakePic = (ImageButton) findViewById(R.id.fab_take_pic);

        TextView currencyText = (TextView) findViewById(R.id.text_currency_symbol);
        currencyText.setText(Currency.getInstance(Locale.getDefault()).getSymbol());

        if (mCurrentProductUri == null) {
            setToInsertMode();
        } else {
            setupSpinner();
            setToOrderMode();
        }

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSoldOutQuantityEditText.setOnTouchListener(mTouchListener);
        mIncrementButton.setOnClickListener(mOnClickListener);
        mDecrementButton.setOnClickListener(mOnClickListener);
        mFabSelectPic.setOnClickListener(mOnClickListener);
        mFabTakePic.setOnClickListener(mOnClickListener);

        mSupplierSpinner.setOnTouchListener(mTouchListener);
        mRestockQuantityEditText.setOnTouchListener(mTouchListener);
        mOrderButton.setOnClickListener(mOnClickListener);

    }

    private void setToOrderMode() {
        mProductHasChanged = false;
        mOrderDataHasChanged = false;
        mSpinnerTouched = false;
        mCurrentMode = M_MODE_ORDER;
        setTitle(getString(R.string.editor_activity_title_order_product));
        mOrderContainer.setVisibility(View.VISIBLE);
        mPicFabContainer.setVisibility(View.GONE);
        mIncrementButton.setEnabled(false);
        mDecrementButton.setEnabled(false);
        mNameEditText.setEnabled(false);
        mPriceEditText.setEnabled(false);
        mSoldOutQuantityEditText.setEnabled(false);
        // mSoldOutQuantityEditText.setFocusable(false);

        getSupportLoaderManager().initLoader(SINGLE_PRODUCT_LOADER, null, this);
        invalidateOptionsMenu();
    }

    private void setToEditMode() {
        mCurrentMode = M_MODE_EDIT;
        setTitle(getString(R.string.editor_activity_title_edit_product));
        mOrderContainer.setVisibility(View.GONE);
        mIncrementButton.setEnabled(true);
        mDecrementButton.setEnabled(true);
        mNameEditText.setEnabled(true);
        mPriceEditText.setEnabled(true);
        mPicFabContainer.setVisibility(View.GONE);
        mSoldOutQuantityEditText.setEnabled(true);
        mSoldOutQuantityEditText.setFocusable(true);
        requestPermissions();

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(SINGLE_PRODUCT_LOADER, null, this);

        // Invalidate the options menu, so the "Delete" menu option can be hidden.
        // (It doesn't make sense to delete a pet that hasn't been created yet.)
        invalidateOptionsMenu();
    }

    private void setToInsertMode() {
        mCurrentMode = M_MODE_INSERT;
        mPicFabContainer.setVisibility(View.GONE);
        setTitle(getString(R.string.editor_activity_title_new_product));
        mOrderContainer.setVisibility(View.GONE);
        mIncrementButton.setEnabled(true);
        mDecrementButton.setEnabled(true);
        mNameEditText.setEnabled(true);
        mPriceEditText.setEnabled(true);
        mStockQuantityTextView.setText(String.valueOf(MIN_QUANTITY));
        mSoldOutQuantityEditText.setEnabled(false);
        mSoldOutQuantityEditText.setText(String.valueOf(VALUE_ZERO));
        requestPermissions();

        // Invalidate the options menu, so the "Delete" menu option can be hidden.
        // (It doesn't make sense to delete a pet that hasn't been created yet.)
        invalidateOptionsMenu();
    }

    /**
     * This method increment the given quantity value.
     */
    public void incrementQuantity() {
        int stockAvalable = Integer.parseInt(mStockQuantityTextView.getText().toString());
        if (stockAvalable == MAX_QUANTITY) {
            Toast.makeText(this, getString(R.string.warn_max_limit), Toast.LENGTH_SHORT).show();
        } else {
            stockAvalable++;
            mProductHasChanged = true;
        }
        mStockQuantityTextView.setText(String.valueOf(stockAvalable));
    }

    /**
     * This method decrement the given quantity value.
     */
    public void decrementQuantity() {
        int stockAvalable = Integer.parseInt(mStockQuantityTextView.getText().toString());
        if (stockAvalable == MIN_QUANTITY) {
            Toast.makeText(this, getString(R.string.warn_min_limit), Toast.LENGTH_SHORT).show();
        } else {
            stockAvalable--;
            mProductHasChanged = true;
        }
        mStockQuantityTextView.setText(String.valueOf(stockAvalable));
    }


    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        getSupportLoaderManager().initLoader(SUPPLIER_LOADER, null, this);

        String[] projection = {
                SupplierEntry._ID,
                SupplierEntry.COLUMN_SUPPLIER_NAME};

        String[] adapterCols = new String[]{SupplierEntry.COLUMN_SUPPLIER_NAME};
        int[] adapterRowViews = new int[]{android.R.id.text1};

        mSupplierAdapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_item, null, adapterCols, adapterRowViews, 0);
        mSupplierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSupplierSpinner.setAdapter(mSupplierAdapter);

        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mSpinnerTouched) {
                    Cursor data = (Cursor) parent.getSelectedItem();
                    if (data != null) {
                        mSupplierId = data.getInt(data.getColumnIndex(SupplierEntry._ID));
                        mSupplierName = data.getString(data.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_NAME));
                        mSupplierEmail = data.getString(data.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_EMAIL));
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplierName = "";
                mSupplierEmail = "";
                mSupplierId = 0; // Unknown
            }
        });
    }

    private void saveProduct() {

        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String stockString = mStockQuantityTextView.getText().toString().trim();
        String soldString = mSoldOutQuantityEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.err_product_name_blank), Toast.LENGTH_SHORT).show();
            return;
        }

        int stockQunatity = Integer.parseInt(stockString);
        double price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            if (hasCorrectPrecision(priceString)) {
                price = Double.parseDouble(priceString);
            } else {
                Toast.makeText(this, getString(R.string.warn_truncate, MAX_DECIMAL_PLACES), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, getString(R.string.warn_price_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        int soldOutQuanity = 0;
        if (!TextUtils.isEmpty(soldString)) {
            soldOutQuanity = Integer.parseInt(soldString);
            if (mCurrentMode == M_MODE_INSERT && stockQunatity < soldOutQuanity) {
                Toast.makeText(this, getString(R.string.warn_sold_gt_stock), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, getString(R.string.warn_quantity_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(soldString) &&
                (stockQunatity < MIN_QUANTITY || stockQunatity > MAX_QUANTITY)) {
            Toast.makeText(this, getString(R.string.warn_fill_all_fields_properly), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK, stockQunatity);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD, soldOutQuanity);
        if(mImageUri != null){
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUri.toString());
        }

        if (mCurrentProductUri == null) {
            Uri newUri = null;
            // Insert the new row, returning the primary key value of the new row
            try {
                newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion..
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            int rowsAffected = 0;
            // Update the current pet, returning the number of rows updated
            try {
                rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            // Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected == 0) {
                // If the rows updated is <= 0, then there was an error with updation.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the updation was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
                setToOrderMode();
            }
        }
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            // Delete the current pet, returning the number of rows deleted
            int rowsAffected = getContentResolver().delete(mCurrentProductUri, null, null);
            // Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected == 0) {
                // If the rows deleted is <= 0, then there was an error with deletion.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the deletion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_product_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem editMenuItem = menu.findItem(R.id.action_edit);
        MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
        MenuItem saveMenuItem = menu.findItem(R.id.action_save);

        if (mCurrentMode == M_MODE_INSERT) {
            editMenuItem.setVisible(false);
            deleteMenuItem.setVisible(false);
            saveMenuItem.setVisible(true);
        } else {
            if (mCurrentMode == M_MODE_EDIT) {
                editMenuItem.setVisible(false);
                deleteMenuItem.setVisible(false);
                saveMenuItem.setVisible(true);
            } else {
                saveMenuItem.setVisible(false);
                editMenuItem.setVisible(true);
                deleteMenuItem.setVisible(true);
            }
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
                saveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_edit:
                setToEditMode();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if ((mCurrentMode == M_MODE_ORDER && !mOrderDataHasChanged) ||
                        ((mCurrentMode == M_MODE_INSERT || mCurrentMode == M_MODE_EDIT) && !mProductHasChanged)) {
                    if (mCurrentMode == M_MODE_EDIT && !mProductHasChanged) {
                        setToOrderMode();
                    } else {
                        NavUtils.navigateUpFromSameTask(EditProductActivity.this);
                    }
                    return true;
                } else if (mCurrentMode == M_MODE_ORDER && mOrderDataHasChanged) {
                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    NavUtils.navigateUpFromSameTask(EditProductActivity.this);
                                }
                            };

                    // Show a dialog that notifies the user they have unsaved changes
                    showExitOrderingDialog(discardButtonClickListener);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mCurrentMode == M_MODE_INSERT) {
                                    // User clicked "Discard" button, navigate to parent activity.
                                    NavUtils.navigateUpFromSameTask(EditProductActivity.this);
                                } else {
                                    setToOrderMode();
                                }
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
        // If the product hasn't changed, continue with handling back button press
        if (mCurrentMode == M_MODE_ORDER && !mOrderDataHasChanged ||
                ((mCurrentMode == M_MODE_INSERT || mCurrentMode == M_MODE_EDIT) && !mProductHasChanged)) {
            if (mCurrentMode == M_MODE_EDIT && !mProductHasChanged) {
                setToOrderMode();
            } else {
                super.onBackPressed();
            }
            return;
        } else if (mCurrentMode == M_MODE_ORDER && mOrderDataHasChanged) {
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    };

            // Show a dialog that notifies the user they have unsaved changes
            showExitOrderingDialog(discardButtonClickListener);
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        if (mCurrentMode == M_MODE_INSERT) {
                            finish();
                        } else {
                            setToOrderMode();
                        }
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == SINGLE_PRODUCT_LOADER) {
            // Define a projection that specifies which columns from the database
            // we care about
            String[] projection = {
                    ProductEntry._ID,
                    ProductEntry.COLUMN_PRODUCT_NAME,
                    ProductEntry.COLUMN_PRODUCT_IMAGE,
                    ProductEntry.COLUMN_PRODUCT_PRICE,
                    ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK,
                    ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD
            };

            // This Loader will execute the ContentProvider's query method on background thread.
            return new CursorLoader(this,   //parent activity context
                    mCurrentProductUri,   // Provider Content URI to query
                    projection,             // The columns to include in resulting cursor
                    null,                   // No Selection clause
                    null,                   // No Selection arguments
                    null);                  // Default sort order
        } else if (id == SUPPLIER_LOADER) {

            // Define a projection that specifies which columns from the database
            // we care about
            String[] projection = {
                    SupplierEntry._ID,
                    SupplierEntry.COLUMN_SUPPLIER_NAME,
                    SupplierEntry.COLUMN_SUPPLIER_EMAIL
            };

            // This Loader will execute the ContentProvider's query method on background thread.
            return new CursorLoader(this,   //parent activity context
                    SupplierEntry.CONTENT_URI,   // Provider Content URI to query
                    projection,             // The columns to include in resulting cursor
                    null,                   // No Selection clause
                    null,                   // No Selection arguments
                    null);                  // Default sort order
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (loaderId == SINGLE_PRODUCT_LOADER) {
            // Proceed with moving to the first row of the cursor and reading data from it
            // (This should be the only row in the cursor)
            if (data.moveToFirst()) {
                // Read the pet attributes from the Cursor for the current pet
                String name = data.getString(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
                String imageUriString = data.getString(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
                if ( imageUriString == null){
                    mImageUri = null;
                }else{
                    if(imageUriString.trim().isEmpty()){
                        mImageUri = null;
                    }else{
                        mImageUri = Uri.parse(imageUriString);
                    }
                }
                Double price = data.getDouble(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
                Integer stockQuantity = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY_IN_STOCK));
                Integer soldQuantity = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY_SOLD));

                // Update the TextViews with the attributes for the current pet
                mNameEditText.setText(name);
                mPriceEditText.setText(new DecimalFormat("0.00").format(price));
                mStockQuantityTextView.setText(String.valueOf(stockQuantity));
                mSoldOutQuantityEditText.setText(String.valueOf(soldQuantity));
                if (mImageUri != null) {
                    Glide.with(this).load(mImageUri).centerCrop().into(mProductImage);
                } else {
                    Glide.with(this).load(R.drawable.no_image).centerCrop().into(mProductImage);
                }
            }
        } else if (loaderId == SUPPLIER_LOADER) {
            mSupplierAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int loaderId = loader.getId();
        if (loaderId == SINGLE_PRODUCT_LOADER) {
            // Find all relevant views that we will need to read user input from
            mNameEditText.setText("");
            mPriceEditText.setText("");
            mStockQuantityTextView.setText(String.valueOf(0));
            mSoldOutQuantityEditText.setText("");
        } else if (loaderId == SUPPLIER_LOADER) {
            mSupplierAdapter.swapCursor(null);
        }
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

    private void showExitOrderingDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.quit_from_order);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.stay_here, new DialogInterface.OnClickListener() {
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
        builder.setMessage(R.string.delete_product_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
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

    private boolean hasCorrectPrecision(String data) {
        int indexOFdec = data.indexOf(".");
        if (indexOFdec >= 0) {
            if (data.substring(indexOFdec).length() > (MAX_DECIMAL_PLACES + 1)) {
                return false;
            }
        }
        return true;
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            mTempUri = null;
            File f = ImageUtils.createImageFile(this);

            mTempUri = FileProvider.getUriForFile(
                    this, ImageUtils.FILE_PROVIDER_AUTHORITY, f);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTempUri);

            // Solution taken from http://stackoverflow.com/a/18332000/3346625
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, mTempUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, getString(R.string.warn_no_camera_app), Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            Uri uri = null;

            if (resultData != null) {
                uri = resultData.getData();

                mImageUri = uri;
                mProductHasChanged = true;
                mBitmap = getBitmapFromUri(this, uri);
                mProductImage.setImageBitmap(mBitmap);
                mIsGalleryPicture = true;
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            mImageUri = mTempUri;
            mBitmap = getBitmapFromUri(this, mTempUri);
            mProductImage.setImageBitmap(mBitmap);
            mIsGalleryPicture = false;
            mProductHasChanged = true;
        } else if (requestCode == SEND_MAIL_REQUEST ) {
            Toast.makeText(this, getString(R.string.editor_insert_order_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void requestPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            mPicFabContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the  task you need to do.
                    mPicFabContainer.setVisibility(View.VISIBLE);
                } else {
                    mPicFabContainer.setVisibility(View.GONE);
                }
                return;
            }
        }
    }

    private void placeOrder() {
        int restockQuantity = 0;
        String restockQuantityString = mRestockQuantityEditText.getText().toString().trim();

        if (mSupplierId == 0) {
            Toast.makeText(this, getString(R.string.warn_supplier_not_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(restockQuantityString)) {
            Toast.makeText(this, getString(R.string.err_order_quantity_blank), Toast.LENGTH_SHORT).show();
            return;
        } else {
            restockQuantity = Integer.parseInt(restockQuantityString);
            if (restockQuantity == VALUE_ZERO) {
                Toast.makeText(this, getString(R.string.err_order_quantity_zero), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String productName = mNameEditText.getText().toString().trim();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(OrdersEntry.COLUMN_ORDER_PRODUCT_ID, Integer.parseInt(String.valueOf(ContentUris.parseId(mCurrentProductUri))));
        values.put(OrdersEntry.COLUMN_ORDER_QUANTITY, restockQuantity);
        values.put(OrdersEntry.COLUMN_ORDER_SUPPLIER_ID, mSupplierId);
        values.put(OrdersEntry.COLUMN_ORDER_STATUS, OrdersEntry.STATUS_ORDERED);
        Uri newUri = null;
        // Insert the new row, returning the primary key value of the new row
        try {
            newUri = getContentResolver().insert(OrdersEntry.CONTENT_URI, values);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion..
            Toast.makeText(this, getString(R.string.editor_insert_order_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast
            StringBuilder summary = new StringBuilder();
            summary.append(getString(R.string.mail_endearment, mSupplierName));
            summary.append("\n\n" + getString(R.string.mail_summary_overview_line));
            summary.append("\n" + getString(R.string.mail_summary_item_name, productName));
            summary.append("\n" + getString(R.string.mail_summary_item_quantity, restockQuantity));
            summary.append("\n" + getString(R.string.thank_you));
            summary.append("\n\n" + getString(R.string.mail_footer));

            sendEmail(new String[]{mSupplierEmail}, getString(R.string.mail_subject, productName),
                    new String(summary), mImageUri);
        }
    }

    private void sendEmail(String[] addresses, String subject, String body, Uri attachment) {
        Intent emailIntent = ShareCompat.IntentBuilder.from(this)
                .setSubject(subject)
                .setEmailTo(addresses)
                .setText(body)
                .setType("message/rfc822")
                .getIntent();

        if (attachment != null) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, attachment);
            emailIntent.setData(attachment);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (Build.VERSION.SDK_INT < 21) {
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            } else {
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }
        }

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            //startActivity(emailIntent);
            startActivityForResult(Intent.createChooser(emailIntent, "Send mail using"), SEND_MAIL_REQUEST);
        } else {
            Toast.makeText(this, getString(R.string.err_no_mail_apps) ,
                Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}

