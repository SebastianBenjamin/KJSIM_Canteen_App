package com.example.dial2.menuPage;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends ViewModel {
    private final MutableLiveData<List<Item>> items = new MutableLiveData<>();
    private final MutableLiveData<String> categoryImage = new MutableLiveData<>();

    public LiveData<List<Item>> getItems(String categoryId) {
        fetchItemsFromFirebase(categoryId);
        return items;
    }

    public LiveData<String> getCategoryImage(String categoryId) {
        fetchCategoryImageFromFirebase(categoryId);
        return categoryImage;
    }

    private void fetchItemsFromFirebase(String categoryId) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Items");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Item> itemList = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String fetchedCategoryId = itemSnapshot.child("categoryId").getValue(String.class);
                    if (categoryId.equals(fetchedCategoryId)) {
                        String name = itemSnapshot.child("name").getValue(String.class);
                        Long priceLong = itemSnapshot.child("price").getValue(Long.class);
                        String itemCode = itemSnapshot.getKey(); // Use the database key as itemCode

                        if (name != null && priceLong != null && itemCode != null) {
                            int price = priceLong.intValue();
                            itemList.add(new Item(itemCode, name, price));
                        }
                    }
                }
                items.setValue(itemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                items.setValue(new ArrayList<>()); // Empty list on error
            }
        });
    }

    private void fetchCategoryImageFromFirebase(String categoryId) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Categories").child(categoryId);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image = snapshot.child("image").getValue(String.class);
                if (image != null) {
                    categoryImage.setValue(image);
                } else {
                    categoryImage.setValue(null); // Handle missing image
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                categoryImage.setValue(null); // Handle error
            }
        });
    }

    public static class Item {
        public String itemCode;
        public String name;
        public int price;

        public Item(String itemCode, String name, int price) {
            this.itemCode = itemCode;
            this.name = name;
            this.price = price;
        }
    }
}
