package com.example.qbook.deliverytracker;



public class Delivery {
    private int deliveryId;
    private String deliveryLocationLatitude;
    private String deliveryLocationLongitude;

    public Delivery(int deliveryId, String deliveryLocationLatitude, String deliveryLocationLongitude) {
        this.deliveryId = deliveryId;
        this.deliveryLocationLatitude = deliveryLocationLatitude;
        this.deliveryLocationLongitude = deliveryLocationLongitude;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public String getDeliveryIdAsString() {
        return String.valueOf(deliveryId);
    }

    public void setDeliveryId(int deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getDeliveryLocationLatitude() {
        return deliveryLocationLatitude;
    }

    public void setDeliveryLocationLatitude(String deliveryLocationLatitude) {
        this.deliveryLocationLatitude = deliveryLocationLatitude;
    }

    public String getDeliveryLocationLongitude() {
        return deliveryLocationLongitude;
    }

    public void setDeliveryLocationLongitude(String deliveryLocationLongitude) {
        this.deliveryLocationLongitude = deliveryLocationLongitude;
    }
}
