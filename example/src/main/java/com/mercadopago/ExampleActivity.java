package com.mercadopago;

import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

import com.mercadopago.examples.R;

public class ExampleActivity extends ActionBarActivity {

    @Override
    public void onDestroy() {
        super.onDestroy();

        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageDrawable(null);
    }
}
