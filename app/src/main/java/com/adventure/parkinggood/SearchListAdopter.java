package com.adventure.parkinggood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.List;

public class SearchListAdopter extends RecyclerView.Adapter<SearchListAdopter.ViewHolder>{

    List<Place> locations;
    List<Marker> markers;
    List<TextView> textViews = new ArrayList<>();
    Context context;
    MapActivity mapActivity;

    SearchListAdopter(List<Place> locations, List<Marker> markers, MapActivity mapActivity){
        this.locations = locations;
        this.markers = markers;
        this.mapActivity = mapActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.location_search_list, parent, false) ;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
          Place location = locations.get(position);
          holder.tv_title.setText(location.getName());
          holder.tv_road.setText(location.getAddress());
          holder.btn_select.setTag(position);
          if(textViews.size() > position){
              textViews.set(position, holder.tv_btn);
          }else {
              textViews.add(holder.tv_btn);
          }
          holder.btn_select.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  int pos = (int) view.getTag();
                  //
              }
          });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_road;
        TextView tv_btn;
        CardView btn_select;

        ViewHolder(View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_road = itemView.findViewById(R.id.tv_road);
            tv_btn = itemView.findViewById(R.id.tv_btn);
            btn_select = itemView.findViewById(R.id.btn_select);
        }
    }
}
