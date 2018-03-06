package br.com.httpfluidobjects.appdivinapolenta;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gabrielabreu on 09/10/17.
 */

public class GetIdPedidoAutomatize {
    String url;
    String idPedido;
    String nomeCliente;
    String[] infoPedido;

    public GetIdPedidoAutomatize() {
        url = null;
        idPedido = "-1";
        nomeCliente = "Não Identificado";
        infoPedido = new String[2];
    }

    public String[] runRequisition(int codigo_cartao) {
        url = "http://automatize.ddns.net:5000/pedidos/codigo/" + codigo_cartao;
        getJSON();
        return infoPedido;
    }

    private void getJSON() {
        Log.d("teste", "entrou no get info pedido");
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
            idPedido = jsonObj.getString("id");
            if (!jsonObj.getString("cliente").equals("null")) {
                nomeCliente = jsonObj.getString("cliente");
            }
            infoPedido[0] = idPedido;
            infoPedido[1] = nomeCliente;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
