package br.com.httpfluidobjects.appdivinapolenta;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gabrielabreu on 27/09/17.
 */

public class IntegracaoAutomatize {

    public static void export(ItemPedido pedido, int idPedido, int id_automatize) throws JSONException {
        JSONObject jobj;
        jobj = new JSONObject();
        //jobj.put("id", pedido.getId());
        //jobj.put("codigo", pedido.getCodigo());
        jobj.put("id_item", id_automatize); //id_item_automatize
        jobj.put("quantidade", 1); //sempre 1
        jobj.put("unitario", pedido.getTotal()); //valor total
        //jobj.put("total", pedido.getTotal());
        jobj.put("total_liquido", 0); //sempre 0
        //jobj.put("tipo", 0);
        jobj.put("status", 1); //sempre 1
        jobj.put("data", 0); //timestamp
        //jobj.put("observacao", "");
        jobj.put("desconto", 0);
        //jobj.put("descricao", "");


        String data = jobj.toString();
        //for tests:
        //sendJSON("http://kampeki.develop.fluidobjects.com/json/put", data);
        String url = "http://automatize.ddns.net:5000/pedidos/" + idPedido + "/item";
        ExportJSON.sendJSON(url,data);
    }

    static void geraItemPedido(cerveja ceva, int volume, int idPedido) throws JSONException { //colhe as informações para montar um pedido
        int id = 0;
        int codigo = 0;
        int id_item = ceva.getIdAutomatize();
        int quantidade = 1;
        double total = (1.0 * volume/100) * ceva.getValor();
        double unitario = ceva.getValor();

        ItemPedido novo_item = new ItemPedido(id, codigo, id_item, quantidade, total, unitario);

        //recolhe o id do pedido
        export(novo_item, idPedido, ceva.getIdAutomatize()); //manda o item e o id do pedido
    }
}
