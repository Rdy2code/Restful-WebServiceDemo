package com.example.android.restful.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class defines the POJO that represents the fields and values in the JSON response returned from
 * the data service at our URL. To wrap this class in an Intent, we must implement the Parceleable
 * interface.
 *
 * Add Android Parcelable Code Generator plugin by going to preferences>plugin and
 * search for Parcelable plugin, restart Android Studio, then click in the"class" term, and select
 * command-N Generate Parcelable.
 *
 * Once you have the POJO set up, you can transform the JSON into a list of POJO objects.
 * Third party libraries for doing this include Gson.
 * Step 1: Add the dependency. Go to File>Project Structure, choose dependencies, app, then + button.
 * Step 2: Search for gson, and select the library, click ok 2x to add the dependency to the build.gradle
 *
 * We want to return the JSON response as an array of strongly typed DataItem objects
 *
 */

public class DataItem implements Parcelable {

    private String itemName;
    private String category;
    private String description;
    private int sort;
    private double price;
    private String image;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.itemName);
        dest.writeString(this.category);
        dest.writeString(this.description);
        dest.writeInt(this.sort);
        dest.writeDouble(this.price);
        dest.writeString(this.image);
    }

    public DataItem() {
    }

    protected DataItem(Parcel in) {
        this.itemName = in.readString();
        this.category = in.readString();
        this.description = in.readString();
        this.sort = in.readInt();
        this.price = in.readDouble();
        this.image = in.readString();
    }

    public static final Parcelable.Creator<DataItem> CREATOR = new Parcelable.Creator<DataItem>() {
        @Override
        public DataItem createFromParcel(Parcel source) {
            return new DataItem(source);
        }

        @Override
        public DataItem[] newArray(int size) {
            return new DataItem[size];
        }
    };
}