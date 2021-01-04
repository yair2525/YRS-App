package com.example.yrs_app.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yrs_app.Interface.itemClickListener;
import com.example.yrs_app.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView food_name, food_price;
    public ImageView food_image;
    public ImageView fav_image;
    public ImageView quick_cart;



    private itemClickListener itemClickListener;

    public void setItemClickListener(com.example.yrs_app.Interface.itemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(View itemView) {
        super(itemView);

        food_name = (TextView) itemView.findViewById(R.id.food_name);
        food_price = (TextView) itemView.findViewById(R.id.food_price);
        food_image = (ImageView) itemView.findViewById(R.id.food_image);
        fav_image = (ImageView) itemView.findViewById(R.id.fav);
        quick_cart = (ImageView) itemView.findViewById(R.id.btn_quick_cart);


        itemView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
