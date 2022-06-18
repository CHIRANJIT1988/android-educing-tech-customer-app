package educing.tech.customer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Store implements Serializable
{

    public static List<Store> storeList = new ArrayList<>();

    public int id, is_online, delivery_status;
    public double price;
    public float rating;
    public String name, owner, phone_no, address, city, state, country, pincode;
    public double latitude, longitude, distance, amount, delivery_charge;


    public Store(int id, String name, String owner, String phone_no, String address, String city, String state, String country, String pincode, double latitude, double longitude, double distance, int delivery_status, double amount, double delivery_charge, float rating, int is_online)
    {

        this.id = id;
        this.name = name;
        this.owner = owner;
        this.phone_no = phone_no;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.is_online = is_online;
        this.delivery_status = delivery_status;
        this.amount = amount;
        this.delivery_charge = delivery_charge;
        this.rating = rating;
    }

    public Store(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return this.id;
    }


    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }


    public void setRating(float rating)
    {
        this.rating = rating;
    }

    public float getRating()
    {
        return this.rating;
    }


    public void setPrice(double price)
    {
        this.price = price;
    }

    public double getPrice()
    {
        return this.price;
    }


    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getAddress()
    {
        return this.address;
    }
}