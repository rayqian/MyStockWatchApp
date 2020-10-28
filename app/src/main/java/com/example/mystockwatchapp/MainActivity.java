package com.example.mystockwatchapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener{

    private RecyclerView recyclerView;
    private StockAdapter myAdapter;
    private final List<Stock> stockList = new ArrayList<>();

    private static final String TAG = "from MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //add dummy data
        Stock a = new Stock("AMZ", "YAMAXUN", 100.00, 0.01);
        Stock b = new Stock("GOOGLE", "GUGE", 1500.00, 0.15);
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
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(et);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handleSearchStock();//handle search stock here
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

    public void handleSearchStock(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //dummy data
        final CharSequence[] sArray = new CharSequence[20];
        for (int i = 0; i < 20; i++)
            sArray[i] = "Choice " + i;

        builder.setTitle("Make a selection");
//        builder.setItems(sArray, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                tv2.setText(sArray[which]);
//            }
//        });
        builder.setItems(sArray, null);

        builder.setNegativeButton("Nevermind", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}