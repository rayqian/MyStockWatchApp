package com.example.mystockwatchapp;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdatePriceRunnable implements Runnable{
    private static final String TAG = "UpdatePriceRunnable";
    private MainActivity mainActivity;
    private String API_KEY = "pk_8919c47b5a824db88dfdd4663637b594";
    private static final String PRE_URL = "https://cloud.iexapis.com/stable/stock/";
    private List<Stock> stocks;
    private List<String> URLs = new ArrayList<>();
    private List<String> jsonresults = new ArrayList<>();

    UpdatePriceRunnable(MainActivity ma, List<Stock> stocks){
        this.mainActivity = ma;
        this.stocks = stocks;
    }

    @Override
    public void run() {
        for(Stock s: stocks){
            String url = PRE_URL + s.getStockSymbol() + "/quote?token=" + API_KEY;
            Uri dataUri = Uri.parse(url);
            String formattedURL = dataUri.toString();
            URLs.add(formattedURL);
        }
        //for each stock, build connection to fetch latest data
        for(String formattedURL:URLs){
            StringBuilder sb = new StringBuilder();
            try{
                URL url = new URL(formattedURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                    handleResults(null);
                    return;
                }

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                Log.d(TAG, "run: " + sb.toString());
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            jsonresults.add(sb.toString());
        }
        handleResults(jsonresults);

    }

    private void handleResults(List<String> s) {
        if (s == null) {
            Log.d(TAG, "handleResults: Failure in data download");
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.downloadFailed();
                }
            });
            return;
        }

        final List<Stock> updated_stocks = parseJSON(stocks, jsonresults);

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (updated_stocks != null)
                    Toast.makeText(mainActivity, " updating " + updated_stocks.size() + " stocks.", Toast.LENGTH_SHORT).show();
                mainActivity.acceptPriceUpdate(updated_stocks);
            }
        });
    }

    private List<Stock> parseJSON(List<Stock> stocks, List<String> jsons) {
        for(int i = 0; i < jsons.size(); i++){
            try {
                Stock s = stocks.get(i);
                JSONObject jStock = new JSONObject(jsons.get(i));
                String price = jStock.getString("latestPrice");
                String change = jStock.getString("change");
                String changeP = jStock.getString("changePercent");
                s.setPrice(Double.parseDouble(price));
                s.setPriceChange(Double.parseDouble(change));
                s.setPriceChangePercent(Double.parseDouble(changeP));
                }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return stocks;
    }
}
