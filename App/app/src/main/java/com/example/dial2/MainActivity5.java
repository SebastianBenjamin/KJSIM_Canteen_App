package com.example.dial2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity5 extends AppCompatActivity {

    ListView lvItems;
    LocalDate date;
    TextView tvTotalItems, tvTotalAmount, tvDeliveryTime;
    Button btnConfirm;

    ArrayList<String> itemListt;
    int totalAmount = 0;
    AlertDialog.Builder alert;
    String expectedDeliveryTime = "30";
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Orders");

        // Initialize views
        lvItems = findViewById(R.id.lvItems);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDeliveryTime = findViewById(R.id.tvDeliveryTime);
        btnConfirm = findViewById(R.id.btnConfirm);
        date = LocalDate.now();

        // Get data from intent
        Intent intent = getIntent();
        ArrayList<String> itemNames = intent.getStringArrayListExtra("itemNames");
//        ArrayList<String> itemCategories = intent.getStringArrayListExtra("itemCategories");
        ArrayList<Integer> itemPrices = intent.getIntegerArrayListExtra("itemPrices");
        ArrayList<Integer> itemCount =intent.getIntegerArrayListExtra("itemCounts");
        String CUST_ID = intent.getStringExtra("userEmailOriginal");


        ArrayList<String> itemDisplayList = new ArrayList<>();
        int totalAmount = 0;

        if (itemNames != null && itemPrices != null) {
            for (int i = 0; i < itemNames.size(); i++) {
                String name = itemNames.get(i);
                int price = itemPrices.get(i);
                int count =itemCount.get(i);
                if (i==0){
                    itemDisplayList.add("- "+name +" - "+ count+"nos  - Rs " + price + "/-\n");

                }
                else{
                itemDisplayList.add(" "+name +" - "+ count+"nos  - Rs " + price + "/-\n");}
                totalAmount += price;
            }
        } else {
            itemDisplayList.add("Sample Item 1 - Rs 50/-");
            itemDisplayList.add("Sample Item 2 - Rs 75/-");
            totalAmount = 125;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                itemDisplayList
        );
        lvItems.setAdapter(adapter);
        expectedDeliveryTime = intent.getStringExtra("deliverytime");

        tvTotalItems.setText("Total Items: " + itemDisplayList.size());
        tvTotalAmount.setText("Total Amount: ₹" + totalAmount);
        tvDeliveryTime.setText("Expected Delivery Time: " + expectedDeliveryTime + " / " + date);

        int finalTotalAmount = totalAmount;
        String finalCUST_ID = CUST_ID;
        btnConfirm.setOnClickListener(view -> {
            // Generate order number
            String orderNo = generateOrderNumber();

            // Create order structure
            Map<String, Object> orderDetails = new HashMap<>();
            Map<String, Object> itemsMap = new HashMap<>();

            // Add items to the order
            for (int i = 0; i < itemNames.size(); i++) {
                Map<String, Object> itemDetails = new HashMap<>();
                itemDetails.put("itemName", itemNames.get(i));
                itemDetails.put("itemPrice", itemPrices.get(i));
                itemDetails.put("itemQuantity", itemCount.get(i));
                itemsMap.put(String.valueOf (i+1), itemDetails);
            }

            // Add order details
            orderDetails.put("items", itemsMap);
            orderDetails.put("status","pending");
            orderDetails.put("totalItems", itemNames.size());
            orderDetails.put("totalAmount", finalTotalAmount);
            orderDetails.put("expectedDeliveryTime", expectedDeliveryTime);
            orderDetails.put("customerId", finalCUST_ID);
            orderDetails.put("orderDate", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

            // Save to Firebase
            databaseReference.child(orderNo).setValue(orderDetails)
                    .addOnSuccessListener(aVoid -> {

                        showAlert("Order Details", "Order Number "+orderNo+"\n\nItems \n" + itemDisplayList.toString().replace('[',' ').replace(']',' ').replace(',','-')+
                                "\n Delivery by :"+expectedDeliveryTime+"\nTotal : "+finalTotalAmount);
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        lvItems.setAdapter(null);
                        tvTotalItems.setText("Total Items: 0" );
                        tvTotalAmount.setText("Total Amount: ₹ 0" );
                        tvDeliveryTime.setText("Expected Delivery Time: " + expectedDeliveryTime + " / " + date);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity5.this, "Failed to save order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

//            Intent goToMainPage=new Intent(MainActivity5.this,MainActivity.class);
//            startActivity(goToMainPage);
        });
    }

    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        return now.format(formatter);
    }

    public void showAlert(String title, String message) {
        alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.create();
        alert.show();
    }
}