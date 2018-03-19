package br.com.httpfluidobjects.appdivinapolenta;

/**
 * Created by hermestessaro on 19/03/2018.
 */

public class cliente {
    int id;
    int nid;
    String nome;
    String cartao;
    String cpf;
    float valor_comprado;
    float saldo;
    boolean isValid;
    String telefone;

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }


    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCartao() {
        return cartao;
    }

    public void setCartao(String cartao) {
        this.cartao = cartao;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public float getValor_comprado() {
        return valor_comprado;
    }

    public void setValor_comprado(float valor_comprado) {
        this.valor_comprado = valor_comprado;
    }

    public float getSaldo(){
        return saldo;
    }

    public void setSaldo(float saldo){
        this.saldo = saldo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
