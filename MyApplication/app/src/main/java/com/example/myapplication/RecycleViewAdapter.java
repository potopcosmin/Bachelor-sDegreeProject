package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.DataModel.CarService;
import com.example.myapplication.DataModel.Service;

import java.util.ArrayList;
import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<MyViewHolder> {
    Context context;
    List<CarService> services;

    public RecycleViewAdapter(Context context, List<CarService> services) {
        this.context = context;
        this.services = services;
    }
    public void filterList(ArrayList<CarService> filterlist) {
        services = filterlist;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.service_view,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CarService curService=services.get(position);
        holder.name.setText(curService.getServiceName());
        holder.picture.setImageResource(R.drawable.service);


    }

    @Override
    public int getItemCount() {
        return services.toArray().length;
    }
}
