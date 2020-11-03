package com.example.mystockwatchapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


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
        if(stock.getPrice() != null  && stock.getPriceChange() != null && stock.getPriceChangePercent() != null){
            holder.price.setText(String.format("%s", stock.getPrice()));
            if(stock.getPriceChange() >= 0){
                int color = Color.GREEN;
                char up = '\u25B2';
                holder.stock_symbol.setTextColor(color);
                holder.company_name.setTextColor(color);
                holder.price.setTextColor(color);
                holder.price_change.setTextColor(color);
                holder.price_change.setText(String.format("%s %s(%.2f%%)", up, stock.getPriceChange(), stock.getPriceChangePercent()));
            }
            else{
                int color = Color.RED;
                char down = '\u25BC';
                holder.stock_symbol.setTextColor(color);
                holder.company_name.setTextColor(color);
                holder.price.setTextColor(color);
                holder.price_change.setTextColor(color);
                holder.price_change.setText(String.format("%s %s(%.2f%%)", down, stock.getPriceChange(), stock.getPriceChangePercent()));
            }
        }

        else{
            holder.price.setText("0.0");
            holder.price_change.setText("0.0");
        }




    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
