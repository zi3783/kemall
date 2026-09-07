package com.kemall.cart.exception;

public class ProductionNotExistException extends RuntimeException{
    public ProductionNotExistException(String message) {
        super(message);
    }
}
