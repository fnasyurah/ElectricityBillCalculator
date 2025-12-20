package com.example.electricitybillcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;

public class BillDetailActivity extends AppCompatActivity {

    private TextView monthText, unitsText, rebateText, totalChargesText, finalCostText;
    private Button editButton, deleteButton;
    private DBHelper dbHelper;
    private int billId;
    private Bill currentBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        // Set ActionBar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📄 Bill Details");
        }

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
            setupButtonListeners();
        }
    }

    private void initializeViews() {
        monthText = findViewById(R.id.monthText);
        unitsText = findViewById(R.id.unitsText);
        rebateText = findViewById(R.id.rebateText);
        totalChargesText = findViewById(R.id.totalChargesText);
        finalCostText = findViewById(R.id.finalCostText);

        // Initialize buttons
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);
    }

    private void displayBillDetails() {
        currentBill = dbHelper.getBillById(billId);

        if (currentBill != null) {
            DecimalFormat df = new DecimalFormat("0.00");

            monthText.setText("Month: " + currentBill.getMonth());
            unitsText.setText("Units Used: " + currentBill.getUnits() + " kWh");
            rebateText.setText("Rebate: " + currentBill.getRebate() + "%");
            totalChargesText.setText("Total Charges: RM " + df.format(currentBill.getTotalCharges()));
            finalCostText.setText("Final Cost: RM " + df.format(currentBill.getFinalCost()));
        } else {
            // If bill not found, show error
            monthText.setText("Month: Not found");
            unitsText.setText("Units Used: Not found");
            rebateText.setText("Rebate: Not found");
            totalChargesText.setText("Total Charges: Not found");
            finalCostText.setText("Final Cost: Not found");
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private void setupButtonListeners() {
        // Edit button - opens MainActivity with data to edit
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBill();
            }
        });

        // Delete button - shows confirmation dialog
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private void editBill() {
        // Pass the bill ID to MainActivity for editing
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("edit_bill_id", billId);  // Pass the bill ID, not individual fields
        startActivity(intent);

        // Close this activity since we're going to edit
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this bill record?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteBill();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteBill() {
        boolean isDeleted = dbHelper.deleteBill(billId);

        if (isDeleted) {
            Toast.makeText(this, "Bill deleted successfully!", Toast.LENGTH_SHORT).show();
            // Go back to BillsActivity
            Intent intent = new Intent(this, BillsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete bill", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to BillsActivity
        return true;
    }
}