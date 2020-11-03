package com.example.mystockwatchapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener{

    private RecyclerView recyclerView;
    private StockAdapter myAdapter;
    private final List<Stock> stockList = new ArrayList<>();
    private final List<Stock> searchResult = new ArrayList<>();
    private SwipeRefreshLayout swiper;


    private static final String TAG = "from MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        myAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(this);//call the override onRefresh() method

        stockList.clear();
        //reading json file in onCreate
        readJSONData();
    }

    //saving data to json happens in onPause
    @Override
    public void onPause() {
        writeJSONData();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        int pos = recyclerView.getChildLayoutPosition(view);
        Stock stock = stockList.get(pos);
        String pre_url = "http://www.marketwatch.com/investing/stock/";
        String url = pre_url + stock.getStockSymbol();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

        //open the stock url
//        Intent intent = new Intent(this, EditNoteActivity.class);
//        intent.putExtra("EDIT", note);
//        intent.putExtra("INDEX", pos);
//
//        startActivityForResult(intent, 2);
    }

    @Override
    public boolean onLongClick(View view) {
        int pos = recyclerView.getChildLayoutPosition(view);
        final Stock s = stockList.get(pos);
        //show dialog to delete the note
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol " + s.getStockSymbol() +"?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteStock(s);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do nothing
            }
        });
        builder.setIcon(R.drawable.baseline_delete_outline_black_36);
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Bye, Thanks for using my app!", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                handleAddStockClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        if(!isNetworkAvailable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stocks cannot be updated without a network connection.");
            builder.setTitle("No Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            swiper.setRefreshing(false);
            return;
        }
        updatePrice();
    }

    public void updatePrice(){
        new Thread(new UpdatePriceRunnable(this, stockList)).start();
    }

    public void handleAddStockClick(){
        if(!isNetworkAvailable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stocks cannot be added without a network connection.");
            builder.setTitle("No Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        // Single input value dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create an EditText and set it to be the builder's view
        final EditText et = new EditText(this);
        et.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        et.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);//only allow CAPITAL letters
        et.setGravity(Gravity.CENTER_HORIZONTAL);

        builder.setView(et);

        builder.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String user_input = et.getText().toString();
                handleStockSearch(user_input);
            }
        });

        builder.setMessage("Please enter a Stock Symbol:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void handleStockSearch(String input){
        if(input.isEmpty()){
            return;
        }

        //create new thread to handle search stock
        SearchStockRunnable sr = new SearchStockRunnable(this, input);
        new Thread(sr).start();

    }

    public void handleResultShow(){
        Toast.makeText(this, "handle result received " + searchResult.size() + " stocks", Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");

        //create the String[] to display the search result to user
        final CharSequence[] sArray = new CharSequence[searchResult.size()];
        for (int i = 0; i < searchResult.size(); i++){
            sArray[i] = searchResult.get(i).getSymbolwithName();
        }
        builder.setItems(sArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //add the selection stock to main page
                    validateAdd(searchResult.get(which));
                    writeJSONData();
                    updatePrice();
                }
            });

        builder.setNegativeButton("Nevermind", null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    //accept result from SearchStockRunnable
    public void acceptResult(ArrayList<Stock> stocks){
        searchResult.clear();
        //if no matching result
        if(stocks.size() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Data for stock symbol not found.");
            builder.setTitle("Symbol Not Found");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        //if only one matching result, add it to stock list directly
        else if(stocks.size() == 1){
            validateAdd(stocks.get(0));
            updatePrice();
            Toast.makeText(this, "your stock is added to the list.", Toast.LENGTH_SHORT).show();
        }
        //if multiple matching results
        else{
            searchResult.addAll(stocks);
            Toast.makeText(this, "search result receive " + searchResult.size() + " stocks", Toast.LENGTH_SHORT).show();
            handleResultShow();
        }

    }

    public void downloadFailed() {
        searchResult.clear();
        //myAdapter.notifyDataSetChanged();
    }

    private void writeJSONData() {
        try {
            FileOutputStream fos = getApplicationContext().
                    openFileOutput("StocksFile.json", Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();//create json array by adding a bracket [ in the beginning of json file

            for (Stock n : stockList) {
                writer.beginObject();//create json object by adding {
                writer.name("symbol").value(n.getStockSymbol());
                writer.name("name").value(n.getCompanyName());
                writer.name("price").value(n.getPrice());
                writer.name("price_change").value(n.getPriceChange());
                writer.name("price_change_p").value(n.getPriceChangePercent());
                writer.endObject();//ending json object by adding }
            }
            writer.endArray();//adding a close bracket ] to the end of the file
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "writeJSONData: " + e.getMessage());
        }
    }
    private void readJSONData() {
        try {
            FileInputStream fis = getApplicationContext().
                    openFileInput("StocksFile.json");

            // Read string content from file
            byte[] data = new byte[(int) fis.available()]; // this technique is good for small files
            int loaded = fis.read(data);
            Log.d(TAG, "readJSONData: Loaded " + loaded + " bytes");
            fis.close();
            String json = new String(data);

            // Create JSON Array from string file content
            JSONArray stockArr = new JSONArray(json);
            for (int i = 0; i < stockArr.length(); i++) {
                JSONObject nObj = stockArr.getJSONObject(i);

                // Access note data fields
                String symbol = nObj.getString("symbol");
                String name = nObj.getString("name");
                String price = nObj.getString("price");
                String price_change = nObj.getString("price_change");
                String price_change_p = nObj.getString("price_change_p");

                // Create Note and add to ArrayList
                Stock n = new Stock(symbol, name);
                n.setPrice(Double.parseDouble(price));
                n.setPriceChange(Double.parseDouble(price_change));
                n.setPriceChangePercent(Double.parseDouble(price_change_p));
                stockList.add(n);
            }
            //Collections.sort(noteList);
            Log.d(TAG, "readJSONData: " + stockList);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "readJSONData: " + e.getMessage());
        }
    }

    public void deleteStock(Stock n){
        stockList.remove(n);
        writeJSONData();
        myAdapter.notifyDataSetChanged();
    }

    public void acceptPriceUpdate(List<Stock> stocks){
        Toast.makeText(this, "update price receive " + stocks.size() + " stocks", Toast.LENGTH_SHORT).show();
        myAdapter.notifyDataSetChanged();
        swiper.setRefreshing(false);
    }


    public void validateAdd(Stock s){
        //validate if the stock is duplicate, otherwise add it to the global list
        Set<String> stock_symbols_set = new HashSet<>();
        for(Stock stock: stockList){
            stock_symbols_set.add(stock.getStockSymbol());
        }
        if(stock_symbols_set.contains(s.getStockSymbol())){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.baseline_warning_black_36);
            builder.setMessage("Stock Symbol " + s.getStockSymbol() + " is already displayed. ");
            builder.setTitle("Duplicate Stock");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            stockList.add(s);
        }
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Toast.makeText(this, "No ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }
        //get the network info
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

}