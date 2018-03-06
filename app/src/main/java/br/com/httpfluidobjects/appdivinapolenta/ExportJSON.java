package br.com.httpfluidobjects.appdivinapolenta;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gabrielweich on 08/11/17.
 */

public class ExportJSON {
    public static void sendJSON(String url, final String data)  {
        class SendJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {

                String s = "";
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty( "Content-Type", "application/json");
                    httpURLConnection.setRequestProperty( "charset", "utf-8");
                    httpURLConnection.setDoInput(true);

                    DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                    wr.writeBytes(data);

                    int responseCode = httpURLConnection.getResponseCode();

                    InputStream in = httpURLConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(in);
                    int inputStreamData = inputStreamReader.read();
                    while (inputStreamData != -1) {
                        char current = (char) inputStreamData;
                        inputStreamData = inputStreamReader.read();
                        s += current;
                    }
                    s += "";
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
                return s;

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        }
        SendJSON sj = new SendJSON();
        sj.execute(url);
    }
}
