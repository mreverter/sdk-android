package com.mercadopago.examples.step6;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mercadopago.ExampleActivity;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.examples.R;
import com.mercadopago.examples.utils.ExamplesUtils;
import com.mercadopago.model.ApiException;

public class Step6Activity extends ExampleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step6);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MercadoPago.CHECKOUT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // Set message
                String msg = "external reference: " + data.getStringExtra("externalReference") + "\n";
                msg += "payment id: " + Long.toString(data.getLongExtra("paymentId", 0L)) + "\n";
                msg += "payment status: " + data.getStringExtra("paymentStatus") + "\n";
                msg += "payment type: " + data.getStringExtra("paymentType") + "\n";
                msg += "preference id: " + data.getStringExtra("preferenceId");

                new AlertDialog.Builder(this)
                        .setTitle("Test")
                        .setMessage(msg)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {

                if ((data != null) && (data.getSerializableExtra("apiException") != null)) {
                    ApiException apiException = (ApiException) data.getSerializableExtra("apiException");
                    Toast.makeText(getApplicationContext(), apiException.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void submitForm(View view) {

        // Create a checkout preference
        ExamplesUtils.createPreference(this);
    }
}
