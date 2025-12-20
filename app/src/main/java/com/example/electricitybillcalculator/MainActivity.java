package com.example.electricitybillcalculator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private Spinner monthSpinner;
    private EditText unitsEditText;
    private SeekBar rebateSeekBar;
    private TextView rebateValue, totalChargesText, finalCostText;
    private Button calculateButton, saveButton, viewBillsButton;

    private double totalCharges = 0;
    private double finalCost = 0;
    private double rebatePercentage = 0;

    private DBHelper dbHelper;

    // Variables for edit mode
    private boolean isEditMode = false;
    private int editBillId = -1;

    // Constants for validation
    private static final double MIN_UNITS = 1.0;
    private static final double MAX_UNITS = 1000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set ActionBar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("⚡ Electricity Bill Calculator");
        }

        // Initialize database
        dbHelper = new DBHelper(this);

        initializeViews();
        setupMonthSpinner();
        setupListeners();

        // Check if we're in edit mode
        checkEditMode();
    }

    private void initializeViews() {
        monthSpinner = findViewById(R.id.monthSpinner);
        unitsEditText = findViewById(R.id.unitsEditText);
        rebateSeekBar = findViewById(R.id.rebateSeekBar);
        rebateValue = findViewById(R.id.rebateValue);
        totalChargesText = findViewById(R.id.totalChargesText);
        finalCostText = findViewById(R.id.finalCostText);

        calculateButton = findViewById(R.id.calculateButton);
        saveButton = findViewById(R.id.saveButton);
        viewBillsButton = findViewById(R.id.viewBillsButton);

        saveButton.setEnabled(false);
    }

    private void setupMonthSpinner() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        rebateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rebatePercentage = progress;
                rebateValue.setText(progress + "%");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        calculateButton.setOnClickListener(v -> calculateBill());
        saveButton.setOnClickListener(v -> {
            if (isEditMode) {
                updateBillInDatabase();
            } else {
                saveToDatabase();
            }
        });

        // View Bills button only available in normal mode
        viewBillsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BillsActivity.class);
            startActivity(intent);
        });
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null) {
            isEditMode = intent.getBooleanExtra("edit_mode", false);
            editBillId = intent.getIntExtra("edit_bill_id", -1);

            if (isEditMode && editBillId != -1) {
                // We're in edit mode, load the bill data
                loadBillForEditing(editBillId);
            }
        }
    }

    private void loadBillForEditing(int billId) {
        Bill bill = dbHelper.getBillById(billId);

        if (bill != null) {
            // Update ActionBar title for edit mode
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("✏️ Edit Bill");
            }

            // Hide View Bills button in edit mode
            viewBillsButton.setVisibility(View.GONE);

            // Set spinner to the correct month
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) monthSpinner.getAdapter();
            int position = adapter.getPosition(bill.getMonth());
            if (position >= 0) {
                monthSpinner.setSelection(position);
            }

            // Set units
            unitsEditText.setText(String.valueOf(bill.getUnits()));

            // Set rebate
            rebatePercentage = bill.getRebate();
            rebateSeekBar.setProgress((int) rebatePercentage);
            rebateValue.setText(bill.getRebate() + "%");

            // Calculate and display results
            totalCharges = bill.getTotalCharges();
            finalCost = bill.getFinalCost();

            DecimalFormat df = new DecimalFormat("0.00");
            totalChargesText.setText("Total Charges: RM " + df.format(totalCharges));
            finalCostText.setText("Final Cost: RM " + df.format(finalCost));

            // Enable save button and change text
            saveButton.setEnabled(true);
            saveButton.setText("Update Bill");
            saveButton.setBackgroundTintList(getResources().getColorStateList(R.color.accent_color));

            // Show toast message
            Toast.makeText(this, "Editing bill for " + bill.getMonth(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Bill not found!", Toast.LENGTH_SHORT).show();
            finish(); // Go back if bill not found
        }
    }

    private void calculateBill() {
        String unitsStr = unitsEditText.getText().toString().trim();

        // Validation - Check if empty
        if (unitsStr.isEmpty()) {
            unitsEditText.setError("Please enter electricity units");
            unitsEditText.requestFocus();
            return;
        }

        double units;
        try {
            units = Double.parseDouble(unitsStr);
        } catch (NumberFormatException e) {
            unitsEditText.setError("Please enter a valid number");
            unitsEditText.requestFocus();
            return;
        }

        // Validation - Check min and max units
        if (units < MIN_UNITS) {
            unitsEditText.setError("Units must be at least " + MIN_UNITS + " kWh");
            unitsEditText.requestFocus();
            return;
        }

        if (units > MAX_UNITS) {
            unitsEditText.setError("Units cannot exceed " + MAX_UNITS + " kWh");
            unitsEditText.requestFocus();
            return;
        }

        // Calculate based on tariff blocks
        totalCharges = calculateTariff(units);
        double rebateAmount = totalCharges * (rebatePercentage / 100);
        finalCost = totalCharges - rebateAmount;

        // Display results
        DecimalFormat df = new DecimalFormat("0.00");
        totalChargesText.setText("Total Charges: RM " + df.format(totalCharges));
        finalCostText.setText("Final Cost: RM " + df.format(finalCost));

        saveButton.setEnabled(true);
    }

    private double calculateTariff(double units) {
        double charges = 0;
        double remainingUnits = units;

        // First 200 kWh (1-200) = 21.8 sen/kWh = 0.218 RM/kWh
        if (remainingUnits > 200) {
            charges += 200 * 0.218;
            remainingUnits -= 200;
        } else {
            charges += remainingUnits * 0.218;
            remainingUnits = 0;
        }

        // Next 100 kWh (201-300) = 33.4 sen/kWh = 0.334 RM/kWh
        if (remainingUnits > 100) {
            charges += 100 * 0.334;
            remainingUnits -= 100;
        } else if (remainingUnits > 0) {
            charges += remainingUnits * 0.334;
            remainingUnits = 0;
        }

        // Next 300 kWh (301-600) = 51.6 sen/kWh = 0.516 RM/kWh
        if (remainingUnits > 300) {
            charges += 300 * 0.516;
            remainingUnits -= 300;
        } else if (remainingUnits > 0) {
            charges += remainingUnits * 0.516;
            remainingUnits = 0;
        }

        // Beyond 600 kWh = 54.6 sen/kWh = 0.546 RM/kWh
        if (remainingUnits > 0) {
            charges += remainingUnits * 0.546;
        }

        return charges;
    }

    private void saveToDatabase() {
        String month = monthSpinner.getSelectedItem().toString();
        String unitsStr = unitsEditText.getText().toString().trim();

        if (unitsStr.isEmpty() || totalCharges == 0) {
            Toast.makeText(this, "Please calculate bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        double units;
        try {
            units = Double.parseDouble(unitsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid units value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate again before saving
        if (units < MIN_UNITS || units > MAX_UNITS) {
            Toast.makeText(this, "Units must be between " + MIN_UNITS + " and " + MAX_UNITS + " kWh", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = dbHelper.addBill(month, units, rebatePercentage, totalCharges, finalCost);

        if (id != -1) {
            Toast.makeText(this, "Bill saved successfully!", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(false);
            clearForm();
        } else {
            Toast.makeText(this, "Error saving bill", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBillInDatabase() {
        String month = monthSpinner.getSelectedItem().toString();
        String unitsStr = unitsEditText.getText().toString().trim();

        if (unitsStr.isEmpty() || totalCharges == 0) {
            Toast.makeText(this, "Please calculate bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        double units;
        try {
            units = Double.parseDouble(unitsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid units value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate again before updating
        if (units < MIN_UNITS || units > MAX_UNITS) {
            Toast.makeText(this, "Units must be between " + MIN_UNITS + " and " + MAX_UNITS + " kWh", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isUpdated = dbHelper.updateBill(editBillId, month, units, rebatePercentage, totalCharges, finalCost);

        if (isUpdated) {
            Toast.makeText(this, "Bill updated successfully!", Toast.LENGTH_SHORT).show();

            // Go back to BillsActivity
            Intent intent = new Intent(this, BillsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error updating bill", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        unitsEditText.setText("");
        rebateSeekBar.setProgress(0);
        rebatePercentage = 0;
        totalChargesText.setText("Total Charges: -");
        finalCostText.setText("Final Cost: -");
        totalCharges = 0;
        finalCost = 0;

        // Reset edit mode if active
        if (isEditMode) {
            isEditMode = false;
            editBillId = -1;
            saveButton.setText("Save");
            saveButton.setBackgroundTintList(getResources().getColorStateList(R.color.secondary_color));
            // Reset ActionBar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("⚡ Electricity Bill Calculator");
            }
            // Show View Bills button again
            viewBillsButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}