package com.example.dial2.menuPage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.dial2.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CategoryFragment extends Fragment {

    private CategoryViewModel categoryViewModel;
    private DatabaseReference cartDatabaseRef;

    private String userEmail;
    private String userEmailOriginal;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.menupage_fragment_category, container, false);
        LinearLayout containerLayout = root.findViewById(R.id.linear_container_common);

        cartDatabaseRef = FirebaseDatabase.getInstance().getReference("Carts");

        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        // Retrieve categoryId passed from navigation arguments
        String categoryId = getArguments() != null ? getArguments().getString("categoryId", "default") : "default";
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail", "defaultEmail");
            userEmailOriginal = getArguments().getString("userEmailOriginal", "defaultOriginalEmail");
        }


        // Observe the category image
        categoryViewModel.getCategoryImage(categoryId).observe(getViewLifecycleOwner(), categoryImage -> {
            if (categoryImage != null) {
                // Observe the items and populate UI
                categoryViewModel.getItems(categoryId).observe(getViewLifecycleOwner(), items -> {
                    containerLayout.removeAllViews();
                    for (CategoryViewModel.Item item : items) {

                        View cardView = inflater.inflate(R.layout.menupage_card_item, containerLayout, false);

                        TextView itemName = cardView.findViewById(R.id.text_item);
                        TextView itemPrice = cardView.findViewById(R.id.text_price);
                        ImageView itemImage = cardView.findViewById(R.id.image_item);
                        ImageButton addButton = cardView.findViewById(R.id.add);
                        ImageButton removeButton = cardView.findViewById(R.id.remove);
                        TextView itemCount = cardView.findViewById(R.id.text_count);

                        itemName.setText(item.name);
                        itemPrice.setText("   ₹" + item.price);
                        Glide.with(requireContext())
                                .load(categoryImage)
                                .placeholder(R.drawable.menupage_placeholder) // Replace with your placeholder
                                .error(R.drawable.menupage_error) // Replace with your error image
                                .into(itemImage);
                        itemCount.setText("0");

                        addButton.setOnClickListener(v -> {
                            int count = Integer.parseInt(itemCount.getText().toString());
                            count++;
                            itemCount.setText(String.valueOf(count));
                            updateCart(item.itemCode, count); // Pass itemCode here
                        });

                        removeButton.setOnClickListener(v -> {
                            int count = Integer.parseInt(itemCount.getText().toString());
                            if (count > 0) {
                                count--;
                                itemCount.setText(String.valueOf(count));
                                updateCart(item.itemCode, count); // Pass itemCode here
                            }
                            if (count == 0) {
                                Toast.makeText(requireContext(), "Please add the item!", Toast.LENGTH_LONG).show();
                            }
                        });

                        containerLayout.addView(cardView);
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Category image not found!", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void updateCart(String itemCode, int quantity) {
        if (cartDatabaseRef == null || userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Error: User email is not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the user's cart
        DatabaseReference userCartRef = cartDatabaseRef.child(userEmail).child("items");

        if (quantity > 0) {
            // Add or update the item in the user's cart
            userCartRef.child(itemCode).child("itemCode").setValue(itemCode);
            userCartRef.child(itemCode).child("quantity").setValue(quantity)
                    .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Item updated successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Remove the item if quantity is zero
            userCartRef.child(itemCode).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Item removed successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to remove item: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }






    private String generateItemId(String itemName) {
        return itemName.replace(" ", "_").toLowerCase(); // Simple logic to generate a unique ID
    }

    public static class CartItem {
        public String itemCode;
        public String itemName;
        public int quantity;

        public CartItem() {
        }

        public CartItem(String itemCode, String itemName, int quantity) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.quantity = quantity;
        }
    }
}
