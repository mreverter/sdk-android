package com.mercadopago.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mercadopago.R;
import com.mercadopago.model.BankDeal;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BankDealsAdapter extends  RecyclerView.Adapter<BankDealsAdapter.ViewHolder> {

    private Activity mActivity;
    private List<BankDeal> mData;
    private View.OnClickListener mListener = null;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mBankDescView;
        public ImageView mBankImageView;
        public TextView mInstallmentsView;

        public ViewHolder(View v, View.OnClickListener listener) {

            super(v);
            mBankDescView = (TextView) v.findViewById(R.id.bank_desc);
            mBankImageView = (ImageView) v.findViewById(R.id.bank_img);
            mInstallmentsView = (TextView) v.findViewById(R.id.installments);
            if (listener != null) {
                v.setOnClickListener(listener);
            }
        }
    }

    public BankDealsAdapter(Activity activity, List<BankDeal> data, View.OnClickListener listener) {

        mActivity = activity;
        mData = data;
        mListener = listener;
    }

    @Override
    public BankDealsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_bank_deals, parent, false);

        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        BankDeal bankDeal = mData.get(position);

        // Set bank description
        holder.mBankDescView.setText(getBankDesc(bankDeal));
        if (!holder.mBankDescView.getText().equals("")) {
            holder.mBankDescView.setVisibility(View.VISIBLE);
        } else {
            holder.mBankDescView.setVisibility(View.GONE);
        }

        // Set bank image
        String issuerId = String.valueOf(bankDeal.getIssuer() != null ? bankDeal.getIssuer().getId() : 0);
        Picasso.with(mActivity)
                .load(mActivity.getString(R.string.bank_deals_image_location) + "ico_bank_" + issuerId + ".png")
                .into(holder.mBankImageView);

        // Set installments
        holder.mInstallmentsView.setText(Html.fromHtml(getInstallments(bankDeal)));

        // Set view tag item
        holder.itemView.setTag(bankDeal);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public BankDeal getItem(int position) {
        return mData.get(position);
    }

    public View.OnClickListener getListener() { return mListener; }

    private String getBankDesc(BankDeal bankDeal) {

        if (bankDeal.getPaymentMethods() != null) {
            String desc = "";
            for (int i = 0; i < bankDeal.getPaymentMethods().size(); i++){
                desc += bankDeal.getPaymentMethods().get(i).getName();
                desc +=" ";
                if (bankDeal.getPaymentMethods().size() > i + 2) {
                    desc += mActivity.getString(R.string.comma_separator) + " ";
                } else if (bankDeal.getPaymentMethods().size() > i + 1) {
                    desc += mActivity.getString(R.string.and) + " ";
                }
            }
            return desc;
        }
        return "";
    }

    private String getInstallments(BankDeal bankDeal) {

        String result = "";

        if (bankDeal != null && bankDeal.getMaxInstallments() >  0) {
            result = mActivity.getString(R.string.bank_zero_rate, bankDeal.getMaxInstallments());
        }
        return result;
    }
}
