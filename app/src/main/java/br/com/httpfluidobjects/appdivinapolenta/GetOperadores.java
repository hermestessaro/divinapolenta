package br.com.httpfluidobjects.appdivinapolenta;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by gabrielweich on 30/10/17.
 */

public class GetOperadores {
    private ArrayList<br.com.httpfluidobjects.appdivinapolenta.operador> dados;
    operador operador;
    String url = "http://divinapolenta.cloud.fluidobjects.com/get_operadores";

    public GetOperadores() {
        dados = new ArrayList<operador>();
    }


    private void getJSON(String url) throws ExecutionException, InterruptedException {
        class GetJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);

                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;

                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    sb.append("");
                    String s = sb.toString().trim();
                    Log.d("operador", s);

                    JSONObject jsonObj = new JSONObject(s);
                    JSONArray jsonArray = jsonObj.getJSONArray("operadores");

                    int y = jsonArray.length();
                    for(int i = 0; i < y; i++)
                    {
                        JSONObject jsonChopeiraObject = new JSONObject(jsonArray.getString(i)); //pega o primeiro elemento desse Array, transforma em string e cria um novo objeto

                        operador = new operador();
                        operador.setId(Integer.parseInt(jsonChopeiraObject.getString("vid")));
                        operador.setCartao(jsonChopeiraObject.getString("cartao"));
                        operador.setNome(jsonChopeiraObject.getString("nome"));
                        dados.add(operador);
                    }

                    // Log.d("json","bonum");

                    return s;

                }catch(Exception e){
                    // Log.d("json","stercore");
                    e.printStackTrace();
                    return null;
                }

            }
        }
        GetJSON gj = new GetJSON();
        gj.execute(url);

    }

    public ArrayList getDados(){
        try {
            getJSON(url);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return dados;
    }
}
