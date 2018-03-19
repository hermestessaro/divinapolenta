package br.com.httpfluidobjects.appdivinapolenta;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.UUID;

import android.os.Handler;

import static android.os.SystemClock.sleep;

public class CervejaActivity extends AppCompatActivity {

    public static final String EXTRA_CEVAID = "cevaId";
    public static final String EXTRA_CHOPEIRAID = "chopeiraId";
    public int cevaId;
    public int chopeiraId;
    public int chopeiraNid;
    cliente cliente;
    cerveja ceva;
    int cont;
    CLPManager clpManager;
    int volume;
    double valor;
    TextView txtVolume;
    TextView txtValor;
    TextView linha1;
    TextView linha2;
    TextView txtSeBeber;
    double valorCeva;
    boolean finaliza;
    int idPedido;
    String entrada;
    String idCartao;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    int i =0;


    boolean leuCartao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerveja);

        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.action_bar);
        retriveSavedImage();
        cevaId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("ID_CERVEJA", "0"));
        //cevaId = 1;
        chopeiraId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("ID_CHOPEIRA", "0"));
        chopeiraNid = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("NID_CHOPEIRA", "0"));
        Log.d("ADM", String.valueOf(chopeiraId));

        if(cevaId != 0 && chopeiraId != 0)
            getJSONCervejas("http://divinapolenta.cloud.fluidobjects.com/get_cervejas");
        else{
            new AlertDialog.Builder(this)
                    .setTitle("Nenhuma chopeira selecionada!")
                    .setMessage("Por favor, peça ao operador que selecione uma chopeira.")
                    .setNeutralButton("ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(CervejaActivity.this, OperadorActivity.class);
                                    startActivity(intent);
                                }
                            }).show();

        }
        findBT();
        try {
            openBT();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //Conecta com a url e busca as cervejas
    private void getJSONCervejas(String url) {
        class GetJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(CervejaActivity.this, "Por favor aguarde...", null, true, false);
            }

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

                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    sb.append("");
                    String s = sb.toString().trim();

                    JSONObject jsonObj = new JSONObject(s);
                    JSONArray jsonArray = jsonObj.getJSONArray("cervejas");

                    int y = jsonArray.length();
                    String urlImagem = "http://divinapolenta.cloud.fluidobjects.com/sites/divinapolenta.cloud.fluidobjects.com/files/";

                    for (int i = 0; i < y; i++) {
                        JSONObject jsonCervejaObject = new JSONObject(jsonArray.getString(i)); //pega o primeiro elemento desse Array, transforma em string e cria um novo objeto

                        if (Integer.parseInt(jsonCervejaObject.getString("vid")) == cevaId) { //Encontra a cervaja de id = cevaId
                            urlImagem = urlImagem + jsonCervejaObject.getString("logo_name");
                            Bitmap logo;
                            ceva = new cerveja();
                            ceva.setNid(jsonCervejaObject.getString("nid"));
                            ceva.setId(jsonCervejaObject.getString("vid"));
                            ceva.setTitle(jsonCervejaObject.getString("title"));
                            ceva.setType(jsonCervejaObject.getString("type"));
                            ceva.setDescricao(jsonCervejaObject.getString("descricao"));
                            ceva.setFabricante(jsonCervejaObject.getString("fabricante"));
                            ceva.setEbc(jsonCervejaObject.getString("ebc"));
                            ceva.setConsumo(jsonCervejaObject.getString("consumo"));
                            ceva.setIbu(jsonCervejaObject.getString("ibu"));
                            ceva.setValor(jsonCervejaObject.getString("valor"));
                            ceva.setLogo_name(jsonCervejaObject.getString("logo_name"));
                            ceva.setLogo_uri(jsonCervejaObject.getString("logo_uri"));
                            ceva.setAb(jsonCervejaObject.getString("ab"));
                            logo = getBitmapFromURL(urlImagem);
                            ceva.setImageBM(logo);
                            saveLogo(logo);
                            break;
                        }
                    }

                    return s;

                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                showBeer();
                // monitoraCartao2();
                loading.dismiss();
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute(url);
    }

    //Conecta com a url e busca as cervejas
    private void getJSONClientes(String url, final String cartao) {
        class GetJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(CervejaActivity.this, "Por favor aguarde...", null, true, false);
            }

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

                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    sb.append("");
                    String s = sb.toString().trim();

                    JSONObject jsonObj = new JSONObject(s);
                    JSONArray jsonArray = jsonObj.getJSONArray("clientes");

                    int y = jsonArray.length();

                    for (int i = 0; i < y; i++) {
                        JSONObject jsonClienteObject = new JSONObject(jsonArray.getString(i)); //pega o primeiro elemento desse Array, transforma em string e cria um novo objeto

                        if(cartao.equals(jsonClienteObject.getString("cartao"))){
                            cliente = new cliente();
                            cliente.setCartao(cartao);
                            cliente.setCpf(jsonClienteObject.getString("cpf"));
                            cliente.setId(Integer.parseInt(jsonClienteObject.getString("id")));
                            cliente.setNome(jsonClienteObject.getString("nome"));
                            cliente.setValid(true);
                        }


                        break;
                    }

                    return s;

                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                showBeer();
                // monitoraCartao2();
                loading.dismiss();
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute(url);
    }

    //Conecta numa url e baixa a logo da cerveja
    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Bota a logo da cerveja na image view
    public void showBeer() {
        setContentView(R.layout.activity_cerveja);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(String.valueOf(ceva.getTitle()));
        TextView desc = (TextView) findViewById(R.id.descricao);
        desc.setText(String.valueOf(ceva.getDescricao()));
        TextView ab = (TextView) findViewById(R.id.teor);
        ab.setText("Teor Alcoólico: " + String.valueOf(ceva.getAb()));
        TextView valor = (TextView) findViewById(R.id.valor);
        valor.setText("R$ " + String.valueOf(ceva.getValor()) + " / 100ml");
        TextView ebc = (TextView) findViewById(R.id.ebc);
        ebc.setText("IBU: " + String.valueOf(ceva.getIbu()) + "| EBC: " + String.valueOf(ceva.getEbc()));
        ImageView imagem = (ImageView) findViewById(R.id.imageView3);
        imagem.setImageBitmap(ceva.getImageBM());
        this.valorCeva = ceva.getValor();
        linha1 = (TextView) findViewById(R.id.linha1);
        linha2 = (TextView) findViewById(R.id.linha2);
        entrada = "";
    }

    //Transforma a imagem em string e guarda em preferencias
    private void saveLogo(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("FOTO_CHOPP", imageEncoded).apply();
    }

    //Pega imagem já salva em preferencias e bota na image view
    private void retriveSavedImage() {
        String photo = PreferenceManager.getDefaultSharedPreferences(this).getString("FOTO_CHOPP", "");

        if (!photo.equals("")) {
            byte[] b = Base64.decode(photo, Base64.DEFAULT);
            InputStream is = new ByteArrayInputStream(b);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            setContentView(R.layout.activity_cerveja);
            ImageView imagem = (ImageView) findViewById(R.id.imageView3);
            imagem.setImageBitmap(bitmap);
        }
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
                                    entrada = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Log.d("data", entrada);
                                            StringBuffer sb = new StringBuffer(entrada);
                                            sb.reverse();
                                            if(sb.length() == 8){
                                                try {
                                                    preparesCLP(sb.toString());
                                                    entrada = "";
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
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


    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

    //------------- CLP -----------//

    public void preparesCLP(String idCartao) throws JSONException { //executa ao clicar no botão (passar cartão)


        Log.d("preparesCLP", "recebeu id do cartao" + idCartao);

        //GetIdPedidoAutomatize requisitor = new GetIdPedidoAutomatize();

        /*Log.d("teste", "irá buscar informações do pedido na automatize");
        String[] infoPedido = requisitor.runRequisition(400); //a partir do codigo do cartao pega o id do pedido(infoPedido[0]) e o nome do br.com.httpfluidobjects.appdivinapolenta.cliente(infoPedido[1])
        Log.d("asd", infoPedido[0]);
        Log.d("fcfsd", infoPedido[1]);
        Log.d("teste", "terminou busca");*/

        //if(i != 0){
//        name.setText("Cliente: Gabriel");}
//        else {name.setText("Cliente: Carla");}
        /*linha1.setText("Olá!, sirva-se à vontade");
        linha2.setText("Valor: R$ 0,00");
        finaliza = false;*/


        //monitoraBatelada();


        /*name.setText("Cliente: " + infoPedido[1]);
        this.idPedido = Integer.parseInt(infoPedido[0]); //converte o id do pedido para int*/

        getJSONClientes("http://divinapolenta.cloud.fluidobjects.com/get_clientes", idCartao);

        if(cliente.isValid() != true){
            new AlertDialog.Builder(this)
                    .setTitle("você não está cadastrado!")
                    .setMessage("Por favor, peça ao operador que o cadastre.")
                    .setNeutralButton("ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(CervejaActivity.this, OperadorActivity.class);
                                    startActivity(intent);
                                }
                            }).show();
        }
        else {
            clpManager = new CLPManager();
            // Log.d("ADM23", String.valueOf(chopeiraId));
            if (clpManager.open(chopeiraId)) { //inicializa os registradores necessários
                monitoraBatelada(); //monitora as informações da batelada
                atualizaInfoPedido(0); //atualiza as informações na tela do aplicativo
            } else {
                linha1.setText("Olá " + cliente.getNome() + ", sirva-se à vontade!");
                linha2.setText("O saldo de seu cartão agora é de: R$ " + cliente.getSaldo());
                entrada = "";
            }
        }

    }


    public void atualizaInfoPedido(Integer vol) { //pega informações registradas no clp manager e atualiza as a tela
        final Handler mHandler = new Handler();

       // new Thread(new Runnable() {
         //   @Override
          //  public void run() {

               // while (!finaliza) {
                   // sleep(70);
                   // mHandler.post(new Runnable() {
                     //   @Override
                      //  public void run() {
                         //   volume = clpManager.getVolume(); //pega o volume registrado em tempo real
                            finaliza = clpManager.finalizou(); //verifica o status da batelada - Se 4(finalizado) -> termina o while
                            if (!String.valueOf(vol).equals(txtVolume.getText())) { //Verifica se houve alteração no volume
                                float saldo_aux = cliente.getSaldo();
                                float custo = (ceva.getValor()/100)*vol;
                                saldo_aux = saldo_aux - custo;

                                linha1.setText(cliente.getNome()+", você serviu "+ vol +"ml, valor R$"+ custo);
                                linha2.setText("O saldo do seu cartão agora é de R$"+ custo); //atualiza a interface
                                //txtValor.setText("Valor: R$ " + getValorStr());
                            }

                       // }
                   // });
               // }
                entrada = "";}
               // mHandler.post(new Runnable() {
               //     @Override
                  //  public void run() {
               /*         try {
                            IntegracaoAutomatize.geraItemPedido(ceva, volume, idPedido);
                           clpManager.atualizaInfoDrupal(chopeiraNid, chopeiraId, ceva.getNid(), volume); //nid_chopeira, id_chopeira, nid_cerveja, volume_consumido
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sleep(2000);
                        name.setText("Passe o cartão");
                        txtVolume.setText("");
                        txtValor.setText("Sirva-se à vontade");
                        txtSeBeber.setText("Se beber, não dirija!");
                    }*/
              //  });

           // }
    //    }).start();

   // }

    public String getValorStr(int vol) { //calcula o valor atual de acordo com o volume
        double valorDbl = (this.valorCeva / 100) * vol;
        String resultado = String.format("%.2f", valorDbl);
        return resultado;
    }


    public void monitoraBateladaDemo() { //abre uma nova thread para escutar os registradores do clp
        final Handler mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!finaliza) {
                int x;
                        if (i!=0){
                            x =150;
                        }else x=300;
                        sleep(1000);
                        for (i = 0; i < x; i = i + 5) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //finaliza = clpManager.finalizou(); //verifica o status da batelada - Se 4(finalizado) -> termina o while
                                    //int vol = clpManager.monitorsCLP(this);
                            volume = i;
                            linha1.setText(String.valueOf("Serviu: " + String.valueOf(i) + "ml")); //atualiza a interface
                            linha2.setText("Valor: R$ " + getValorStr(i));
                                }
                            });
                            sleep(150);
                        }
                        sleep(3000);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //name.setText("Passe o cartão");
                        linha1.setText("Sirva-se à vontade");
                         linha2.setText("Se beber, não dirija!");
                    }
                });
                }
            }
        }).start();
    }



    public void monitoraBatelada() { //abre uma nova thread para escutar os registradores do clp
        final Handler mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!finaliza) {
                    finaliza = clpManager.finalizou(); //verifica o status da batelada - Se 4(finalizado) -> termina o while
                    final int vol = clpManager.monitorsCLP();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            linha1.setText(String.valueOf("Serviu: " + String.valueOf(vol) + "ml")); //atualiza a interface
                            linha2.setText("Valor: R$ " + getValorStr(vol));
                        }
                    });
                    sleep(3000);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //name.setText("Passe o cartão");
                            linha1.setText("Sirva-se à vontade");
                            linha2.setText("Se beber, não dirija!");
                        }
                    });
                }
            }
        }).start();
    }
}
