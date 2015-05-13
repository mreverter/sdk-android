package com.mercadopago;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.mercadopago.adapters.BankDealsAdapter;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.decorations.DividerItemDecoration;
import com.mercadopago.model.BankDeal;
import com.mercadopago.util.LayoutUtil;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BankDealsActivity extends ActionBarActivity {

    // Activity parameters
    protected String mMerchantPublicKey;

    // Local vars
    protected Activity mActivity;
    protected MercadoPago mMercadoPago;
    protected RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView();

        // Set activity
        mActivity = this;

        // Get activity parameters
        mMerchantPublicKey = mActivity.getIntent().getStringExtra("merchantPublicKey");

        // Init MercadoPago object with public key
        mMercadoPago = new MercadoPago.Builder()
                .setContext(mActivity)
                .setPublicKey(mMerchantPublicKey)
                .build();

        // Set recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.bank_deals_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));

        // Set a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        // Get bank deals
        getBankDeals();
    }

    protected void setContentView() {

        setContentView(R.layout.activity_bank_deals);
    }

    public void refreshLayout(View view) {
        getBankDeals();
    }

    private void getBankDeals() {

        LayoutUtil.showProgressLayout(mActivity);
        mMercadoPago.getBankDeals(new Callback<List<BankDeal>>() {
            @Override
            public void success(List<BankDeal> bankDeals, Response response) {

                try {
                    mRecyclerView.setAdapter(new BankDealsAdapter(mActivity, bankDeals, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            BankDeal selectedBankDeal = (BankDeal) view.getTag();

                            Toast.makeText(mActivity, selectedBankDeal.getLegals(), Toast.LENGTH_LONG).show();
                /*
                Object row = mListView.getItemAtPosition(position);
                if (row instanceof ArrayList) {
                    Intent intent = new Intent(parent.getContext(), TermsAndConditionsActivity.class);
                    intent.putExtra(PROMOS, (ArrayList<Promo>) row);
                    startActivity(intent);
                }
                */
                        }
                    }));

                    LayoutUtil.showRegularLayout(mActivity);

                } catch (Exception ex) {

                }
            }

            @Override
            public void failure(RetrofitError error) {

                Toast.makeText(mActivity, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
