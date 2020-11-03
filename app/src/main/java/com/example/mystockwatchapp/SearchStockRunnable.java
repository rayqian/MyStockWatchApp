package com.example.mystockwatchapp;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SearchStockRunnable implements Runnable {

    private static final String TAG = "SearchStockRunnable";
    private MainActivity mainActivity;
    private String keyword;
    private static final String DATA_URL = "https://api.iextrading.com/1.0/ref-data/symbols";

    SearchStockRunnable(MainActivity ma, String input){
        this.mainActivity = ma;
        this.keyword = input;
    }

    @Override
    public void run() {
        Uri dataUri = Uri.parse(DATA_URL);
        String formatedURL = dataUri.toString();

        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(formatedURL);
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
            } catch (IOException e) {
                e.printStackTrace();
                }

        handleResults(sb.toString());
    }

    private void handleResults(String s) {
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

        final ArrayList<Stock> stockList = parseJSON(s, keyword);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (stockList != null)
                mainActivity.acceptResult(stockList);
            }
        });
    }

    private ArrayList<Stock> parseJSON(String s, String key) {
        ArrayList<Stock> stockList = new ArrayList<>();
        try {
            JSONArray jObjMain = new JSONArray(s);
            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);
                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name");
                if(symbol.indexOf(key) != -1 || name.indexOf(key) != -1){
                    stockList.add(new Stock(symbol,name));
                }
            }
            return stockList;
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
