package br.com.httpfluidobjects.appdivinapolenta;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OperadorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextView txtNomeChopeira;
    TextView txtVolumeBarril;
    TextView txtStatusBatelada;
    TextView txtStatusChopeira;
    TextView txtVazao;
    TextView txtStatusVs;
    TextView txtMultFator;
    TextView txtVolumeConsumido;

    TextView numChopeira;
    EditText edtVolumeAbastecido;


    String idNovaChopeira;
    chopeira torneira;
    GetChopeiraById getChopeira;
    CLPManager clpManager;
    boolean finaliza;



    int cont;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operador);
        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.action_bar);
        torneira = new chopeira();
        getChopeira = new GetChopeiraById();
    }

    @Override
    public void onResume() {
        super.onResume();
        selectChopeira();
        String id_chopeira = PreferenceManager.getDefaultSharedPreferences(this).getString("ID_CHOPEIRA", "0");

        idNovaChopeira = id_chopeira;

        txtNomeChopeira = (TextView) findViewById(R.id.txtNomeChopeira);
        txtVolumeBarril = (TextView) findViewById(R.id.txtVolumeBarril);

        edtVolumeAbastecido = (EditText) findViewById(R.id.edtVolumeAbastecido);

        torneira = getChopeira.getDados(idNovaChopeira);
        txtVolumeBarril.setText("Volume no barril: " + String.valueOf(torneira.getQuantidade()));


        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
        mySpinner.setSelection(getIndex(mySpinner, id_chopeira));
        txtNomeChopeira.setText("Número da chopeira: " + id_chopeira);
    }

    private int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void selectChopeira() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        List<String> chopeiraNames = new ArrayList<String>();
        for (int i = 1; i <= 10; i++) {
            chopeiraNames.add(String.valueOf(i));
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, chopeiraNames);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice); //simple_spinner_dropdown_item

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        return;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        idNovaChopeira = item;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void confirma(View view) {
        torneira = getChopeira.getDados(idNovaChopeira);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("ID_CERVEJA", String.valueOf(torneira.getIdCerveja())).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("NID_CHOPEIRA", String.valueOf(torneira.getNid())).apply();

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("ID_CHOPEIRA", idNovaChopeira).apply();
        //TODO ajeitar isso
        /*String volumeAbastecidoStr = edtVolumeAbastecido.getText().toString();
        if(!volumeAbastecidoStr.equals("")){
            Log.d("abastecer barril: ", volumeAbastecidoStr);
            try {
                abasteceBarril(volumeAbastecidoStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
        Intent intent = new Intent(OperadorActivity.this, IniciaServicoActivity.class);
        intent.putExtra("operou", 1);
        startActivity(intent);
    }

    private void abasteceBarril(String volumeAbastecidoStr) throws JSONException {
        int volumeAbastecido = Integer.parseInt(volumeAbastecidoStr)*1000;
        Date currentTime = Calendar.getInstance().getTime();
        JSONObject jobj;
        jobj = new JSONObject();

        jobj.put("nid_chopeira", torneira.getNid()); //id_item_automatize
        jobj.put("volume_barril", volumeAbastecido);
        jobj.put("data", currentTime);
        String data = jobj.toString();

        String url = "http://divinapolenta.cloud.fluidobjects.com/abastece_barril";
        ExportJSON.sendJSON(url,data);
    }

}
