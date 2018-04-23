package com.example.calendarapi.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.calendarapi.R;

public class DetailCalendarActivity extends AppCompatActivity {

    TextView detail,location,evento,hora,id;

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

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
}
