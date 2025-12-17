package com.example.electricitybillcalculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    private ArrayList<Bill> billList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Bill bill);
    }

    public BillAdapter(ArrayList<Bill> billList, OnItemClickListener listener) {
        this.billList = billList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bill bill = billList.get(position);
        DecimalFormat df = new DecimalFormat("0.00");

        holder.monthText.setText(bill.getMonth());
        holder.costText.setText("RM " + df.format(bill.getFinalCost()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(bill);
            }
        });
    }

    @Override
    public int getItemCount() {
        return billList == null ? 0 : billList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView monthText, costText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            monthText = itemView.findViewById(R.id.monthText);
            costText = itemView.findViewById(R.id.costText);
        }
    }
}