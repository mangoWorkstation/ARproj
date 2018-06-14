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
import com.mango.arproj.entity.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RecordResultAdapter extends ArrayAdapter<Record> {

    private int resourceId;

    public RecordResultAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Record> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Record record = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        ImageView icon = view.findViewById(R.id.imageView_record_result);
        TextView time = view.findViewById(R.id.textView_record_time_interval);
        TextView arCount = view.findViewById(R.id.textView_rank_result_ar_count);
        TextView stepCount = view.findViewById(R.id.textView_rank_result_step_count);

        String startDate = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(Long.valueOf(record.getStart_t())*1000));
        String endDate = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(Long.valueOf(record.getEnd_t())*1000));

        icon.setImageResource(R.drawable.icon_appicon);
        time.setText("时间："+startDate+" - "+endDate);
        arCount.setText("AR数量："+String.valueOf(record.getArCount()));
        stepCount.setText("步数："+String.valueOf(record.getStepCount()));
        return view;
    }
}
