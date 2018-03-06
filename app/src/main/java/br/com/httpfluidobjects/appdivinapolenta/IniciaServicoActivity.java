package br.com.httpfluidobjects.appdivinapolenta;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class IniciaServicoActivity extends AppCompatActivity {

    String entrada;

    ArrayList<String[]> dadosOperadores;
    MasterTest master;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicia_servico);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        Log.d("ADM", "on create");
        entrada="";
        GetOperadores operadores = new GetOperadores();
        dadosOperadores = operadores.getDados();


        master = new MasterTest("192.168.15.7", 502);

    }

    @Override
    public void onResume() {
        Button telaPrincipal = (Button) findViewById(R.id.btnEntrar);
        Button telaOp = (Button) findViewById(R.id.btnEntrarOp);
        Button telaMonitora = (Button) findViewById(R.id.btnEntrarMonitora);

        telaPrincipal.setVisibility(View.GONE);
        telaOp.setVisibility(View.GONE);
        telaMonitora.setVisibility(View.GONE);
        super.onResume();
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { //ao passar o cartão lê cada caractere como uma tecla, chamando a função 8 vezes

        char pressedKey = (char) event.getUnicodeChar();
        entrada += Character.toString(pressedKey);//armazena cada caractere na variável entrada
        //Log.d("TAD", entrada);
        if(entrada.length() == 8){
            //Log.d("TAD2", entrada);//quando ler os 8 caracteres começa a monitorar a batelada
            for(String[] item: dadosOperadores){
                if(entrada.equals(item[0])) {//se cartao de operador dispara a ativity operador
                   // Log.d("TAD3", entrada);
                    showButtons();
                    break;
                }

            }
            entrada="";
        }
        return super.onKeyDown(keyCode, event);
    }

    public void btnEntra(View view) {
        Intent intent = new Intent(IniciaServicoActivity.this, CervejaActivity.class);
        startActivity(intent);
    }

    public void btnEntraOp(View view) {
        Intent intent = new Intent(IniciaServicoActivity.this, OperadorActivity.class);
        startActivity(intent);
    }

    public void btnEntraMonitora(View view) {
        Intent intent = new Intent(IniciaServicoActivity.this, MonitoraActivity.class);
        startActivity(intent);
    }

    public void showButtons(){
        Button telaPrincipal = (Button) findViewById(R.id.btnEntrar);
        Button telaOp = (Button) findViewById(R.id.btnEntrarOp);
        Button telaMonitora = (Button) findViewById(R.id.btnEntrarMonitora);

        telaPrincipal.setVisibility(View.VISIBLE);
        telaOp.setVisibility(View.VISIBLE);
        telaMonitora.setVisibility(View.VISIBLE);
    }



}
