package com.example.mystockwatchapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
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

        // load the data - add dummy data
        Stock a = new Stock("AMZ", "YAMAXUN");
        Stock b = new Stock("GOOGLE", "GUGE");
        stockList.add(a);
        stockList.add(b);

        recyclerView = findViewById(R.id.recycler);
        myAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View view) {
        return false;
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

}