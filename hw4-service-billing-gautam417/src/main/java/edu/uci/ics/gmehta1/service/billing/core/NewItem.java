package edu.uci.ics.gmehta1.service.billing.core;

public class NewItem {
    private int quantity;
    private float unit_price;
    private float discount;

    public NewItem(int quantity, float unit_price, float discount) {
        this.quantity = quantity;
        this.unit_price = unit_price;
        this.discount = discount;
    }
    @Override
    public String toString() {return "Quantity: " + (quantity) + "\nUnit_price: " + Float.toString(unit_price)+ "\nDiscount: " + Float.toString(discount);}

    public int getQuantity() {
        return quantity;
    }

    public float getUnit_price() {
        return unit_price;
    }

    public float getDiscount() {
        return discount;
    }
}
