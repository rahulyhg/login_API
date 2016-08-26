package com.example.gigalodon.login_api;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    Button login;
    TextView link, lbl_http_connection;
    HttpURLConnection connection;
    BufferedReader reader;


    @Override

    protected void onCreate(Bundle savedInstanceState) {


        SharedPreferences get_shared_preference = getSharedPreferences("authentication", MODE_PRIVATE);
        if (get_shared_preference.getString("token_authentication", "") == null) {
            Intent intent_obj = new Intent(this, Member.class);
            startActivity(intent_obj);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.edtuser);
        password = (EditText) findViewById(R.id.edtpass);

        login = (Button) findViewById(R.id.button);

        login.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                //localhost or 127.0.0.1 , is refer to emulator device it self
                // use 10.0.2.2, for access local server
                new ApiConnect().execute("http://private-cc41c4-signup30.apiary-mock.com/questions");
                //new ApiConnect().execute("http://10.0.2.2:3000/");
            }

        });
    }


//this method for handle http connection

    public String get_data(String url_target) {
        String line = "";
        try {
            URL url = new URL(url_target);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            //this will return to onPostExecute when doInBackground finished

            return buffer.toString();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            if (connection != null) connection.disconnect();

            try {

                if (reader != null) reader.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }

        return null;

    }

//this method for handel json parse

    public void process_json(String json_str) throws JSONException {

        try {

            JSONObject api_json = new JSONObject(json_str);

            JSONArray users = api_json.getJSONArray("users");
            boolean userdata = false;
            for (int i = 0; i < users.length(); i++) {

                JSONObject user = users.getJSONObject(i);
                if (user.getString("password").equals(password.getText().toString()) && user.getString("email").equals(username.getText().toString())) {
                    Intent intent_obj = new Intent(this, Member.class);
                    startActivity(intent_obj);
                    finish();
                    SharedPreferences set_shared_preference = getSharedPreferences("authentication", MODE_PRIVATE);
                    SharedPreferences.Editor sp_editor = set_shared_preference.edit();
                    sp_editor.putString("token_authentication", user.getString("token_auth"));
                    sp_editor.commit();

                    Log.e("Log", "Login success");
                    userdata = true;
                }
            }
            if (userdata == false) {
                Toast.makeText(getApplicationContext(), "Your data is invalid!", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();

        }

    }

    class ApiConnect extends AsyncTask<String, String, String> {
        ProgressDialog progress_dialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            //unseen_dialog.setCancelable(false);
            //unseen_dialog.show();
            // this for init progress dialog
            // progress_dialog.setTitle("On Progress ....");
            //progress_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress_dialog.setCancelable(true);
            progress_dialog.setMessage("Please wait");
            progress_dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            return get_data(params[0]);

        }

        @Override

        protected void onPostExecute(String s) {

            super.onPostExecute(s);

            try {

                process_json(s);
                progress_dialog.dismiss();
            } catch (JSONException e) {

                e.printStackTrace();

            }

        }

    }
}
