package com.mercadopago.mpcardio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class CardScannerLauncherActivity extends AppCompatActivity {

    private static final int SCAN_REQUEST_CODE = 1;

    private int mColor;
    private boolean mRequireExpiryDate;
    private CardScannerCallback mCardScannerCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequireExpiryDate = getIntent().getBooleanExtra("requireExpiryDate", false);
        mColor = getIntent().getIntExtra("color", getResources().getColor(R.color.blue));

        Intent scanIntent = new Intent(this, CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, mRequireExpiryDate); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_GUIDE_COLOR, getResources().getColor(mColor));
        this.startActivityForResult(scanIntent, SCAN_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {

                CreditCard scannedCard = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                String cardNumber = scannedCard.getFormattedCardNumber().replaceAll(" ", "");
                Integer expiryMonth = null;
                Integer expiryYear = null;

                if (scannedCard.isExpiryValid()) {
                    expiryMonth = scannedCard.expiryMonth;
                    expiryYear = scannedCard.expiryYear;
                }
                CardScanner.resolveSuccessfulScan(cardNumber, expiryMonth, expiryYear);
            }
            else if(resultCode == RESULT_CANCELED){
                CardScanner.resolveCancelledScan();
            }
            else
            {
                CardScanner.resolveFailedScan();
            }
            finish();
        }
    }
}
