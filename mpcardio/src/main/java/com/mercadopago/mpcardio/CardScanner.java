package com.mercadopago.mpcardio;

import android.content.Context;
import android.content.Intent;

/**
 * Created by mreverter on 4/12/15.
 */
public class CardScanner {
    private int mColor;
    private Context mContext;
    private boolean mRequireExpiryDate;

    private CardScannerCallback mCardScannerCallback;

    private static CardScannerCallback mCurrentNeededCardScannerCallback;

    public static CardScanner buildCardScanner(Context context, boolean requireExpiryDate, int color, CardScannerCallback callback) {
        return new CardScanner(context, requireExpiryDate, color, callback);
    }

    public static CardScanner buildCardScanner(Context context, CardScannerPreference preference, CardScannerCallback callback) {
        return new CardScanner(context, preference.isRequireExpiryDate(), preference.getColor(), callback);
    }

    private CardScanner(Context context, boolean requireExpiryDate, int color, CardScannerCallback callback) {
        mContext = context;
        mRequireExpiryDate = requireExpiryDate;
        mColor = color;
        mCardScannerCallback = callback;
    }

    public void startCardScanning() {
        Intent launcherIntent = new Intent(mContext, CardScannerLauncherActivity.class);
        launcherIntent.putExtra("requireExpiryDate", mRequireExpiryDate);
        launcherIntent.putExtra("color", mColor);
        mCurrentNeededCardScannerCallback = this.mCardScannerCallback;
        mContext.startActivity(launcherIntent);
    }

    static void resolveSuccessfulScan(String cardNumber, Integer expiryMonth, Integer expiryYear){
        mCurrentNeededCardScannerCallback.onCardScanned(cardNumber, expiryMonth, expiryYear);
    }

    static void resolveFailedScan(){
        mCurrentNeededCardScannerCallback.onCardScanningFailed();
    }

    static void resolveCancelledScan() {
        mCurrentNeededCardScannerCallback.onCardScanningCancelled();
    }
}
