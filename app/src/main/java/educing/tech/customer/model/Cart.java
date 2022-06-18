package educing.tech.customer.model;

import java.util.ArrayList;
import java.util.List;


public class Cart
{

    public static int selected_category;

    public static List<Product> cart = new ArrayList<>();


    public static List<Product> buildCart(int category_id, int store_id)
    {

        List<Product> list = new ArrayList<>();

        for(int index = 0; index < cart.size(); index++)
        {

            if(cart.get(index).category_id == category_id && cart.get(index).store_id == store_id)
            {
                list.add(cart.get(index));
            }
        }

        return list;
    }


    public static int searchProduct(int product_id)
    {

        for(int index = 0; index < cart.size(); index++)
        {

            if(cart.get(index).getProductId() == product_id)
            {
                return index;
            }
        }

        return -1;
    }


    public static float calculateSubTotal(List<Product> list)
    {

        float total = 0;

        for (int i = 0; i< list.size(); i++)
        {
            total += list.get(i).price * list.get(i).quantity;
        }

        return total;
    }


    public static float calculateTotalDiscount(List<Product> list)
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


    public static double calculateDeliveryCharge(List<Product> list, double amount, double delivery_charge)
    {

        double total = calculateSubTotal(list) - calculateTotalDiscount(list);

        if(total < amount)
        {
            return delivery_charge;
        }

        return 0;
    }
}