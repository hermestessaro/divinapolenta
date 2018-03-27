package br.com.httpfluidobjects.appdivinapolenta;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import static android.os.SystemClock.sleep;

/**
 * Created by hermestessaro on 03/10/17.
 */


public class CLPManager {
    final MasterTest master;
    private int volumeProgramado = 40;
    private int statusBatelada;
    private int volume;

    // REGISTRADORES CLP
    private int BATELADA_REG = 0;
    private int STATUS_REG = 3;
    private int VOLUME_REG = 4;
    private int STATUS_VS_REG = 5;
    private int MAX_VOL_REG = 7;
    private int VAZAO_REG = 8;
    private int MULT_FACTOR_REG = 12;
    private int VOLUME_BARRIL_REG = 14;
    private int PROBLEMA_VS_REG = 15;

    private int batelada_valor;
    private int status_valor;
    private int volume_valor;
    private int status_vs_valor;
    private int max_vol_valor;
    private int vazao_valor;
    private int mult_factor_valor;
    private int volume_barril_valor;
    private int problema_vs_valor;

    private boolean finalizaOp;

    public CLPManager() {
        master = new MasterTest("192.168.15.12", 502);
        //master = new MasterTest("10.0.1.15", 502);
        finalizaOp = false;
    }

    public int getMaxVol(){
        return master.readRegister(MAX_VOL_REG);
    }


    //ajusta o endereco dos registradores de acordo com o numero da chopeira
    public void inicializaEndRegistradores(int chopeira) {
        //chopeira -= 1;
        Log.d("chopeira", String.valueOf(chopeira));
        chopeira = chopeira - 1;
        int regInicial = 3000 + (chopeira  * 30) - 1;
        //int regInicial = 1;

        BATELADA_REG = regInicial + 0; //2999
        STATUS_REG = regInicial + 3;   //3002
        VOLUME_REG = regInicial + 4;   //3003
        STATUS_VS_REG = regInicial + 5;
        MAX_VOL_REG = regInicial + 7;
        VAZAO_REG = regInicial + 8;
        MULT_FACTOR_REG = regInicial + 12;
        VOLUME_BARRIL_REG = regInicial + 14;
        PROBLEMA_VS_REG = regInicial + 15;

        //Log.d("status", String.valueOf(STATUS_REG));
    }


    // - - - - - - - - - -  CLIENTE - - - - - - - - - - - //

    //É chamado para abrir a batelada apos passar o cartão
    public boolean open(int numeroChopeira) {
        Log.d("t1", "vai tentar abrir a batelada");
        inicializaEndRegistradores(numeroChopeira);

        boolean aux = master.readRegister(STATUS_REG) == 10;
        Log.d("valor de aux", String.valueOf(aux));
        if (aux) {
            master.writeRegisters(MULT_FACTOR_REG, 390);
            //master.writeRegisters(MAX_VOL_REG, 500); //seta o volume maximo
            master.writeRegisters(STATUS_REG, 20); //seta status para programado
            master.writeRegisters(BATELADA_REG, 1); //abre a batelada
            statusBatelada = 1;
            Log.d("teste", "abriu batelada");
            volume = 0;
            return true;
        }
        return false;
    }




    public int monitorsCLP() {
        int volumeLido = master.readRegister(VOLUME_REG);
        if (volume < volumeLido) volume = volumeLido;

        this.statusBatelada = master.readRegister(BATELADA_REG);

        if (this.statusBatelada == 3) {
            Log.d("teste", "encerrou batelada");
            master.writeRegisters(BATELADA_REG, 4);
            this.statusBatelada = 4;
            return 1;
        }
        Log.d("teste", "listening");
        sleep(200);
        return monitorsCLP();
    }


    public int getVolume() {
        return volume;
    }

    public boolean finalizou() {
        if (statusBatelada == 4) return true;
        return false;
    }


    // - - - - - - - - - - OPERADOR - - - - - - - - - - - //
    void openOp() {
        inicializaEndRegistradores(-1);
        batelada_valor = -1;
        status_valor = -1;
        volume_valor = -1;
        status_vs_valor = -1;
        max_vol_valor = -1;
        vazao_valor = -1;
        mult_factor_valor = -1;
        volume_barril_valor = -1;
        problema_vs_valor = -1;
    }

