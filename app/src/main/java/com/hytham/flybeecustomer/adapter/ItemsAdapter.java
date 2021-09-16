package com.hytham.flybeecustomer.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.hytham.flybeecustomer.R;
import com.hytham.flybeecustomer.activity.MainActivity;
import com.hytham.flybeecustomer.fragment.TrackerDetailFragment;
import com.hytham.flybeecustomer.helper.ApiConfig;
import com.hytham.flybeecustomer.helper.Constant;
import com.hytham.flybeecustomer.helper.Session;
import com.hytham.flybeecustomer.helper.VolleyCallback;
import com.hytham.flybeecustomer.model.OrderTracker;

import static com.hytham.flybeecustomer.fragment.TrackerDetailFragment.pBar;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemHolder> {

    final Activity activity;
    final ArrayList<OrderTracker> orderTrackerArrayList;
    final Session session;
    String from = "";

    public ItemsAdapter(Activity activity, ArrayList<OrderTracker> orderTrackerArrayList, String from) {
        this.activity = activity;
        this.orderTrackerArrayList = orderTrackerArrayList;
        this.from = from;
        session = new Session(activity);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_items, null);
        return new ItemHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ItemHolder holder, final int position) {

        final OrderTracker order = orderTrackerArrayList.get(position);

        String payType = "";
        if (order.getPayment_method().equalsIgnoreCase("cod"))
            payType = activity.getResources().getString(R.string.cod);
        else
            payType = order.getPayment_method();
        String activeStatus = order.getActiveStatus().substring(0, 1).toUpperCase() + order.getActiveStatus().substring(1).toLowerCase();
        holder.txtqty.setText(order.getQuantity());

        String taxPercentage = order.getTax_percent();
        double price;

        if (order.getDiscounted_price().equals("0") || order.getDiscounted_price().equals("")) {
            price = ((Float.parseFloat(order.getPrice()) + ((Float.parseFloat(order.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
        } else {
            price = ((Float.parseFloat(order.getDiscounted_price()) + ((Float.parseFloat(order.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
        }
        holder.txtprice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + price));

        holder.txtpaytype.setText(activity.getResources().getString(R.string.via) + payType);
        holder.txtstatus.setText(activeStatus);
        if (activeStatus.equalsIgnoreCase(Constant.AWAITING_PAYMENT)) {
            holder.txtstatus.setText(activity.getString(R.string.awaiting_payment));
        }
        holder.txtstatusdate.setText(order.getActiveStatusDate());
        holder.txtname.setText(order.getName() + "(" + order.getMeasurement() + order.getUnit() + ")");

        Picasso.get().
                load(order.getImage())
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgorder);

        holder.carddetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new TrackerDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", "");
                bundle.putSerializable("model", order);
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            }
        });

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateOrderStatus(activity, order, Constant.CANCELLED, holder, from);
            }
        });

        holder.btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                Date date = new Date();
                //System.out.println (myFormat.format (date));
                String inputString1 = order.getActiveStatusDate();
                String inputString2 = myFormat.format(date);
                try {
                    Date date1 = myFormat.parse(inputString1);
                    Date date2 = myFormat.parse(inputString2);
                    long diff = date2.getTime() - date1.getTime();
                    //  System.out.println("Days: "+TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

                    if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= Integer.parseInt(new Session(activity).getData(Constant.max_product_return_days))) {
                        updateOrderStatus(activity, order, Constant.RETURNED, holder, from);

                    } else {
                        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getResources().getString(R.string.product_return) + Integer.parseInt(new Session(activity).getData(Constant.max_product_return_days)) + activity.getString(R.string.day_max_limit), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(activity.getResources().getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();

                            }
                        });
                        snackbar.setActionTextColor(Color.RED);
                        View snackbarView = snackbar.getView();
                        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setMaxLines(5);
                        snackbar.show();

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        if (from.equals("detail")) {
            if (order.getActiveStatus().equalsIgnoreCase("delivered") && session.getData(Constant.ratings).equals("1")) {
                holder.lytRatings.setVisibility(View.VISIBLE);
                if (Boolean.parseBoolean(order.getReview_status())) {
                    holder.ratingProduct.setRating(Float.parseFloat(order.getRate()));
                    holder.tvAddUpdateReview.setText(R.string.update_review);
                }
            } else {
                holder.lytRatings.setVisibility(View.GONE);
            }

            holder.ratingProduct.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    AddUpdateReview(holder, order, ratingBar.getRating(), order.getReview(), Boolean.parseBoolean(order.getReview_status()), order.getProduct_id(), position);
                }
            });

            holder.tvAddUpdateReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddUpdateReview(holder, order, holder.ratingProduct.getRating(), order.getReview(), Boolean.parseBoolean(order.getReview_status()), order.getProduct_id(), position);
                }
            });

            if (order.getActiveStatus().equalsIgnoreCase("cancelled")) {
                holder.txtstatus.setTextColor(Color.RED);
                holder.btnCancel.setVisibility(View.GONE);
            } else if (order.getActiveStatus().equalsIgnoreCase("delivered")) {
                holder.btnCancel.setVisibility(View.GONE);
                if (order.getReturn_status().equalsIgnoreCase("1")) {
                    holder.btnReturn.setVisibility(View.VISIBLE);
                } else {
                    holder.btnReturn.setVisibility(View.GONE);
                }
            } else if (order.getActiveStatus().equalsIgnoreCase("returned")) {
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnReturn.setVisibility(View.GONE);
            } else {
                if (order.getCancelable_status().equalsIgnoreCase("1")) {
                    if (order.getTill_status().equalsIgnoreCase("received")) {
                        if (order.getActiveStatus().equalsIgnoreCase("received")) {
                            holder.btnCancel.setVisibility(View.VISIBLE);
                        } else {
                            holder.btnCancel.setVisibility(View.GONE);
                        }
                    } else if (order.getTill_status().equalsIgnoreCase("processed")) {
                        if (order.getActiveStatus().equalsIgnoreCase("received") || order.getActiveStatus().equalsIgnoreCase("processed")) {
                            holder.btnCancel.setVisibility(View.VISIBLE);
                        } else {
                            holder.btnCancel.setVisibility(View.GONE);
                        }
                    } else if (order.getTill_status().equalsIgnoreCase("shipped")) {
                        if (order.getActiveStatus().equalsIgnoreCase("received") || order.getActiveStatus().equalsIgnoreCase("processed") || order.getActiveStatus().equalsIgnoreCase("shipped")) {
                            holder.btnCancel.setVisibility(View.VISIBLE);
                        } else {
                            holder.btnCancel.setVisibility(View.GONE);
                        }
                    }
                } else {
                    holder.btnCancel.setVisibility(View.GONE);
                }
            }
        }
    }

    private void updateOrderStatus(final Activity activity, final OrderTracker order, final String status, final ItemHolder holder, final String from) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        if (status.equals(Constant.CANCELLED)) {
            alertDialog.setTitle(activity.getResources().getString(R.string.cancel_order));
            alertDialog.setMessage(activity.getResources().getString(R.string.cancel_msg));
        } else if (status.equals(Constant.RETURNED)) {
            alertDialog.setTitle(activity.getResources().getString(R.string.return_order));
            alertDialog.setMessage(activity.getResources().getString(R.string.return_msg));
        }
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final Map<String, String> params = new HashMap<>();
                params.put(Constant.UPDATE_ORDER_ITEM_STATUS, Constant.GetVal);
                params.put(Constant.ORDER_ITEM_ID, order.getId());
                params.put(Constant.ORDER_ID, order.getOrder_id());
                params.put(Constant.STATUS, status);
                if (pBar != null)
                    pBar.setVisibility(View.VISIBLE);
                ApiConfig.RequestToVolley(new VolleyCallback() {
                    @Override
                    public void onSuccess(boolean result, String response) {
                        // System.out.println("================= " + response);
                        if (result) {
                            try {
                                JSONObject object = new JSONObject(response);
                                if (!object.getBoolean(Constant.ERROR)) {
                                    if (status.equals(Constant.CANCELLED)) {
                                        holder.btnCancel.setVisibility(View.GONE);
                                        holder.txtstatus.setText(status);
                                        holder.txtstatus.setTextColor(Color.RED);
                                        order.status = status;
                                        if (from.equals("detail")) {
                                            if (orderTrackerArrayList.size() == 1) {
                                                TrackerDetailFragment.btnCancel.setVisibility(View.GONE);
                                                TrackerDetailFragment.lyttracker.setVisibility(View.GONE);
                                            }
                                        }
                                        ApiConfig.getWalletBalance(activity, new Session(activity));
                                    } else {
                                        holder.btnReturn.setVisibility(View.GONE);
                                        holder.txtstatus.setText(status);
                                    }
                                    Constant.isOrderCancelled = true;
                                }
                                Toast.makeText(activity, object.getString("message"), Toast.LENGTH_LONG).show();
                                if (pBar != null)
                                    pBar.setVisibility(View.GONE);
                            } catch (JSONException e) {

                            }
                        }
                    }
                }, activity, Constant.ORDERPROCESS_URL, params, false);

            }
        });
        alertDialog.setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog1.dismiss();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return orderTrackerArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        final TextView txtqty;
        final TextView txtprice;
        final TextView txtpaytype;
        final TextView txtstatus;
        final TextView txtstatusdate;
        final TextView txtname;
        final ImageView imgorder;
        final CardView carddetail;
        final RecyclerView recyclerView;
        final Button btnCancel;
        final Button btnReturn;
        final LinearLayout returnLyt;
        RelativeLayout lytRatings;
        RatingBar ratingProduct;
        TextView tvAddUpdateReview;

        public ItemHolder(View itemView) {
            super(itemView);

            txtqty = itemView.findViewById(R.id.txtqty);
            txtprice = itemView.findViewById(R.id.txtprice);
            txtpaytype = itemView.findViewById(R.id.txtpaytype);
            txtstatus = itemView.findViewById(R.id.txtstatus);
            txtstatusdate = itemView.findViewById(R.id.txtstatusdate);
            txtname = itemView.findViewById(R.id.txtname);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            imgorder = itemView.findViewById(R.id.imgorder);
            carddetail = itemView.findViewById(R.id.carddetail);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            btnReturn = itemView.findViewById(R.id.btnReturn);
            returnLyt = itemView.findViewById(R.id.returnLyt);

            lytRatings = itemView.findViewById(R.id.lytRatings);
            ratingProduct = itemView.findViewById(R.id.ratingProduct);
            tvAddUpdateReview = itemView.findViewById(R.id.tvAddUpdateReview);
        }
    }

    public void AddUpdateReview(ItemHolder holder, OrderTracker orderTracker, Float rating, String review, boolean isUpdate, String productId, int position) {
        try {
            View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_review, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetTheme);
            mBottomSheetDialog.setContentView(sheetView);
            if (!new Session(activity).getBoolean("update_skip")) {
                mBottomSheetDialog.show();
            }

            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            ImageView imgClose = sheetView.findViewById(R.id.imgClose);
            RatingBar ratingProduct = sheetView.findViewById(R.id.ratingProduct);
            EditText edtReviewMessage = sheetView.findViewById(R.id.edtReviewMessage);
            Button btnCancel = sheetView.findViewById(R.id.btnCancel);
            Button btnVerify = sheetView.findViewById(R.id.btnVerify);

            mBottomSheetDialog.setCancelable(true);
            if (isUpdate) {
                edtReviewMessage.setText(review);
            }
            ratingProduct.setRating(rating);

            imgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetDialog.dismiss();
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetDialog.dismiss();
                }
            });

            btnVerify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (pBar != null)
                        pBar.setVisibility(View.VISIBLE);
                    SetReview(holder, orderTracker, ratingProduct.getRating(), edtReviewMessage.getText().toString(), productId, mBottomSheetDialog);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SetReview(ItemHolder holder, OrderTracker orderTracker, Float rating, String review, String productId, BottomSheetDialog mBottomSheetDialog) {
        {
            final Map<String, String> params = new HashMap<>();
            params.put(Constant.ADD_PRODUCT_REVIEW, Constant.GetVal);
            params.put(Constant.PRODUCT_ID, productId);
            params.put(Constant.USER_ID, session.getData(Constant.ID));
            params.put(Constant.RATE, "" + rating);
            params.put(Constant.REVIEW, review);
            if (pBar != null)
                pBar.setVisibility(View.VISIBLE);
            ApiConfig.RequestToVolley(new VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {
                    // System.out.println("================= " + response);
                    if (result) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean(Constant.ERROR)) {
                                holder.ratingProduct.setRating(rating);
                                holder.tvAddUpdateReview.setText(R.string.update_review);
                                holder.tvAddUpdateReview.setText(R.string.update_review);
                                orderTracker.setReview_status("true");
                                orderTracker.setRate("" + rating);
                                orderTracker.setReview(review);
                                notifyDataSetChanged();
                            }
                            Toast.makeText(activity, object.getString("message"), Toast.LENGTH_LONG).show();
                            mBottomSheetDialog.dismiss();
                            if (pBar != null)
                                pBar.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, activity, Constant.GET_ALL_PRODUCTS_URL, params, false);

        }
    }


}
