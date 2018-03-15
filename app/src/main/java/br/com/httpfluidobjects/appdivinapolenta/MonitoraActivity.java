package br.com.httpfluidobjects.appdivinapolenta;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static android.os.SystemClock.sleep;

public class MonitoraActivity extends AppCompatActivity {

    TextView txtNomeChopeira;
    TextView txtVolumeBarril;
    TextView txtStatusBatelada;
    TextView txtStatusChopeira;
    TextView txtVazao;
    TextView txtStatusVs;
    TextView txtMultFator;
    TextView txtVolumeConsumido;


    chopeira torneira;
    GetChopeiraById getChopeira;
    CLPManager clpManager;
    boolean finaliza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitora);

        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.action_bar);
        //torneira = new chopeira();
        //getChopeira = new GetChopeiraById();
    }

    /*@Override
    public void onResume() {
        super.onResume();
        //String id_chopeira = PreferenceManager.getDefaultSharedPreferences(this).getString("ID_CHOPEIRA", "1");
        //torneira = getChopeira.getDados(id_chopeira);

        //clpManager = new CLPManager();

        //clpManager.inicializaEndRegistradores(Integer.parseInt(id_chopeira));
       // clpManager.open(Integer.parseInt(id_chopeira));
       // this.monitoraCLP();

        txtNomeChopeira = (TextView) findViewById(R.id.txtNomeChopeira);
        txtVolumeBarril = (TextView) findViewById(R.id.txtVolumeBarril);
        txtStatusBatelada = (TextView) findViewById(R.id.txtStatusBatelada);
        txtStatusChopeira = (TextView) findViewById(R.id.txtStatusChopeira);
        txtVazao = (TextView) findViewById(R.id.txtVazao);
        txtStatusVs = (TextView) findViewById(R.id.txtStatusVs);
        txtMultFator = (TextView) findViewById(R.id.txtMultFator);
        txtVolumeConsumido = (TextView) findViewById(R.id.txtVolumeConsumido);



        txtVolumeBarril.setText("Volume no barril: " );//+ String.valueOf(torneira.getQuantidade())

        txtNomeChopeira.setText("Número da chopeira: " );// + id_chopeira


        //monitora as informações da batelada
        //this.atualizaInfo(); //atualiza as informações na tela do aplicativo
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        finaliza = true;
        clpManager.setFinalizaOp(true);
    }*/

    public void atualizaInfo() { //pega informações registradas no clp manager e atualiza a tela
        final Handler mHandler = new Handler();
        finaliza = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!finaliza) {
                    sleep(100);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                           // Log.d("TAG", "escutando");
                            txtStatusBatelada.setText(String.valueOf("Status da servida: " + clpManager.getBatelaStr()));
                            txtStatusChopeira.setText(String.valueOf("Status da chopeira: " + clpManager.getStatusStr()));
                            txtVazao.setText(String.valueOf("Vazão: " + clpManager.getVazaoStr() + "ml/s"));
                            //txtVolumeBarril.setText(String.valueOf("Volume no barril: " + clpManager.getVolumeBarrilStr()));
                          //  txtStatusVs.setText(String.valueOf("Válvula solenoide:  " + clpManager.getStatusValvulaStr()));
                          //  txtMultFator.setText(String.valueOf("Fator de multiplicacao: " + clpManager.getMultFatorStr()));
                          //  txtVolumeConsumido.setText(String.valueOf("Volume consumido ao simular servida: " + clpManager.getVolumeStr()));
                        }
                    });
                }
            }
        }).start();

    }

    public void simulaServida(View view){
        Thread t = new Thread(new Runnable() {
            public void run() {
                clpManager.simulaServida();
            }
        });
        t.start();

    }

    public void monitoraCLP() { //abre uma nova thread para escutar os registradores do clp
        Thread t = new Thread(new Runnable() {
            public void run() {
                clpManager.monitorsCLPOp();
            }
        });
        t.start();
    }
}
