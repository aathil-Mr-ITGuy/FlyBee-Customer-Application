package com.hytham.flybeecustomer.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hytham.flybeecustomer.R;
import com.hytham.flybeecustomer.adapter.ReviewAdapter;
import com.hytham.flybeecustomer.helper.ApiConfig;
import com.hytham.flybeecustomer.helper.Constant;
import com.hytham.flybeecustomer.helper.Session;
import com.hytham.flybeecustomer.helper.VolleyCallback;
import com.hytham.flybeecustomer.model.Review;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class ReviewFragment extends Fragment {
    View root;

    RecyclerView recyclerView;
    ArrayList<Review> reviewArrayList;
    ReviewAdapter reviewAdapter;
    SwipeRefreshLayout swipeLayout;
    NestedScrollView scrollView;
    RelativeLayout tvAlert;
    int total = 0;
    LinearLayoutManager linearLayoutManager;
    Activity activity;
    int offset = 0;
    Session session;
    boolean isLoadMore = false;
    String from, productid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_review, container, false);

        activity = getActivity();

        session = new Session(activity);

        from = getArguments().getString(Constant.FROM);
        productid = getArguments().getString(Constant.ID);

        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        scrollView = root.findViewById(R.id.scrollView);
        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        setHasOptionsMenu(true);


        if (ApiConfig.isConnected(activity)) {
            getNotificationData();
        }

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (reviewArrayList != null) {
                    reviewArrayList = null;
                }
                offset = 0;
                getNotificationData();
                swipeLayout.setRefreshing(false);
            }
        });


        return root;
    }


    void getNotificationData() {
        reviewArrayList = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_PRODUCT_REVIEW, Constant.GetVal);
        params.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 10));
        params.put(Constant.OFFSET, "" + offset);
        if (from.equals("share")) {
            params.put(Constant.SLUG, productid);
        } else {
            params.put(Constant.PRODUCT_ID, productid);
        }

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            total = Integer.parseInt(jsonObject.getString(Constant.NUMBER_OF_REVIEW));
                            session.setData(Constant.TOTAL, String.valueOf(total));

                            JSONArray jsonArrayReviews = jsonObject.getJSONArray(Constant.PRODUCT_REVIEW);

                            for (int i = 0; i < (Math.min(jsonArrayReviews.length(), 5)); i++) {
                                Review review = new Gson().fromJson(jsonArrayReviews.getJSONObject(i).toString(), Review.class);
                                reviewArrayList.add(review);
                            }
                            if (offset == 0) {
                                reviewAdapter = new ReviewAdapter(activity, reviewArrayList);
                                reviewAdapter.setHasStableIds(true);
                                recyclerView.setAdapter(reviewAdapter);
                                scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    @Override
                                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                                        // if (diff == 0) {
                                        if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                            if (reviewArrayList.size() < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == reviewArrayList.size() - 1) {
                                                        //bottom of list!
                                                        reviewArrayList.add(null);
                                                        reviewAdapter.notifyItemInserted(reviewArrayList.size() - 1);
                                                        offset += Constant.LOAD_ITEM_LIMIT + 10;
                                                        Map<String, String> params = new HashMap<>();
                                                        params.put(Constant.GET_PRODUCT_REVIEW, Constant.GetVal);
                                                        params.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 10));
                                                        params.put(Constant.OFFSET, "" + offset);
                                                        if (from.equals("share")) {
                                                            params.put(Constant.SLUG, productid);
                                                        } else {
                                                            params.put(Constant.PRODUCT_ID, productid);
                                                        }

                                                        ApiConfig.RequestToVolley(new VolleyCallback() {
                                                            @Override
                                                            public void onSuccess(boolean result, String response) {

                                                                if (result) {
                                                                    try {
                                                                        JSONObject objectbject1 = new JSONObject(response);
                                                                        if (!objectbject1.getBoolean(Constant.ERROR)) {

                                                                            session.setData(Constant.TOTAL, objectbject1.getString(Constant.TOTAL));

                                                                            reviewArrayList.remove(reviewArrayList.size() - 1);
                                                                            reviewAdapter.notifyItemRemoved(reviewArrayList.size());

                                                                            JSONArray jsonArrayReviews = jsonObject.getJSONArray(Constant.PRODUCT_REVIEW);

                                                                            for (int i = 0; i < (Math.min(jsonArrayReviews.length(), 5)); i++) {
                                                                                Review review = new Gson().fromJson(jsonArrayReviews.getJSONObject(i).toString(), Review.class);
                                                                                reviewArrayList.add(review);
                                                                            }
                                                                            reviewAdapter = new ReviewAdapter(activity, reviewArrayList);
                                                                            recyclerView.setAdapter(reviewAdapter);

                                                                            reviewAdapter.notifyDataSetChanged();
                                                                            reviewAdapter.setLoaded();
                                                                            isLoadMore = false;
                                                                        }
                                                                    } catch (JSONException e) {

                                                                    }
                                                                }
                                                            }
                                                        }, activity, Constant.GET_ALL_PRODUCTS_URL, params, false);

                                                    }
                                                    isLoadMore = true;
                                                }

                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            tvAlert.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {

                    }
                }
            }
        }, activity, Constant.GET_ALL_PRODUCTS_URL, params, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.reviews);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {

        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }


}