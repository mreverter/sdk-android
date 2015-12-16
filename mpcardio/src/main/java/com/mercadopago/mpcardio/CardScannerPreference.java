package com.mercadopago.mpcardio;

/**
 * Created by mreverter on 5/12/15.
 */
public class CardScannerPreference {
    private int color;
    private boolean requireExpiryDate;

    public CardScannerPreference(int color, boolean requireExpiryDate){
        this.color = color;
        this.requireExpiryDate = requireExpiryDate;
    }

    public int getColor() {
        return color;
    }

    public boolean isRequireExpiryDate() {
        return requireExpiryDate;
    }
}
