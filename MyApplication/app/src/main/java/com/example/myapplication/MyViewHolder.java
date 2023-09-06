package com.example.myapplication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    ImageView picture;
    TextView name;

    public MyViewHolder(@NonNull View itemView){
        super(itemView);
        picture=itemView.findViewById(R.id.Picture);
        name=itemView.findViewById(R.id.Name);
    }
}
