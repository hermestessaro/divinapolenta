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
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import android.os.Handler;
import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;

import static android.os.SystemClock.sleep;

public class CervejaActivity extends AppCompatActivity {

    public int cevaId;
    public int chopeiraId;
    public int chopeiraNid;
    cliente cliente = null;
    cerveja ceva;
    int volume;
    int volume_aux;
    public TextView  linha1;
    public TextView linha2;
    double valorCeva;
    boolean finaliza;
    String entrada;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    int fator;

    float saldo_aux;
    float custo;
    CLPManager clpManager;
    ArrayList<cliente> clientes = new ArrayList<cliente>();
    BluetoothManager bm;


    //EditText cartao;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerveja);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        retriveSavedImage();


        //cartao = (EditText) findViewById(R.id.testecartao);



        //MUDAR O VALOR DEFAULT
        cevaId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("ID_CERVEJA", "0"));
        //cevaId = 1;
        chopeiraId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("ID_CHOPEIRA", "0"));
        chopeiraNid = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("NID_CHOPEIRA", "0"));
        fator = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString("FATOR", "0"));
        Log.d("ADM", String.valueOf(chopeiraId));
        bm = (BluetoothManager) this.getApplication();
        //TODO: verificar se ha conexao com a internet

        if(cevaId != 0 && chopeiraId != 0) {

            getJSONCervejasSincrono("http://divinapolenta.cloud.fluidobjects.com/get_cervejas");
            showBeer();
            getJSONTodosClientesAssincrono("http://divinapolenta.cloud.fluidobjects.com/get_clientes");

            findBT();

            try {
                openBT();
            } catch (IOException e) {
                //Toast.makeText(this, "Erro bluetooth; o leitor correto esta pareado?", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (RuntimeException y){
                Toast.makeText(this, "Erro bluetooth; o leitor correto esta pareado?", Toast.LENGTH_LONG).show();
                y.printStackTrace();
                Intent intent = new Intent(CervejaActivity.this, OperadorActivity.class);
                startActivity(intent);
            }


            beginListenForData();

        }
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
        //setContentView(R.layout.activity_cerveja);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(String.valueOf(ceva.getTitle()));
        TextView desc = (TextView) findViewById(R.id.descricao);
        desc.setText(String.valueOf(ceva.getDescricao()));
        TextView ab = (TextView) findViewById(R.id.teor);
        float teor = ceva.getAb();
        ab.setText("Teor Alcoólico: " + Float.toString(teor)+" %");
        TextView valor = (TextView) findViewById(R.id.valor);
        valor.setText("R$ " + String.valueOf(ceva.getValor()) + " / 100ml");
        TextView ebc = (TextView) findViewById(R.id.ebc);

        String textEbc="";
        if(ceva.getIbu()>0){
            textEbc+="IBU: " + String.valueOf(ceva.getIbu());
            if(ceva.getEbc()>0) {
                textEbc += " | EBC: " + Float.toString(ceva.getEbc());
            }
        }
        else
        {
            if(ceva.getEbc()>0) {
                textEbc += "EBC: " + Float.toString(ceva.getEbc());
            }
        }

        ebc.setText(textEbc);
        ImageView imagem = (ImageView) findViewById(R.id.imageView3);
        imagem.setImageBitmap(ceva.getImageBM());
        this.valorCeva = ceva.getValor();

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
        if (mBluetoothAdapter == null) {
            //myLabel.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().contains("CHOPEIRA-01")){
                    mmDevice = device;
                    break;
                }
                if(device.getName().contains("CHOPEIRA-02")){
                    mmDevice = device;
                    break;
                }
                if(device.getName().contains("CHOPEIRA-03")){
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


    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte new_line = 10; //This is the ASCII code for a newline character
        final byte carriage_return = 13;
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
                                if(b == new_line)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    entrada = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Log.d("leituracartao", entrada);
                                            //StringBuffer sb = new StringBuffer(entrada);
                                            //sb.reverse();
                                            if(entrada.length() >= 8){
                                                try {
                                                    preparesCLP(entrada);
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

    //------------- CLP -----------//


    public void preparesCLP(String idCartao) throws JSONException { //executa ao clicar no botão (passar cartão)

        clpManager = new CLPManager();
        finaliza = false;
        Log.d("preparesCLP", "recebeu id do cartao" + idCartao);
        //getJSONClientesSincrono("http://divinapolenta.cloud.fluidobjects.com/get_clientes", idCartao);

        for(int i = 0; i<clientes.size();i++) {
            if (clientes.get(i).isValid() && idCartao.contains(clientes.get(i).getCartao())) {
                cliente = clientes.get(i);
                break;

            }
            cliente = null;
        }
        if(cliente == null) {
            linha2 = (TextView) findViewById(R.id.linha2);
            linha2.setText("Aguarde enquanto seu cadastro é verificado");
            getJSONClientesSincrono("http://divinapolenta.cloud.fluidobjects.com/get_clientes", idCartao);
            if(!cliente.isValid){
                linha1 = (TextView) findViewById(R.id.linha1);
                linha1.setText("Cliente não cadastrado");
                linha2.setText("");
                sleep(3000);
            }
            else
            {
                linha1 = (TextView) findViewById(R.id.linha1);
                linha1.setText("Olá " + cliente.getNome() + ". Seu saldo é de R$" + cliente.getSaldo());
                linha2 = (TextView) findViewById(R.id.linha2);
                linha2.setText("Pode se servir");
                sleep(2000);
                if (clpManager.open(chopeiraId, fator)) { //inicializa os registradores necessários
                    Log.d("clp", "abriu");
                    setMaxVolume();
                    Log.d("volumemax", "setou");
                    monitoraBatelada(); //monitora as informações da batelada

                } else {
                    Log.d("2", "segunda mensagem");
                    linha1 = (TextView) findViewById(R.id.linha1);
                    linha1.setText("Aproxime o cartão");
                    linha2 = (TextView) findViewById(R.id.linha2);
                    linha2.setText("se beber não dirija");
                    entrada = "";
                }

                linha1.setText("Aproxime o cartão");
                linha2.setText("Aguarde aparecer seu nome e saldo para se servir");
            }

        }
        else {

            Log.d("1", "primeira mensagem");
            linha1 = (TextView) findViewById(R.id.linha1);
            linha1.setText("Olá " + cliente.getNome() + ". Seu saldo é de R$" + cliente.getSaldo());
            linha2 = (TextView) findViewById(R.id.linha2);
            linha2.setText("Pode se servir");
            sleep(2000);
            if (clpManager.open(chopeiraId, fator)) { //inicializa os registradores necessários
                Log.d("clp", "abriu");
                setMaxVolume();
                Log.d("volumemax", "setou");
                monitoraBatelada(); //monitora as informações da batelada

            } else {
                Log.d("2", "segunda mensagem");
                linha1 = (TextView) findViewById(R.id.linha1);
                linha1.setText("Aproxime o cartão");
                linha2 = (TextView) findViewById(R.id.linha2);
                linha2.setText("se beber não dirija");
                entrada = "";
            }

//            linha1.setText("Aproxime o cartão");
//            linha2.setText("Aguarde aparecer seu nome e saldo para se servir");
        }



        //int fator = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("FATOR", "0"));


    }


    public void atualizaInfoPedido() { //pega informações registradas no clp manager e atualiza as a tela
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (!finaliza) {
                    sleep(70);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            volume_aux = volume;
                            volume = clpManager.getVolume(); //pega o volume registrado em tempo real
                            finaliza = clpManager.finalizou(); //verifica o status da batelada - Se 4(finalizado) -> termina o while

                            if (volume != volume_aux) { //Verifica se houve alteração no volume
                                linha2 =(TextView) findViewById(R.id.linha2);
                                linha2.setText("Serviu "+volume+"ml, valor R$ "+ getValorStr());
                                Log.d("3", "terceira mensagem");

                            }

                        }
                    });
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        saldo_aux = cliente.getSaldo();
                        custo = (ceva.getValor()/100)*volume;
                        //double roundOff = Math.round(custo * 100.0) / 100.0;
                        saldo_aux = (saldo_aux - custo);

                        linha1 =(TextView) findViewById(R.id.linha1);
                        linha1.setText(cliente.getNome()+", você serviu "+ volume +"ml, valor R$"+ custo);
                        linha2 =(TextView) findViewById(R.id.linha2);
                        linha2.setText("O saldo do seu cartão agora é de R$"+ saldo_aux); //atualiza a interface
                        cliente.setSaldo(saldo_aux);
                        for(int i = 0;i<clientes.size();i++){
                            if(clientes.get(i).getCpf().equals(cliente.getCpf())){
                                clientes.get(i).setSaldo(saldo_aux);
                                break;
                            }
                        }
                        Log.d("5", "quinta mensagem");
                        Log.d("valores", String.valueOf(custo) +","+ String.valueOf(saldo_aux)+","+ String.valueOf(volume));

                    }
                });
                sleep(5000);
                entrada = "";
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(saldo_aux > 1) {
                                atualizaClienteDrupal(); //nid_chopeira, id_chopeira, nid_cerveja, volume_consumido
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        clpManager.closeCon();
                        linha1.setText("Aproxime o cartão");
                        linha2.setText("Espere aparecer o seu nome e saldo para servir");
                        Log.d("4", "quarta mensagem");

                    }
                });

            }
        }).start();

    }

    public String getValorStr() { //calcula o valor atual de acordo com o volume
        double valorDbl = (this.valorCeva / 100) * volume;
        String resultado = String.format("%.2f", valorDbl);
        return resultado;
    }


    public void monitoraBatelada() { //abre uma nova thread para escutar os registradores do clp
        Thread t = new Thread(new Runnable() {
            public void run() {
                clpManager.monitorsCLP();
            }
        });
        t.start();
        atualizaInfoPedido(); //atualiza as informações na tela do aplicativo

    }



    private void setMaxVolume(){
        float max = (cliente.getSaldo()/(ceva.getValor())*100);
        if(max > 500){
            max = 500;
        }
        clpManager.setMaxVol((int) max);
        Log.d("max vol", String.valueOf(max));
    }



    public void atualizaClienteDrupal() throws JSONException { //pega informações registradas no clp manager e atualiza as a tela


        JSONObject jobj;
        jobj = new JSONObject();

        jobj.put("id_cliente", cliente.getId()); //id_item_automatize

        jobj.put("novo_saldo", saldo_aux);
        String data = jobj.toString();

        String url = "http://divinapolenta.cloud.fluidobjects.com/atualiza_saldo";
        ExportJSON.sendJSON(url, data);

    }





    //----------------JSON----------------//
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


    private void getJSONCervejasSincrono(String uri){



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

        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    int achou = 0;

                    for (int i = 0; i < y; i++) {
                        JSONObject jsonClienteObject = new JSONObject(jsonArray.getString(i)); //pega o primeiro elemento desse Array, transforma em string e cria um novo objeto

                        if(cartao.equals(jsonClienteObject.getString("cartao"))){
                            cliente = new cliente();
                            cliente.setCartao(cartao);
                            cliente.setCpf(jsonClienteObject.getString("cpf"));
                            cliente.setId(Integer.parseInt(jsonClienteObject.getString("id")));
                            cliente.setNome(jsonClienteObject.getString("nome"));
                            cliente.setSaldo(Float.parseFloat(jsonClienteObject.getString("saldo")));
                            cliente.setValid(true);
                            clientes.add(cliente);
                            achou = 1;
                        }


                        break;
                    }
                    if(achou == 0){
                        cliente = new cliente();
                        cliente.setValid(false);
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

    private void getJSONClientesSincrono(String uri, final String cartao){
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
            JSONArray jsonArray = jsonObj.getJSONArray("cliente");

            int y = jsonArray.length();
            int achou = 0;

            for (int i = 0; i < y; i++) {
                JSONObject jsonClienteObject = new JSONObject(jsonArray.getString(i)); //pega o primeiro elemento desse Array, transforma em string e cria um novo objeto

                if(cartao.contains(jsonClienteObject.getString("cartao"))){
                    cliente = new cliente();
                    cliente.setCartao(cartao);
                    cliente.setCpf(jsonClienteObject.getString("cpf"));
                    cliente.setId(Integer.parseInt(jsonClienteObject.getString("id")));
                    cliente.setNome(jsonClienteObject.getString("nome"));
                    cliente.setSaldo(Float.parseFloat(jsonClienteObject.getString("saldo")));
                    cliente.setValid(true);
                    achou = 1;
                    break;
                }
            }
            if(achou == 0){
                cliente = new cliente();
                cliente.setValid(false);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getJSONTodosClientesSincrono(String uri){
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
            JSONArray jsonArray = jsonObj.getJSONArray("cliente");

            int y = jsonArray.length();
            int achou = 0;

            for (int i = 0; i < y; i++) {
                JSONObject jsonClienteObject = new JSONObject(jsonArray.getString(i)); //pega o primeiro elemento desse Array, transforma em string e cria um novo objeto

                cliente = new cliente();
                cliente.setCartao(jsonClienteObject.getString("cartao"));
                cliente.setCpf(jsonClienteObject.getString("cpf"));
                cliente.setId(Integer.parseInt(jsonClienteObject.getString("id")));
                cliente.setNome(jsonClienteObject.getString("nome"));
                cliente.setSaldo(Float.parseFloat(jsonClienteObject.getString("saldo")));
                cliente.setValid(true);

                clientes.add(cliente);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getJSONTodosClientesAssincrono(String url) {
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
                    JSONArray jsonArray = jsonObj.getJSONArray("cliente");

                    int y = jsonArray.length();
                    int achou = 0;

                    for (int i = 0; i < y; i++) {
                        JSONObject jsonClienteObject = new JSONObject(jsonArray.getString(i)); //pega o primeiro elemento desse Array, transforma em string e cria um novo objeto


                        cliente = new cliente();
                        cliente.setCartao(jsonClienteObject.getString("cartao"));
                        cliente.setCpf(jsonClienteObject.getString("cpf"));
                        cliente.setId(Integer.parseInt(jsonClienteObject.getString("id")));
                        cliente.setNome(jsonClienteObject.getString("nome"));
                        cliente.setSaldo(Float.parseFloat(jsonClienteObject.getString("saldo")));
                        cliente.setValid(true);

                        clientes.add(cliente);
                    }


                    return s;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                // monitoraCartao2();
                loading.dismiss();
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute(url);
    }


    /*public void simulaCartao(View view){
        try {

            //String numero = cartao.getText().toString();
            preparesCLP("55A1AEC6");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/
}
