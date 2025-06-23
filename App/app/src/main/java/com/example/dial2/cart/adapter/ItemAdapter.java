package com.example.dial2.cart.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.dial2.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.dial2.cart.classFiles.Item;

public class ItemAdapter extends BaseAdapter {
    private final Context context;
    private final List<Item> itemList;
    private TextView cartTotal;
    private TextView deliveryTimeText;
    private Calendar deliveryTime;
    public String CUST_ID="";

    public ItemAdapter(Context context, List<Item> itemList, TextView cartTotal, TextView deliveryTimeText) {
        this.context = context;
        this.itemList = itemList;
        this.cartTotal = cartTotal;
        this.deliveryTimeText = deliveryTimeText;
        deliveryTime = Calendar.getInstance();
        deliveryTime.add(Calendar.MINUTE, 30);
        updateDeliveryTime();
        updateCartTotal();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.itemName = convertView.findViewById(R.id.itemName);
            holder.itemCategory = convertView.findViewById(R.id.itemCategory);
            holder.itemPrice = convertView.findViewById(R.id.itemPrice);
            holder.itemCount = convertView.findViewById(R.id.itemCount);
            holder.plusButton = convertView.findViewById(R.id.plusButton);
            holder.minusButton = convertView.findViewById(R.id.minusButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item currentItem = itemList.get(position);
        String itemId = currentItem.getId();
        Glide.with(context)
                .load(currentItem.getImage())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .into(holder.imageView);

        holder.itemName.setText(currentItem.getName());
        holder.itemCategory.setText(currentItem.getCategory());
        holder.itemPrice.setText("₹" + currentItem.getPrice());
        holder.itemCount.setText(String.valueOf(currentItem.getCount()));

        holder.plusButton.setOnClickListener(v -> {
            int count = currentItem.getCount() + 1;
            currentItem.setCount(count);
            holder.itemCount.setText(String.valueOf(count));
            holder.itemPrice.setText("₹" + currentItem.getPrice());
            updateItemQuantityInDB(itemId, count);
            updateCartTotal();
        });

        holder.minusButton.setOnClickListener(v -> {
            if (currentItem.getCount() > 1) {
                int count = currentItem.getCount() - 1;
                currentItem.setCount(count);
                holder.itemCount.setText(String.valueOf(count));
                holder.itemPrice.setText("₹" + currentItem.getPrice());
                updateCartTotal();
                updateItemQuantityInDB(itemId, count);
            } else {
                itemList.remove(position);
                notifyDataSetChanged();
                deleteItemFromDB(itemId);
                updateCartTotal();
                Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    public void adjustDeliveryTime(int minutes) {
        Calendar newTime = (Calendar) deliveryTime.clone();
        newTime.add(Calendar.MINUTE, minutes);

        Calendar now = Calendar.getInstance();
        Calendar minAllowedTime = (Calendar) now.clone();
        minAllowedTime.add(Calendar.MINUTE, 30);

        if (minutes > 0) {
            deliveryTime = newTime;
            updateDeliveryTime();
        }

        else {
            if (newTime.before(minAllowedTime)) {
                Toast.makeText(context, "Delivery time must be at least 30 minutes from now",
                        Toast.LENGTH_SHORT).show();
                deliveryTime = minAllowedTime;
                updateDeliveryTime();
            } else {
                deliveryTime = newTime;
                updateDeliveryTime();
            }
        }
    }

    public void updateDeliveryTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        deliveryTimeText.setText(timeFormat.format(deliveryTime.getTime()));
    }

    public void updateCartTotal() {
        int total = 0;
        for (Item item : itemList) {
            total += item.getPrice();
        }
        cartTotal.setText("₹" + total);
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView itemName;
        TextView itemCategory;
        TextView itemPrice;
        TextView itemCount;
        Button plusButton;
        Button minusButton;
    }
    private void updateItemQuantityInDB(String itemId, int newQuantity) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("Carts")
                .child( CUST_ID)
                .child("items")
                .child(itemId)
                .child("quantity");

        dbRef.setValue(newQuantity)
                .addOnSuccessListener(aVoid -> {
                    // Quantity updated successfully
                })
                .addOnFailureListener(e -> {

                    Log.e("ItemAdapter", "Error updating quantity", e);
                });
    }
    private void deleteItemFromDB(String itemId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("Carts")
                .child( CUST_ID)
                .child("items")
                .child(itemId);

        dbRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Item deleted successfully
                })
                .addOnFailureListener(e -> {
                    Log.e("ItemAdapter", "Error deleting item", e);
                });
    }
}