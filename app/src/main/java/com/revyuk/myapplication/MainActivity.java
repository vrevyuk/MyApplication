package com.revyuk.myapplication;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


public class MainActivity extends ActionBarActivity {
    final Context context = this;
    String pattern = "apptest.com/I?Id=";
    String urltoSite = "http://app.mobilenobo.com/c/apptest?id=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver contentResolver = getContentResolver();
                Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"),new String[]{"address", "body"}, null, null, null);
                String str;
                String s1;
                while(cursor.moveToNext()) {

                    str = cursor.getString(1);
                    Log.d("XXX", str);
                    if(str.contains(pattern)) {
                        s1 = str.substring(str.indexOf(pattern)+pattern.length(), str.length());
                        StringBuilder sb = new StringBuilder("");
                        for(int x=0; x<s1.length(); x++) {
                            if(s1.substring(x,x+1).matches("\\w")) {
                                sb.append(s1.toCharArray()[x]);
                            } else break;
                        }
                        //Log.d("XXX", "<<>>"+sb.toString());
                        urltoSite = urltoSite+sb.toString();
                        if(sb.length()>0) {
                            AlertDialog ad = new AlertDialog.Builder(context)
                                    .setMessage("Found:\nFrom:"+cursor.getString(0)+"\nMessage:"+cursor.getString(1)+"\nID:"+sb.toString())
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MyAsyncTask asyncTask = new MyAsyncTask();
                                            asyncTask.execute(urltoSite);

                                        }
                                    })
                                    .setCancelable(true)
                                    .show();
                        }
                    }
                }
                cursor.close();

            }
        });
    }

    class MyAsyncTask extends AsyncTask<String, Void, String> {
        String urlStr, returnStr;

        String convertStreamToString(InputStream inputStreams) {
            Scanner s = new Scanner(inputStreams).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }

        @Override
        protected String doInBackground(String... params) {
            if(params!=null) urlStr = params[0];
            URL url = null;
            try {
                url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept-Charset", "UTF-8");
                con.setUseCaches(false);
                con.setDoInput(true);
                if(con.getResponseCode()==200) {
                    InputStream is = con.getInputStream();
                    returnStr = convertStreamToString(is);
                    is.close();
                }
                con.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return returnStr;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            AlertDialog ad = new AlertDialog.Builder(context)
                    .setMessage("Return:"+s)
                    .setPositiveButton("Ok", null)
                    .setCancelable(true)
                    .show();
        }
    }
}
