package com.example.calendarapi.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.calendarapi.R;
import com.example.calendarapi.adapter.EventAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CalendarioActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    ListView listView;
    List<Event> eventosItems;
    TextView mesText, diaSemana,fecha;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private TextView mOutputText;

    GoogleAccountCredential login;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };
    private SimpleDateFormat simple=new SimpleDateFormat("MMMM");
    private SimpleDateFormat simple2=new SimpleDateFormat("EEE");
    private SimpleDateFormat simple3=new SimpleDateFormat("d");

    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);

        mesText = (TextView) findViewById(R.id.textMes);
        diaSemana = (TextView) findViewById(R.id.textWeak);
        fecha = (TextView) findViewById(R.id.textFecha);


        Calendar c = Calendar.getInstance();
        mesText.setText(simple.format(c.getTime()));
        diaSemana.setText(simple2.format(c.getTime()));
        fecha.setText(simple3.format(c.getTime()));

        listView = (ListView) findViewById(R.id.listaEvento);
        adapter=new EventAdapter(this,R.layout.calendario_eventos);

        listView.setAdapter(adapter);
        //setListAdapter(adapter);
        //getListView().setDividerHeight(10);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event evento = adapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(), DetailCalendarActivity.class);
                intent.putExtra("id", evento.getId());
                intent.putExtra("summary",evento.getSummary());
                intent.putExtra("location",evento.getLocation());
                intent.putExtra("description",evento.getDescription());
                startActivity(intent);
            }
        });


        login = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (login.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No hay conexion establecida.");
        } else {
            new CalendarQueryTask(login).execute();
        }
    }
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                login.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        login.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }



    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        login.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }

                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }

        //super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            getResultsFromApi();
            if (login.getSelectedAccountName() == null) {
                startActivityForResult(login.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            } else {
                if (isDeviceOnline()) {
                    new CalendarQueryTask(login).execute();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /*private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            apiAvailability.getErrorDialog(
                    CalendarioActivity.this,
                    connectionStatusCode,
                    REQUEST_GOOGLE_PLAY_SERVICES).show();
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }*/
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                CalendarioActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }


    private class CalendarQueryTask extends AsyncTask<Void, Void, List<Event>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private ProgressDialog progress=null;

        CalendarQueryTask(GoogleAccountCredential credential) {
            HttpTransport client = AndroidHttp.newCompatibleTransport();
            JsonFactory json = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    client, json, credential)
                    .build();
        }

        @Override
        protected List<Event> doInBackground(Void... params) {
            try {
                DateTime now = new DateTime(System.currentTimeMillis());

                Events events = mService.events()
                        .list("primary")
                        .setMaxResults(10).setTimeMin(now).setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                return events.getItems();
            } catch (Exception e) {
                // gestione los errores
                return null;
            }
        }



        @Override
        protected void onPreExecute() {
            progress=ProgressDialog.show(CalendarioActivity.this,"Google Calendar", "Conexion en Progreso...",true);
            progress.show();
        }

        @Override
        protected void onPostExecute(List<Event> output) {
            if (progress!=null)
                progress.dismiss();
            adapter.addAll(output);
        }
    }


}
