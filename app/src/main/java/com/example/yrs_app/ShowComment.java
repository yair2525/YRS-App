package com.example.yrs_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.example.yrs_app.Common.Common;
import com.example.yrs_app.Model.Rating;
import com.example.yrs_app.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ShowComment extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout mSwipeRefreshLayout;

    FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder> adapter;

    String foodId="";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        super.onStop();
        //if(adapter != null)
            //adapter.stop
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/restaurant_font.otf")
        .setFontAttrId(R.attr.fontPath)
        .build());

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);


        setContentView(R.layout.activity_show_comment);

        //Firebase
        database = FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerComment);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getIntent() != null)
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if(!foodId.isEmpty() && foodId != null)
                {
                    //Create request query
                    //Query query = ratingTbl.orderByChild("foodId").equalTo(foodId);

                   /* FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();*/

                   adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(Rating.class,
                           R.layout.show_comment_layout,
                           ShowCommentViewHolder.class,
                           ratingTbl.orderByChild("foodId").equalTo(foodId)
                   ) {
                       @Override
                       protected void populateViewHolder(ShowCommentViewHolder showCommentViewHolder, Rating rating, int i) {

                           showCommentViewHolder.ratingBar.setRating(Float.parseFloat(rating.getRateValue()));
                           showCommentViewHolder.txtComment.setText(rating.getComment());
                           showCommentViewHolder.txtUserPhone.setText(rating.getUserPhone());

                       }

                   };

                   loadComment(foodId);
                }
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);

                if(getIntent() != null)
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if(!foodId.isEmpty() && foodId != null)
                {
                    //Create request query
                    //Query query = ratingTbl.orderByChild("foodId").equalTo(foodId);

                   /* FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();*/

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(Rating.class,
                            R.layout.show_comment_layout,
                            ShowCommentViewHolder.class,
                            ratingTbl.orderByChild("foodId").equalTo(foodId)
                    ) {
                        @Override
                        protected void populateViewHolder(ShowCommentViewHolder showCommentViewHolder, Rating rating, int i) {

                            showCommentViewHolder.ratingBar.setRating(Float.parseFloat(rating.getRateValue()));
                            showCommentViewHolder.txtComment.setText(rating.getComment());
                            showCommentViewHolder.txtUserPhone.setText(rating.getUserPhone());

                        }

                    };

                    loadComment(foodId);
                }
            }
        });


    }

    private void loadComment(String foodId) {
        //adapter.startListening();

        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}