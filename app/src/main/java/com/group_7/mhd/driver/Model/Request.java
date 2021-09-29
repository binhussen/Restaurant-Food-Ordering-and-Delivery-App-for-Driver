package com.group_7.mhd.driver.Model;

import java.util.List;

/**
 * Created by Guest User on 4/1/2018.
 */

public class Request {

    private String phone;
    private String name;
    private String addresslat;
    private String addresslon;
    private String total;
    private String status;
    private String paymentMethod;
    private String table_No;
    private String TackAway;
    private List<Order> foods; //list of fod orders

    public Request() {
    }

    public Request(String phone, String name, String addresslat, String addresslon, String total, String status, String paymentMethod, String table_No, String tackAway, List<Order> foods) {
        this.phone = phone;
        this.name = name;
        this.addresslat = addresslat;
        this.addresslon = addresslon;
        this.total = total;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.table_No = table_No;
        TackAway = tackAway;
        this.foods = foods;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddresslat() {
        return addresslat;
    }

    public void setAddresslat(String addresslat) {
        this.addresslat = addresslat;
    }

    public String getAddresslon() {
        return addresslon;
    }

    public void setAddresslon(String addresslon) {
        this.addresslon = addresslon;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTable_No() {
        return table_No;
    }

    public void setTable_No(String table_No) {
        this.table_No = table_No;
    }

    public String getTackAway() {
        return TackAway;
    }

    public void setTackAway(String tackAway) {
        TackAway = tackAway;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }
}
