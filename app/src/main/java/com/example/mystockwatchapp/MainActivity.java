package com.example.mystockwatchapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener{

    private RecyclerView recyclerView;
    private StockAdapter myAdapter;
    private final List<Stock> stockList = new ArrayList<>();
    private final List<Stock> searchResult = new ArrayList<>();


    private static final String TAG = "from MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        myAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // load the data - add dummy data
//        Stock a = new Stock("AMZ", "YAMAXUN");
//        Stock b = new Stock("GOOGLE", "GUGE");
//        stockList.add(a);
//        stockList.add(b);

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

    public void handleAddStockClick(){
        // Single input value dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create an EditText and set it to be the builder's view
        final EditText et = new EditText(this);
        et.setFilters(new InputFilter[] {new InputFilter.AllCaps()});//only allow CAPITAL letters
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(et);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String user_input = et.getText().toString();
                handleStockSearch(user_input);
            }
        });

        builder.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setMessage("Please enter a Stock Symbol:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void handleStockSearch(String input){
        //create new thread to handle search stock
        SearchStockRunnable sr = new SearchStockRunnable(this, input);
        new Thread(sr).start();

        if(input.isEmpty()){
            return;
        }
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
                stockList.add(searchResult.get(which));
                myAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Nevermind", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //accept result from SearchStockRunnable
    public void acceptResult(ArrayList<Stock> stocks){
        searchResult.addAll(stocks);
        Toast.makeText(this, "search result receive " + searchResult.size() + " stocks", Toast.LENGTH_SHORT).show();

        handleResultShow();
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

            //Collections.sort(stockList);

            for (Stock n : stockList) {
                writer.beginObject();//create json object by adding {
                writer.name("symbol").value(n.getStockSymbol());
                writer.name("name").value(n.getCompanyName());
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

                // Create Note and add to ArrayList
                Stock n = new Stock(symbol, name);
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
        myAdapter.notifyDataSetChanged();
    }

}