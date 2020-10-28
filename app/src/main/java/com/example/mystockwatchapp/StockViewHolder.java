package com.example.mystockwatchapp;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {
    //showing on the main activity page for each stock
    TextView stock_symbol;
    TextView company_name;
    TextView price;
    TextView price_change;

    StockViewHolder(View itemView){
        super(itemView);

        stock_symbol = itemView.findViewById(R.id.stock_symbol);
        company_name = itemView.findViewById(R.id.company_name);
        price = itemView.findViewById(R.id.price);
        price_change = itemView.findViewById(R.id.price_change);
    }

}
