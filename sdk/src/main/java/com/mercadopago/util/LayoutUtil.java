package com.mercadopago.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.mercadopago.R;

public class LayoutUtil {

    public static void hideKeyboard(Activity activity) {

        try {
            EditText editText = (EditText) activity.getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
        catch (Exception ex) {
        }
    }

    public static void showProgressLayout(Activity activity) {
        showLayout(activity, true, false, false);
    }

    public static void showRegularLayout(Activity activity) {
        showLayout(activity, false, true, false);
    }

    public static void showRefreshLayout(Activity activity) {
        showLayout(activity, false, false, true);
    }

    private static void showLayout(Activity activity, final boolean showProgress, final boolean showLayout, final boolean showRefresh) {

        final View form = activity.findViewById(R.id.regularLayout);
        final View progress = activity.findViewById(R.id.progressLayout);
        final View refresh = activity.findViewById(R.id.refreshLayout);

        int shortAnimTime = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);

        if(progress != null) {
            progress.setVisibility(showRefresh || showLayout ? View.GONE : View.VISIBLE);
            progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha(showProgress ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progress.setVisibility(!showRefresh && showProgress ? View.VISIBLE : View.GONE);
                        }
                    });
        }

        if(form != null) {
            form.setVisibility(showRefresh || showProgress ? View.GONE : View.VISIBLE);
            form.animate()
                    .setDuration(shortAnimTime)
                    .alpha(showLayout ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            form.setVisibility(!showRefresh && showLayout ? View.VISIBLE : View.GONE);
                        }
                    });
        }

        if (refresh != null) {
            refresh.setVisibility(showRefresh ? View.VISIBLE : View.GONE);
        }
    }
}
