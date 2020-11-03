package com.example.mystockwatchapp;

import java.io.Serializable;

public class Stock implements Serializable {
    private String stock_symbol;
    private String company_name;
    private Double price;
    private Double price_change;
    private Double price_change_percent;

    Stock(String stock_symbol, String company_name){
        this.stock_symbol = stock_symbol;
        this.company_name = company_name;
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
    void setPrice(Double price){
        this.price = price;
    }
    Double getPriceChange(){
        return this.price_change;
    }
    void setPriceChange(Double price_change){
        this.price_change = price_change;
    }
    Double getPriceChangePercent(){
        return this.price_change_percent;
    }
    void setPriceChangePercent(Double change_percent){ this.price_change_percent = change_percent; }


    String getSymbolwithName(){
        return this.stock_symbol + " - " + this.company_name;
    }
}
