package com.mercadopago;

import android.content.Intent;
import android.view.ContextThemeWrapper;

import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.test.BaseTest;
import com.mercadopago.test.StaticMock;
import com.mercadopago.util.JsonUtil;

public class CongratsActivityTest extends BaseTest<CongratsActivity> {

    public CongratsActivityTest() {

        super(CongratsActivity.class);
    }

    public void testApprovedPaymentCongrats() {

        // Set activity
        CongratsActivity activity = prepareActivity(StaticMock.getPayment(getApplicationContext()),
                StaticMock.getPaymentMethod(getApplicationContext()));

        // Validate view
        assertTrue(activity.getTitle().equals(activity.getString(R.string.approved_title)));
    }

    public void testPendingPaymentCongrats() {

        // Set activity
        Payment payment = StaticMock.getPayment(getApplicationContext());
        payment.setStatus("pending");
        CongratsActivity activity = prepareActivity(payment,
                StaticMock.getPaymentMethod(getApplicationContext()));

        // Validate view
        assertTrue(activity.getTitle().equals(activity.getString(R.string.pending_title)));
    }

    public void testInProcessPaymentCongrats() {

        // Set activity
        Payment payment = StaticMock.getPayment(getApplicationContext());
        payment.setStatus("in_process");
        CongratsActivity activity = prepareActivity(payment,
                StaticMock.getPaymentMethod(getApplicationContext()));

        // Validate view
        assertTrue(activity.getTitle().equals(activity.getString(R.string.in_process_title)));
    }

    public void testRejectedPaymentCongrats() {

        // Set activity
        Payment payment = StaticMock.getPayment(getApplicationContext());
        payment.setStatus("rejected");
        CongratsActivity activity = prepareActivity(payment,
                StaticMock.getPaymentMethod(getApplicationContext()));

        // Validate view
        assertTrue(activity.getTitle().equals(activity.getString(R.string.rejected_title)));
    }

    private CongratsActivity prepareActivity(Payment payment, PaymentMethod paymentMethod) {

        Intent intent = new Intent();
        if (payment != null) {
            intent.putExtra("payment", JsonUtil.getInstance().toJson(payment));
        }
        if (paymentMethod != null) {
            intent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(paymentMethod));
        }
        setActivityIntent(intent);
        return getActivity();
    }
}
