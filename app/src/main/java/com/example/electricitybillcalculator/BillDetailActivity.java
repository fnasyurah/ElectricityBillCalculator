package com.example.electricitybillcalculator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.text.DecimalFormat;

public class BillDetailActivity extends AppCompatActivity {

    private TextView monthText, unitsText, rebateText, totalChargesText, finalCostText;
    private DBHelper dbHelper;
    private int billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bill Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize database
        dbHelper = new DBHelper(this);

        // Initialize views
        initializeViews();

        // Get bill ID from intent
        billId = getIntent().getIntExtra("bill_id", -1);

        if (billId != -1) {
            displayBillDetails();
        }
    }

    private void initializeViews() {
        monthText = findViewById(R.id.monthText);
        unitsText = findViewById(R.id.unitsText);
        rebateText = findViewById(R.id.rebateText);
        totalChargesText = findViewById(R.id.totalChargesText);
        finalCostText = findViewById(R.id.finalCostText);
    }

    private void displayBillDetails() {
        Bill bill = dbHelper.getBillById(billId);

        if (bill != null) {
            DecimalFormat df = new DecimalFormat("0.00");

            monthText.setText("Month: " + bill.getMonth());
            unitsText.setText("Units Used: " + bill.getUnits() + " kWh");
            rebateText.setText("Rebate: " + bill.getRebate() + "%");
            totalChargesText.setText("Total Charges: RM " + df.format(bill.getTotalCharges()));
            finalCostText.setText("Final Cost: RM " + df.format(bill.getFinalCost()));
        } else {
            // If bill not found, show error
            monthText.setText("Month: Not found");
            unitsText.setText("Units Used: Not found");
            rebateText.setText("Rebate: Not found");
            totalChargesText.setText("Total Charges: Not found");
            finalCostText.setText("Final Cost: Not found");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to BillsActivity
        return true;
    }
}