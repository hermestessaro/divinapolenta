package br.com.httpfluidobjects.appdivinapolenta;

/**
 * Created by gabrielweich on 11/09/17.
 */

public class chopeira {
    private int id;
    private int nid;
    private String title;
    private String type;
    private int id_cerveja;
    private float quantidade;

    public void setId(String i)
    {
        id = Integer.parseInt(i);
    }

    public void setIdCeva(String i)
    {
        if(i != "null") id_cerveja = Integer.parseInt(i);
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
    public void setQuantidade(String q)
    {
        if(q != "null") quantidade = Float.parseFloat(q);
    }

    public int getId(){
        return id;
    }
    public int getNid(){
        return nid;
    }
    public String getTitle(){
        return title;
    }
    public String getType(){
        return type;
    }

    public int getIdCerveja(){
        return id_cerveja;
    }
    public float getQuantidade(){
        return quantidade;
    }

    @Override
    public String toString() {
        return "chopeira{" +
                "id=" + id +
                ", title='" + title + '\'' +
                //", type='" + type + '\'' +
                ", id_cerveja=" + id_cerveja +
                ", quantidade=" + quantidade +
                '}';
    }
}
