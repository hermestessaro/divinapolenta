package br.com.httpfluidobjects.appdivinapolenta;

import android.graphics.Bitmap;

/**
 * Created by hermestessaro on 06/09/17.
 */

public class cerveja {
    int id;
    int nid;
    int id_automatize;
    String title;
    String type;
    String descricao;
    String fabricante;
    float valor;
    int ibu;
    int ebc;
    float consumo;
    String logo_name;
    String logo_uri;
    float ab;
    Bitmap imageBM;

    public void setId(String i)
    {
        id = Integer.parseInt(i);
    }
    public void setIdAutomatize(String i) {
        id_automatize = Integer.parseInt(i);
    }
    public void setNid(String i)
    {
        nid = Integer.parseInt(i);
    }
    public void setTitle(String t){
        title = t;
    }
    public void setType(String t){
        type = t;
    }
    public void setDescricao(String d){
        descricao = d;
    }
    public void setFabricante(String f){
        fabricante = f;
    }
    public void setLogo_name(String ln){
        logo_name = ln;
    }
    public void setLogo_uri(String lu){
        logo_uri = lu;
    }
    public void setAb(String s){
        if(s != "null") ab = Float.parseFloat(s);
    }
    public void setValor(String s){
        if(s != "null") valor = Float.parseFloat(s);
    }
    public void setConsumo(String s){
        if(s != "null") consumo = Float.parseFloat(s);
    }
    public void setIbu(String s){
        if(s != "null") ibu = Integer.parseInt(s);
    }
    public void setEbc(String s){
        if(s != "null") ebc = Integer.parseInt(s);
    }

    public void setImageBM(Bitmap b){
        imageBM = b;
    }

    public int getId(){
        return id;
    }
    public int getNid(){
        return nid;
    }
    public int getIdAutomatize() { return id_automatize; }
    public String getTitle(){
        return title;
    }
    public String getType(){
        return type;
    }
    public String getDescricao(){
        return descricao;
    }
    public String getFabricante(){
        return fabricante;
    }
    public float getValor(){
        return valor;
    }
    public int getIbu(){
        return ibu;
    }
    public int getEbc(){
        return ebc;
    }
    public float getAb(){
        return ebc;
    }
    public float getConsumo(){
        return consumo;
    }
    public String getLogo_name(){
        return logo_name;
    }
    public String getLogo_uri(){
        return logo_uri;
    }
    public Bitmap getImageBM(){return imageBM;}

}
