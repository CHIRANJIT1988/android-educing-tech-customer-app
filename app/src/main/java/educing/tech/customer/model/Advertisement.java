package educing.tech.customer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CHIRANJIT on 4/26/2016.
 */
public class Advertisement
{

    public static List<Advertisement> advertisementList = new ArrayList<>();

    public int advertisement_id, store_id, category_id;
    public String message, timestamp, file_name, store_name;
    public double store_rating;


    public Advertisement()
    {

    }

    public Advertisement(int store_id, String store_name, int category_id, String file_name)
    {

        this.store_id = store_id;
        this.store_name = store_name;
        this.category_id = category_id;
        this.file_name = file_name;
    }


    public Advertisement(int store_id, String store_name, double store_rating, int category_id, String message, String file_name, String timestamp)
    {

        this.store_id = store_id;
        this.store_name = store_name;
        this.store_rating = store_rating;
        this.category_id = category_id;
        this.file_name = file_name;
        this.message = message;
        this.timestamp = timestamp;
    }


    public void setAdvertisementId(int advertisement_id)
    {
        this.advertisement_id = advertisement_id;
    }

    public int getAdvertisementId()
    {
        return this.advertisement_id;
    }


    public void setStoreId(int store_id)
    {
        this.store_id = store_id;
    }

    public int getStoreId()
    {
        return this.store_id;
    }


    public void setStoreName(String store_name)
    {
        this.store_name = store_name;
    }

    public String getStoreName()
    {
        return this.store_name;
    }


    public void setRating(double store_rating)
    {
        this.store_rating = store_rating;
    }

    public double getRating()
    {
        return this.store_rating;
    }


    public void setCategoryId(int category_id)
    {
        this.category_id = category_id;
    }

    public int getCategoryId()
    {
        return this.category_id;
    }


    public void setFileName(String file_name)
    {
        this.file_name = file_name;
    }

    public String getFileName()
    {
        return this.file_name;
    }


    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return this.message;
    }


    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getTimestamp()
    {
        return this.timestamp;
    }
}
