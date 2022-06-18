package educing.tech.customer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CHIRANJIT on 10/8/2015.
 */
public class ShippingAddress implements Serializable
{


    public static List<ShippingAddress> shippingAddressList = new ArrayList<>();

    private int address_id, user_id;
    public String name, pincode, phone_no, address, landmark, city, state, country;


    public ShippingAddress()
    {

    }


    public ShippingAddress(int address_id, int user_id, String name, String pincode, String phone_no, String landmark, String address, String city, String state, String country)
    {

        this.address_id = address_id;
        this.user_id = user_id;
        this.name = name;
        this.pincode = pincode;
        this.phone_no = phone_no;
        this.landmark = landmark;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
    }


    public void setAddressId(int address_id)
    {
        this.address_id = address_id;
    }

    public int getAddressId()
    {
        return this.address_id;
    }


    public void setUserId(int user_id)
    {
        this.user_id = user_id;
    }

    public int getUserId()
    {
        return this.user_id;
    }


    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }


    public void setPhoneNo(String phone_no)
    {
        this.phone_no = phone_no;
    }

    public String getPhoneNo()
    {
        return this.phone_no;
    }


    public void setLandmark(String landmark)
    {
        this.landmark = landmark;
    }

    public String getLandmark()
    {
        return this.landmark;
    }


    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getAddress()
    {
        return this.address;
    }


    public void setCity(String city)
    {
        this.city = city;
    }

    public String getCity()
    {
        return this.city;
    }


    public void setState(String state)
    {
        this.state = state;
    }

    public String getState()
    {
        return this.state;
    }


    public void setPincode(String pincode)
    {
        this.pincode = pincode;
    }

    public String getPincode()
    {
        return this.pincode;
    }
}