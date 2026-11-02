package com.erp.inventory.service;

import com.erp.core.exception.BusinessRuleException;

public class InsufficientStockException extends BusinessRuleException {

    public InsufficientStockException(String sku, int available, int requested) {
        super("Insufficient stock for " + sku + ": available " + available + ", requested " + requested);
    }
}
