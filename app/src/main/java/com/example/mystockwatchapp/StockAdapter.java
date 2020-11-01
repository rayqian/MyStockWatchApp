package com.example.mystockwatchapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private List<Stock> stockList;
    private MainActivity ma;

    StockAdapter(List<Stock> stockList, MainActivity ma){
        this.stockList = stockList;
        this.ma = ma;
    }


    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_entry, parent, false);

        itemView.setOnClickListener(ma);
        itemView.setOnLongClickListener(ma);

        return new StockViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.stock_symbol.setText(stock.getStockSymbol());
        holder.company_name.setText(stock.getCompanyName());
        holder.price.setText(String.format("%s", stock.getPrice()));
        holder.price_change.setText(String.format("%s", stock.getPriceChange()));
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
