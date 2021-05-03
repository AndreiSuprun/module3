package com.epam.esm.service.search;

public class OrderCriteria {

    private String key;
    private String direction;

    public OrderCriteria() {
    }

    public OrderCriteria(final String key, final String direction) {
        this.key = key;
        this.direction = direction;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
