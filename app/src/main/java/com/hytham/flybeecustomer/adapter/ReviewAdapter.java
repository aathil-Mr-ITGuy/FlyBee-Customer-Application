package com.hytham.flybeecustomer.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.hytham.flybeecustomer.R;
import com.hytham.flybeecustomer.model.Review;
import com.hytham.flybeecustomer.ui.CircleTransform;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final ArrayList<Review> Reviews;
    public boolean isLoading;
    String id = "0";


    public ReviewAdapter(Activity activity, ArrayList<Review> Reviews) {
        this.activity = activity;
        this.Reviews = Reviews;
    }

    public void add(int position, Review item) {
        Reviews.add(position, item);
        notifyItemInserted(position);
    }

    public void setLoaded() {
        isLoading = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.lyt_review_list, parent, false);
            return new ReviewItemHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
            return new ViewHolderLoading(view);
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderparent, final int position) {

        if (holderparent instanceof ReviewItemHolder) {
            final ReviewItemHolder holder = (ReviewItemHolder) holderparent;
            final Review review = Reviews.get(position);

            Picasso.get()
                    .load(review.getUser_profile().isEmpty() ? "-" : review.getUser_profile())
                    .fit()
                    .centerInside()
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgProfile);

            holder.ratingReview.setRating(Float.parseFloat(review.getRatings()));
            holder.tvDate.setText(activity.getString(R.string.reviewed_on) + review.getDate_added().split(" ")[0]);
            holder.tvName.setText(review.getUsername());
            holder.tvMessage.setText(review.getReview());

        } else if (holderparent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderparent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return Reviews.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Reviews.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        Review product = Reviews.get(position);
        if (product != null)
            return Integer.parseInt(product.getId());
        else
            return position;
    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = view.findViewById(R.id.itemProgressbar);
        }
    }

    class ReviewItemHolder extends RecyclerView.ViewHolder {

        final ImageView imgProfile;
        final TextView tvName, tvDate, tvMessage;
        RatingBar ratingReview;


        public ReviewItemHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            ratingReview = itemView.findViewById(R.id.ratingReview);
        }
    }
}
