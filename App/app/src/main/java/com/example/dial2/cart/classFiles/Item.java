package com.example.dial2.cart.classFiles;


public class Item {
    private String name, category,image,id;
    private int price, unitPrice, count;


    public Item(String name, String category, String image, String id, int price) {
        this.name = name;
        this.category = category;
        this.image = image;
        this.id = id;
        this.price = price;
        this.unitPrice = price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Item(String name, String category, int price, String image) {
        this.name = name;
        this.category = category;
        this.unitPrice = price;
        this.price = price;
        this.image = image;
        this.count = 1;
    }


    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getPrice() { return price; }
    public int getUnitPrice() { return unitPrice; }
    public String getImage() { return image; }
    public int getCount() { return count; }


    public void setCount(int count) {
        this.count = count;
        this.price = unitPrice * count;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
