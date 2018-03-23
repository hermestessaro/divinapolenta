package br.com.httpfluidobjects.appdivinapolenta;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class IniciaServicoActivity extends AppCompatActivity {

    String entrada;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    ArrayList<operador> dadosOperadores;
    MasterTest master;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicia_servico);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.action_bar);

        Log.d("ADM", "on create");
        entrada="";
        GetOperadores operadores = new GetOperadores();
        dadosOperadores = operadores.getDados();


        master = new MasterTest("192.168.1.15", 502);

        findBT();
        try {
            openBT();
        } catch (IOException e) {
            e.printStackTrace();
        }

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








    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            //myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().contains("CHOPEIRA-01"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

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
                                            Log.d("primeiratela", data);
                                            //StringBuffer sb = new StringBuffer(data);
                                            //sb.reverse();
                                            int i = 0;
                                            for(i=0; i<dadosOperadores.size(); i++) {
                                                if (data.contains(dadosOperadores.get(i).getCartao())) {//se cartao de operador dispara a ativity operador
                                                    Log.d("TAD3", data.toString());
                                                    showButtons();
                                                    break;
                                                }
                                            }
                                            /*if(data.equals("A04DFC87")) {
                                                showButtons();
                                            }*/
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


    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

 /*   @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { //ao passar o cartão lê cada caractere como uma tecla, chamando a função 8 vezes

        char pressedKey = (char) event.getUnicodeChar();
        entrada += Character.toString(pressedKey);//armazena cada caractere na variável entrada
        Log.d("TAD", entrada);

        //gambiarra pra pular leitura de cartao
        showButtons();

        if(entrada.length() == 8){
            //Log.d("TAD2", entrada);//quando ler os 8 caracteres começa a monitorar a batelada
            int i = 0;
            for(i = 0; i<dadosOperadores.size(); i++){
                if(entrada.equals(dadosOperadores.get(i))) {//se cartao de operador dispara a ativity operador
                    // Log.d("TAD3", entrada);
                    showButtons();
                    break;
                }

            }
            entrada="";
        }
        return super.onKeyDown(keyCode, event);
    }*/

    public void btnEntraOp(View view) {
        Intent intent = new Intent(IniciaServicoActivity.this, OperadorActivity.class);
        startActivity(intent);
    }

    public void btnEntraMonitora(View view) {
        Intent intent = new Intent(IniciaServicoActivity.this, MonitoraActivity.class);
        startActivity(intent);
    }
    public void btnEntra(View view) throws IOException {
        closeBT();
        Intent intent = new Intent(IniciaServicoActivity.this, CervejaActivity.class);
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
