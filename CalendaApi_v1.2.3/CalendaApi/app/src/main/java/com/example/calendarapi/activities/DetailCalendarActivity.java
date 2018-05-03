package com.example.calendarapi.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.TextView;

import com.example.calendarapi.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static okhttp3.internal.http.HttpDate.parse;

public class DetailCalendarActivity extends AppCompatActivity {

    TextView detail,location,evento,hora,id;
    private SimpleDateFormat simple=new SimpleDateFormat("h:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_calendar);

        id = (TextView) findViewById(R.id.detalleId);
        detail = (TextView)findViewById(R.id.textDescripcion);
        location = (TextView) findViewById(R.id.textLocation);
        evento = (TextView) findViewById(R.id.textEvent);
        hora = (TextView)findViewById(R.id.hora);

        id.setText(getIntent().getStringExtra("id"));
        evento.setText(getIntent().getStringExtra("summary"));
        location.setText(getIntent().getStringExtra("location"));
        detail.setText(getIntent().getStringExtra("description"));
        hora.setText(getIntent().getStringExtra("startTime").toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    public static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);

        now.add(Calendar.DATE, -1);

        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

    public static boolean isTomorrow(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);

        now.add(Calendar.DATE, +1);

        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }
    public static boolean isToday(long date){
        return DateUtils.isToday(date);
    }
}
