package com.example.electricitybillcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;

public class BillsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private BillAdapter adapter;
    private ArrayList<Bill> billList;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills);

        // Set ActionBar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📋 Saved Bills");
        }

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Saved Bills");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);

        // Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize database
        dbHelper = new DBHelper(this);

        // Load bills
        loadBills();
    }

    private void loadBills() {
        billList = dbHelper.getAllBills();

        if (billList == null || billList.isEmpty()) {
            // Show empty message
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            // Show bills list
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Create and set adapter
            adapter = new BillAdapter(billList, new BillAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Bill bill) {
                    // Open bill details
                    Intent intent = new Intent(BillsActivity.this, BillDetailActivity.class);
                    intent.putExtra("bill_id", bill.getId());
                    startActivity(intent);
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning to this activity
        loadBills();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}