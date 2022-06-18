package educing.tech.customer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 12-06-2015.
 */

public class Product implements Serializable
{

    public static List<Product> productList = new ArrayList<>();
    public static List<Product> productCategoryList = new ArrayList<>();
    public static List<Product> productSubCategoryList = new ArrayList<>();

    public int product_id, category_id, sub_category_id, store_id, weight, quantity;
    public double price, discount_price;
    public String product_name, category_name, description, category_thumbnail, sub_category_name, unit, product_thumbnail, date;


    public Product()
    {

    }


    public Product(int category_id, String category_name, String category_thumbnail)
    {
        this.category_id = category_id;
        this.category_name = category_name;
        this.category_thumbnail = category_thumbnail;
    }


    public Product(int category_id, int sub_category_id, String sub_category_name)
    {
        this.category_id = category_id;
        this.sub_category_id = sub_category_id;
        this.sub_category_name = sub_category_name;
    }


    public Product(int category_id, int sub_category_id, int product_id, int quantity, String date)
    {
        this.category_id = category_id;
        this.sub_category_id = sub_category_id;
        this.product_id = product_id;
        this.quantity = quantity;
        this.date = date;
    }


    public Product(int category_id, int sub_category_id, int product_id, String product_name, int weight, String unit, double price, String product_thumbnail)
    {

        this.category_id = category_id;
        this.sub_category_id = sub_category_id;
        this.product_id = product_id;
        this.product_name = product_name;
        this.weight = weight;
        this.unit = unit;
        this.price = price;
        this.product_thumbnail = product_thumbnail;
    }


    public Product(int category_id, int sub_category_id, int product_id, int store_id, String product_name, String description, int weight, String unit, double price, double discount_price, String product_thumbnail)
    {

        this.category_id = category_id;
        this.sub_category_id = sub_category_id;
        this.product_id = product_id;
        this.store_id = store_id;
        this.product_name = product_name;
        this.description = description;
        this.weight = weight;
        this.unit = unit;
        this.price = price;
        this.discount_price = discount_price;
        this.product_thumbnail = product_thumbnail;
    }


    /*public void setProductId(int product_id)
    {
        this.product_id = product_id;
    }*/

    public int getProductId()
    {
        return this.product_id;
    }


    /*public void setCategoryId(int category_id)
    {
        this.category_id = category_id;
    }*/

    public int getCategoryId()
    {
        return this.category_id;
    }


    /*public void setCategoryName(String category_name)
    {
        this.category_name = category_name;
    }*/

    public String getCategoryName()
    {
        return this.category_name;
    }


    /*public void setCategoryThumbnail(String category_thumbnail)
    {
        this.category_thumbnail = category_thumbnail;
    }*/

    public String getCategoryThumbnail()
    {
        return this.category_thumbnail;
    }


    public void setWeight(int weight)
    {
        this.weight = weight;
    }

    public int getWeight()
    {
        return this.weight;
    }


    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public int getQuantity()
    {
        return this.quantity;
    }


    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getUnit()
    {
        return this.unit;
    }


    public void setPrice(double price)
    {
        this.price = price;
    }

    public double getPrice()
    {
        return this.price;
    }


    public static List<Product> getProductList(String value)
    {

        List<Product> list = new ArrayList<>();

        for(Product product: productList)
        {
            if(product.product_name.toLowerCase().contains(value.toLowerCase()))
            {
                list.add(product);
            }
        }

        return list;
    }


    public Product getProduct(int product_id)
    {

        for(Product product: productList)
        {
            if(product.getProductId() == product_id)
            {
                return product;
            }
        }

        return null;
    }


    public static String getCategory(int category_id)
    {

        for(Product product: productCategoryList)
        {
            if(product.category_id == category_id)
            {
                return product.category_name;
            }
        }

        return "";
    }
}