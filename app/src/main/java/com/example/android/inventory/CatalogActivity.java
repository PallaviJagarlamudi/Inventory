package com.example.android.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class CatalogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        View stockOption = findViewById(R.id.option_inventory);
        stockOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CatalogActivity.this, ProductActivity.class);
                startActivity(i);
            }
        });

        View suppliersOption = findViewById(R.id.option_suppliers);
        suppliersOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CatalogActivity.this, SupplierActivity.class);
                startActivity(i);
            }
        });

        View salesOption = findViewById(R.id.option_sales);
        salesOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CatalogActivity.this, SalesActivity.class);
                startActivity(i);
            }
        });

        View ordersOption = findViewById(R.id.option_orders);
        ordersOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CatalogActivity.this, OrdersActivity.class);
                startActivity(i);
            }
        });
    }
}
