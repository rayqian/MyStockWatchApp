package com.example.mystockwatchapp;

import java.io.Serializable;

public class Stock implements Serializable {
    private String stock_symbol;
    private String company_name;
    private Double price;
    private Double price_change;

    Stock(String stock_symbol, String company_name, Double price, Double price_change){
        this.stock_symbol = stock_symbol;
        this.company_name = company_name;
        this.price = price;
        this.price_change = price_change;
    }

    String getStockSymbol(){
        return this.stock_symbol;
    }
    String getCompanyName(){
        return this.company_name;
    }
    Double getPrice(){
        return this.price;
    }
    Double getPriceChange(){
        return this.price_change;
    }
}
