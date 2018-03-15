package br.com.httpfluidobjects.appdivinapolenta;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class GetChopeiraById {


    String url = "http://divinapolenta.cloud.fluidobjects.com/get_chopeiras";
    String item;
    String id_chopeira;
    int id_cerveja;
    chopeira retornada;


    private void getJSON() {
        String uri = url;

        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            StringBuilder sb = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String json;
            while ((json = bufferedReader.readLine()) != null) {
                sb.append(json + "\n");
            }
            sb.append("");
            String s = sb.toString().trim();

            JSONObject jsonObj = new JSONObject(s);
            JSONArray jsonArray = jsonObj.getJSONArray("chopeiras");

            int y = jsonArray.length();
            for (int i = 0; i < y; i++) {
                JSONObject jsonChopeiraObject = new JSONObject(jsonArray.getString(i));
                if (id_chopeira.equals(jsonChopeiraObject.getString("id"))) {
                    retornada.setId(jsonChopeiraObject.getString("id"));
                    retornada.setTitle(jsonChopeiraObject.getString("title"));
                    retornada.setIdCeva(jsonChopeiraObject.getString("cerveja"));
                    retornada.setQuantidade(jsonChopeiraObject.getString("quantidade"));
                    retornada.setNid(jsonChopeiraObject.getString("nid"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public chopeira getDados(String id_chopeira) {
        retornada = new chopeira();
        this.id_chopeira = id_chopeira;

        getJSON();
        return retornada;
    }
}
