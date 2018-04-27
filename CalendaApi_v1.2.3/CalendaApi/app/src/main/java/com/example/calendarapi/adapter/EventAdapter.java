package com.example.calendarapi.adapter;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.calendarapi.R;
import com.example.calendarapi.activities.DetailCalendarActivity;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class EventAdapter extends ArrayAdapter<Event>{

    private List<Event> events;
    private Context context;
    private int layoutId;
    private SimpleDateFormat simple=new SimpleDateFormat("d MMMM");
    private SimpleDateFormat simple2=new SimpleDateFormat("h:mm a");

    public EventAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.context=context;
        layoutId=resource;
    }

    @Override
    public void addAll(Collection<? extends Event> collection) {
        clear();
        super.addAll(collection);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Event e=getItem(position);

        View v= LayoutInflater.from(context).inflate(layoutId, null);
        TextView txt_event= (TextView) v.findViewById(R.id.event);
        //TextView txt_date= (TextView) v.findViewById(R.id.date);
        TextView txt_time= (TextView)v.findViewById(R.id.time);

        Calendar cal=Calendar.getInstance();
        DateTime dt=e.getStart().getDateTime();
        if (dt==null)
            dt=e.getStart().getDate();
        cal.setTimeInMillis(dt.getValue());

        txt_event.setText(e.getSummary());
        //txt_date.setText(simple.format(cal.getTime()));
        txt_time.setText(simple2.format(cal.getTime()));

        return v;
    }

}