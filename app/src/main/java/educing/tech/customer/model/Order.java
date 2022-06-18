package educing.tech.customer.model;

import java.util.ArrayList;
import java.util.List;

import educing.tech.customer.helper.GenerateUniqueId;

/**
 * Created by dell on 10-09-2015.
 */
public class Order
{

    public String order_no, category_name, category_image, product_name, product_image, store_name, order_date, unit, order_status;
    public int user_id, store_id, category_id, product_id, weight, quantity;
    public double price, delivery_charge, discount_price, rating;

    public static double sub_total, discount_total, delivery_charge_total, grand_total;


    public static List<Order> orderList = new ArrayList<>();
    public static List<Order> myOrderList = new ArrayList<>();
    public static List<Order> myOrderDetailsList = new ArrayList<>();


    public Order()
    {

    }

    public Order(String order_no, int user_id)
    {
        this.order_no = order_no;
        this.user_id = user_id;
    }


    public Order(int product_id, String product_name, String product_image, int weight, String unit, double price, double discount_price, int quantity)
    {

        this.product_id = product_id;
        this.product_name = product_name;
        this.product_image = product_image;
        this.weight = weight;
        this.unit = unit;
        this.price = price;
        this.discount_price = discount_price;
        this.quantity = quantity;
    }


    public Order(int user_id, String order_no, String category_name, String category_image, int store_id, String store_name, String order_date, String order_status, double rating, double delivery_charge)
    {

        this.user_id = user_id;
        this.order_no = order_no;
        this.category_name = category_name;
        this.category_image = category_image;
        this.store_id = store_id;
        this.store_name = store_name;
        this.order_date = order_date;
        this.order_status = order_status;
        this.rating = rating;
        this.delivery_charge = delivery_charge;
    }


    public void setOrderNo(String order_no)
    {
        this.order_no = order_no;
    }

    public String getOrderNo()
    {
        return this.order_no;
    }


    public void setStoreID(int store_id)
    {
        this.store_id = store_id;
    }

    /*public int getStoreID()
    {
        return this.store_id;
    }*/


    public void setCategoryID(int category_id)
    {
        this.category_id = category_id;
    }

    /*public int getCategoryID()
    {
        return this.category_id;
    }*/


    public void setProductID(int product_id)
    {
        this.product_id = product_id;
    }

    /*public int getProductID()
    {
        return this.product_id;
    }*/


    public void setProductName(String product_name)
    {
        this.product_name = product_name;
    }

    /*public String getProductName()
    {
        return this.product_name;
    }*/


    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getUnit()
    {
        return this.unit;
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


    public void setDiscountPrice(double discount_price)
    {
        this.discount_price = discount_price;
    }

    /*public double getDiscountPrice()
    {
        return this.discount_price;
    }*/


    public void setPrice(double price)
    {
        this.price = price;
    }

    public double getPrice()
    {
        return this.price;
    }



    public List<Order> buildOrderList(List<Product> cart, User user)
    {

        String order_no = GenerateUniqueId.generateOrderNumber(user.user_id);

        List<Order> orderList = new ArrayList<>();

        for (Product pro: cart)
        {

            Order order = new Order();

            order.setOrderNo(order_no);
            order.setStoreID(pro.store_id);
            order.setCategoryID(pro.category_id);
            order.setProductID(pro.product_id);
            order.setProductName(pro.product_name);
            order.setWeight(pro.weight);
            order.setUnit(pro.unit);
            order.setQuantity(pro.quantity);
            order.setPrice(pro.price);
            order.setDiscountPrice(pro.discount_price);

            orderList.add(order);
        }

        return orderList;
    }


    public static float calculateSubTotal(List<Order> list)
    {

        float total = 0;

        for (int i = 0; i< list.size(); i++)
        {
            total += list.get(i).price * list.get(i).quantity;
        }

        return total;
    }


    public static float calculateTotalDiscount(List<Order> list)
    {

        float discount_total = 0;

        for (int i = 0; i< list.size(); i++)
        {
            discount_total += (list.get(i).price - list.get(i).discount_price) * list.get(i).quantity;
        }

        return discount_total;
    }


    public static double calculateGrandTotal(double sub_total, double discount_total, double delivery_charge)
    {
        return (sub_total - discount_total + delivery_charge);
    }
}