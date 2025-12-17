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
    private Button calculateButton, saveButton, viewBillsButton, aboutButton;

    private double totalCharges = 0;
    private double finalCost = 0;
    private double rebatePercentage = 0;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set custom title
        //getSupportActionBar().setTitle("⚡ MyElectricBill Calculator");


        // Initialize database
        dbHelper = new DBHelper(this);

        initializeViews();
        setupMonthSpinner();
        setupListeners();
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
        aboutButton = findViewById(R.id.aboutButton);

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
        saveButton.setOnClickListener(v -> saveToDatabase());
        viewBillsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BillsActivity.class);
            startActivity(intent);
        });
        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private void calculateBill() {
        String unitsStr = unitsEditText.getText().toString().trim();

        // Validation
        if (unitsStr.isEmpty()) {
            unitsEditText.setError("Please enter electricity units");
            unitsEditText.requestFocus();
            return;
        }

        double units = Double.parseDouble(unitsStr);
        if (units <= 0) {
            unitsEditText.setError("Units must be greater than 0");
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

        double units = Double.parseDouble(unitsStr);

        long id = dbHelper.addBill(month, units, rebatePercentage, totalCharges, finalCost);

        if (id != -1) {
            Toast.makeText(this, "Bill saved successfully!", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(false);
            clearForm();
        } else {
            Toast.makeText(this, "Error saving bill", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        unitsEditText.setText("");
        rebateSeekBar.setProgress(0);
        totalChargesText.setText("Total Charges: -");
        finalCostText.setText("Final Cost: -");
        totalCharges = 0;
        finalCost = 0;
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}