    boolean simulaServida() {
        if (master.readRegister(STATUS_REG) == 10) {
            master.writeRegisters(MULT_FACTOR_REG, 15);
            master.writeRegisters(MAX_VOL_REG, volumeProgramado); //seta o volume maximo
            master.writeRegisters(STATUS_REG, 20); //seta status para programado
            master.writeRegisters(BATELADA_REG, 1); //abre a batelada
            statusBatelada = 1;
            Log.d("teste", "abriu batelada");
            volume = 0;
            return true;
        }
        return false;
    }


    public int monitorsCLPOp() {
        if(finalizaOp){
            return 1;
        }
        batelada_valor = master.readRegister(BATELADA_REG);
        status_valor = master.readRegister(STATUS_REG);
        volume_valor = master.readRegister(VOLUME_REG);
        status_vs_valor = master.readRegister(STATUS_VS_REG);
        max_vol_valor = master.readRegister(MAX_VOL_REG);
        vazao_valor = master.readRegister(VAZAO_REG);
        mult_factor_valor = master.readRegister(MULT_FACTOR_REG);
        volume_barril_valor = master.readRegister(VOLUME_BARRIL_REG);
        problema_vs_valor = master.readRegister(PROBLEMA_VS_REG);

        sleep(200);
        return monitorsCLPOp();
    }


    //Retorna o significado dos registradores
    public String getBatelaStr() { //status da servida
        int valor = batelada_valor;

        switch (valor) {
            case 0:
                return "Esperando (0)";
            case 1:
                return "Inicia servida (1)";
            case 2:
                return "Comando para fechar válvula (2)";
            case 3:
                return "Cliente terminou de se servir (3)";
            case 4:
                return "Servida finalizada (4)";
            default:
                return "Erro na leitura (" + valor + ")";
        }
    }

    public String getStatusStr() { //status da chopeira
        int valor = status_valor;

        switch (valor) {
            case 10:
                return "Parada (10)";
            case 20:
                return "Programada (20)";
            case 30:
                return "Torneira aberta (30)";
            case 40:
                return "Servida finalizada (40)";
            default:
                return "Erro na leitura (" + valor + ")";
        }
    }

    public String getVolumeStr() { //status da chopeira
        return String.valueOf(volume_valor);
    }

    public String getStatusValvulaStr() { //status da chopeira
        int valor = status_vs_valor;

        switch (valor) {
            case 0:
                return "Fechada (0)";
            case 1:
                return "Aberta (1)";
            default:
                return "Erro na leitura (" + valor + ")";
        }
    }

    public String getVazaoStr() {
        return String.valueOf(vazao_valor);
    }

    public String getMultFatorStr() {
        return String.valueOf(mult_factor_valor);
    }

    public String getVolumeBarrilStr() {
        return String.valueOf(volume_barril_valor);
    }

    public String getProblemaVsStr() {
        int valor = problema_vs_valor;

        switch (valor) {
            case 0:
                return "Normal (0)";
            case 1:
                return "Possível vazamento (1)";
            default:
                return "Erro na leitura (" + valor + ")";
        }
    }

    public void setFinalizaOp(boolean status){
        this.finalizaOp = status;
    }


    // - - - - - - - - - - DRUPAL - - - - - - - - - - - //

    public void atualizaInfoDrupal(int nid_chopeira, int id_chopeira, int nid_cerveja, int volume_consumido) throws JSONException {
        inicializaEndRegistradores(id_chopeira);
        JSONObject jobj;
        jobj = new JSONObject();

        jobj.put("nid_chopeira", nid_chopeira); //id_item_automatize
        jobj.put("nid_cerveja", nid_cerveja);
        jobj.put("volume_consumido", volume_consumido);
        String data = jobj.toString();

        String url = "http://divinapolenta.cloud.fluidobjects.com/json/put";
        ExportJSON.sendJSON(url, data);
    }

    public void setMaxVol(int max){
        master.writeRegisters(MAX_VOL_REG, max);
    }
}
