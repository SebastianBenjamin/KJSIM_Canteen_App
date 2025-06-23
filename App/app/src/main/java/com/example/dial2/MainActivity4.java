package com.example.dial2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dial2.cart.adapter.ItemAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.dial2.cart.classFiles.Item;

import java.util.ArrayList;
import java.util.List;

public class MainActivity4 extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference dbReference;
    private ListView listView;
    private ItemAdapter adapter;
    AlertDialog.Builder ShowAlert;
    private List<Item> itemList;
    private ArrayList<String> itemQuantity, itemName, itemPrice, itemCategory, itemImage, itemCatId , itemIds ;
    private TextView cartTotal;
    private TextView deliveryTimeText;
    private Button timePlusBtn, timeMinusBtn, proceedBtn;
    public String CUST_ID ;


    interface FetchItemsCallback {
        void onItemsFetched();
        void onError(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        Intent thisIntent=getIntent();
        String userEmailOriginal=thisIntent.getStringExtra("userEmailOriginal");


        CUST_ID=thisIntent.getStringExtra("userEmail");
        database = FirebaseDatabase.getInstance();
        dbReference = database.getReference();

        listView = findViewById(R.id.itemsList);
        cartTotal = findViewById(R.id.cartTotal);
        deliveryTimeText = findViewById(R.id.textView3);
        timePlusBtn = findViewById(R.id.timePlusBtn);
        timeMinusBtn = findViewById(R.id.timeMinusBtn);
        proceedBtn = findViewById(R.id.btnProceed);
        itemIds = new ArrayList<>();
        itemList = new ArrayList<>();
        itemQuantity = new ArrayList<>();
        itemImage = new ArrayList<>();
        itemName = new ArrayList<>();
        itemCategory = new ArrayList<>();
        itemPrice = new ArrayList<>();
        itemCatId = new ArrayList<>();
        adapter = new ItemAdapter(this, itemList, cartTotal, deliveryTimeText);

        adapter.CUST_ID=CUST_ID;

//        Toast.makeText(this, CUST_ID, Toast.LENGTH_SHORT).show();
        listView.setAdapter(adapter);

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> itemNames = new ArrayList<>();
                ArrayList<String> itemCategories = new ArrayList<>();
                ArrayList<Integer> itemPrices = new ArrayList<>();
                ArrayList<Integer> itemCounts = new ArrayList<>();

                for (Item item : itemList) {
                    itemNames.add(item.getName());
                    itemCategories.add(item.getCategory());
                    itemPrices.add(item.getPrice());
                    itemCounts.add(item.getCount());
                }
                Intent goToCheckoutPage = new Intent(MainActivity4.this, MainActivity5.class);
                goToCheckoutPage.putStringArrayListExtra("itemNames", itemNames);
                goToCheckoutPage.putStringArrayListExtra("itemCategories", itemCategories);
                goToCheckoutPage.putIntegerArrayListExtra("itemPrices", itemPrices);
                goToCheckoutPage.putIntegerArrayListExtra("itemCounts", itemCounts);
                goToCheckoutPage.putExtra("deliverytime", deliveryTimeText.getText().toString());
                goToCheckoutPage.putExtra("userEmailOriginal",userEmailOriginal);
                startActivity(goToCheckoutPage);
            }
        });

        fetchItemIds(new FetchItemsCallback() {
            @Override
            public void onItemsFetched() {
                runOnUiThread(() -> {

                    updateItemList();
                    adapter.updateCartTotal();
//                    showAlert("Items loaded successfully:\n" +
//                            "Names: " + itemName.toString() + "\n" +
//                            "Categories: " + itemCategory.toString() + "\n" +
//                            "Prices: " + itemPrice.toString() + "\n" +
//                            "Images: " + itemImage.toString());
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showAlert(message));
            }
        });

        setupTimeButtons();
    }

    private void updateItemList() {
        itemList.clear();
        for (int i = 0; i < itemName.size(); i++) {
            try {
                String name = itemName.get(i);
                String category = itemCategory.get(i);
                int price = Integer.parseInt(itemPrice.get(i));
                String image = itemImage.get(i);
                int count = Integer.parseInt(itemQuantity.get(i));
                String id=itemIds.get(i);
                Item item = new Item(name, category,image,id,price);
                item.setCount(count);
                itemList.add(item);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error creating item: " + e.getMessage());
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void fetchItemIds(FetchItemsCallback callback) {

        dbReference = database.getReference("Carts/" + CUST_ID).child("items");
        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    itemIds.clear();
                    itemQuantity.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String code = dataSnapshot.child("itemCode").getValue(String.class);
                        Long quantity = dataSnapshot.child("quantity").getValue(Long.class);
                        if (code != null && quantity != null) {
                            itemIds.add(code);
                            itemQuantity.add(quantity.toString());
                        }
                    }
                    if (itemIds.size() > 0) {
                        Toast.makeText(MainActivity4.this, itemIds.size() + " items found in the cart", Toast.LENGTH_LONG).show();
                        fetchItemDetails(itemIds, callback);
                    } else {
                        callback.onError("No valid items in the cart");
                    }
                } else {
                    callback.onError("Cart is empty");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Error reading cart: " + error.getMessage());
            }
        });
    }

    public void fetchItemDetails(ArrayList<String> itemIds, FetchItemsCallback callback) {
        itemName.clear();
        itemCatId.clear();
        itemPrice.clear();

        if (itemIds.isEmpty()) {
            callback.onItemsFetched();
            return;
        }

        final int[] processedItems = {0};
        final int totalItems = itemIds.size();

        for (String itemId : itemIds) {
            dbReference = database.getReference("Items").child(itemId);
            dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String catId = snapshot.child("categoryId").getValue(String.class);
                        Long price = snapshot.child("price").getValue(Long.class);

                        if (name != null && catId != null && price != null) {
                            itemName.add(name);
                            itemCatId.add(catId);
                            itemPrice.add(price.toString());
                        }
                    }

                    processedItems[0]++;
                    if (processedItems[0] == totalItems) {
                        if (itemName.size() > 0) {
                            fetchItemCategoryImage(itemCatId, callback);
                        } else {
                            callback.onError("No valid item details found");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError("Error fetching item details: " + error.getMessage());
                }
            });
        }

    }

    public void fetchItemCategoryImage(ArrayList<String> itemCatId, FetchItemsCallback callback) {
        itemImage.clear();
        itemCategory.clear();

        if (itemCatId.isEmpty()) {
            callback.onItemsFetched();
            return;
        }

        final int[] processedCategories = {0};
        final int totalCategories = itemCatId.size();

        for (String catId : itemCatId) {
            dbReference = database.getReference("Categories").child(catId);
            dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String image = snapshot.child("image").getValue(String.class);
                        String name = snapshot.child("name").getValue(String.class);

                        if (image != null && name != null) {
                            itemImage.add(image);
                            itemCategory.add(name);
                        }
                    }

                    processedCategories[0]++;
                    if (processedCategories[0] == totalCategories) {
                        callback.onItemsFetched();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError("Error fetching category details: " + error.getMessage());
                }
            });
        }
    }

    public void showAlert(String message) {
        runOnUiThread(() -> {
            ShowAlert = new AlertDialog.Builder(MainActivity4.this);
            ShowAlert.setTitle("Alert Message");
            ShowAlert.setMessage(message);
            ShowAlert.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            ShowAlert.create().show();
        });
    }

    private void setupTimeButtons() {
        timePlusBtn.setOnClickListener(v -> adapter.adjustDeliveryTime(10));
        timeMinusBtn.setOnClickListener(v -> adapter.adjustDeliveryTime(-10));
        adapter.updateDeliveryTime();
    }
}