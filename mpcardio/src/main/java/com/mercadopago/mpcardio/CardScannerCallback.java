package com.mercadopago.mpcardio;

/**
 * Created by mreverter on 4/12/15.
 */
public abstract class CardScannerCallback {
    public abstract void onCardScanned(String cardNumber, Integer expiryMonth, Integer expiryYear);
    public abstract void onCardScanningCancelled();
    public abstract void onCardScanningFailed();
}
