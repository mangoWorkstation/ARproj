package com.mango.arproj.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mango.arproj.R;

import java.util.HashMap;
import java.util.List;

public class RankResultAdapter extends ArrayAdapter<HashMap<String,String>> {

    private int resourceId;

    public RankResultAdapter(@NonNull Context context, int resource, @NonNull List<HashMap<String, String>> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        HashMap<String,String> rankItem = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        ImageView icon = view.findViewById(R.id.imageView_rank_result);
        TextView name = view.findViewById(R.id.textView_rank_result_name);
        TextView time = view.findViewById(R.id.textView_rank_result_time);
        icon.setImageResource(R.drawable.icon_appicon);
        name.setText("第"+String.valueOf(position+1)+"名：\n"+rankItem.get("name"));
        time.setText("用时："+rankItem.get("time")+"秒");
        return view;
    }
}
