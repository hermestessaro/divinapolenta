package br.com.httpfluidobjects.appdivinapolenta;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class IniciaServicoActivity extends AppCompatActivity {



    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    String entrada;
    ArrayList<operador> dadosOperadores;
    String readerBT;
    BluetoothManager bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicia_servico);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }



        Log.d("ADM", "on create");
        entrada="";
        GetOperadores operadores = new GetOperadores();
        dadosOperadores = operadores.getDados();
        bm = (BluetoothManager)this.getApplication();

        //master = new MasterTest("192.168.1.15", 502);

       /*findBT();
        try {
            openBT();
            beginListenForData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException y) {
            Toast.makeText(this, "Erro bluetooth; o leitor correto esta pareado?", Toast.LENGTH_LONG).show();
            try {
                workerThread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }*/




    }

    @Override
    public void onResume() {
        super.onResume();
        Button telaPrincipal = (Button) findViewById(R.id.btnEntrar);
        Button telaOp = (Button) findViewById(R.id.btnEntrarOp);
        Button telaMonitora = (Button) findViewById(R.id.btnEntrarMonitora);


        Intent intent = getIntent();
        int operou = intent.getIntExtra("operou", 0);
        Log.d("operou", String.valueOf(operou));
        if (operou == 0) {
            //telaPrincipal.setVisibility(View.INVISIBLE);
            //telaOp.setVisibility(View.INVISIBLE);
            //telaMonitora.setVisibility(View.INVISIBLE);
            boolean first_time = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_time", true);
            int fator = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString("FATOR", "0"));
            if (!first_time) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_time", false);
                if (fator == 0) {
                    new AlertDialog.Builder(this)
                            .setTitle("Fator de pulso não configurado")
                            .setMessage("Por favor, peça ao operador que configure a chopeira.")
                            .setNeutralButton("ok",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(IniciaServicoActivity.this, OperadorActivity.class);
                                            startActivity(intent);
                                        }
                                    }).show();
                }

            }
        }
        else{
            //beginListenForData();
        }
    }


    void findBT()
    {
        bm.setmBluetoothAdapter(BluetoothAdapter.getDefaultAdapter());

        if(bm.getmBluetoothAdapter() == null)
        {
            //myLabel.setText("No bluetooth adapter available");
        }

        if(!bm.getmBluetoothAdapter().isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = bm.getmBluetoothAdapter().getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().contains("CHOPEIRA-01")){
                    readerBT = "CHOPEIRA-01";
                    ((BluetoothManager) this.getApplication()).setMmDevice(device);
                    break;
                }
                if(device.getName().contains("CHOPEIRA-02")){
                    readerBT = "CHOPEIRA-02";
                    ((BluetoothManager) this.getApplication()).setMmDevice(device);
                    break;
                }
                if(device.getName().contains("CHOPEIRA-03")){
                    readerBT = "CHOPEIRA-03";
                    ((BluetoothManager) this.getApplication()).setMmDevice(device);
                    break;
                }
                if(device.getName().contains("Dragon Fluid BT")){
                    readerBT = "Dragon Fluid BT";
                    ((BluetoothManager) this.getApplication()).setMmDevice(device);
                    break;
                }

            }
        }
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        bm.setMmSocket(bm.getMmDevice().createRfcommSocketToServiceRecord(uuid));
        bm.getMmSocket().connect();
        bm.setMmOutputStream(bm.getMmSocket().getOutputStream());
        bm.setMmInputStream(bm.getMmSocket().getInputStream());

    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character
        final InputStream mmInputStream = bm.getMmInputStream();
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            int i = 0;
                                            for(i=0; i<dadosOperadores.size(); i++) {
                                                if (data.contains(dadosOperadores.get(i).getCartao())) {//se cartao de operador dispara a ativity operador
                                                    Log.d("TAD3", data.toString());
                                                    showButtons();
                                                    break;
                                                }
                                            }

                                            //isso é pra debug
                                            if(!data.isEmpty()){
                                                showButtons();
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    void closeBT() throws IOException {
        stopWorker = true;
        ((BluetoothManager) this.getApplication()).getMmOutputStream().close();
        ((BluetoothManager) this.getApplication()).getMmInputStream().close();
        ((BluetoothManager) this.getApplication()).getMmSocket().close();

    }


    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { //ao passar o cartão lê cada caractere como uma tecla, chamando a função 8 vezes

        char pressedKey = (char) event.getUnicodeChar();
        entrada += Character.toString(pressedKey);//armazena cada caractere na variável entrada
        Log.d("TAD", entrada);
        showButtons();

        if(entrada.length() == 8){
            //Log.d("TAD2", entrada);//quando ler os 8 caracteres começa a monitorar a batelada
            int i = 0;
            for(i = 0; i<dadosOperadores.size(); i++){
                if(entrada.equals(dadosOperadores.get(i))) {//se cartao de operador dispara a ativity operador
                     Log.d("TAD3", entrada);
                    showButtons();
                    break;
                }

            }
            entrada="";
        }
        showButtons();
        return super.onKeyDown(keyCode, event);
    }*/

    public void btnEntraOp(View view) {
        Intent intent = new Intent(IniciaServicoActivity.this, OperadorActivity.class);
        startActivity(intent);
    }

    public void btnEntraMonitora(View view) {
        showButtons();
        //Intent intent = new Intent(IniciaServicoActivity.this, MonitoraActivity.class);
        //startActivity(intent);
    }
    public void btnEntra(View view) throws IOException {
        //closeBT();
        Intent intent = new Intent(IniciaServicoActivity.this, CervejaActivity.class);
        startActivity(intent);
    }

    public void showButtons(){
        Button telaPrincipal = (Button) findViewById(R.id.btnEntrar);
        Button telaOp = (Button) findViewById(R.id.btnEntrarOp);
        Button telaMonitora = (Button) findViewById(R.id.btnEntrarMonitora);

        telaPrincipal.setVisibility(View.VISIBLE);
        telaOp.setVisibility(View.VISIBLE);
        //telaMonitora.setVisibility(View.VISIBLE);
    }



}
