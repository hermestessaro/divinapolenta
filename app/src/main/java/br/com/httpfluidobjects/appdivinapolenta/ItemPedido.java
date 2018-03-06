package br.com.httpfluidobjects.appdivinapolenta;

/**
 * Created by gabrielabreu on 13/09/17.
 */

//POST pedidos/{pedidoId}/item
public class ItemPedido {
    private int id;
    private int codigo;
    private int id_item;
    private double quantidade;
    private double unitario;
    private double total;
    private double total_liquido;
    private int tipo;
    private double desconto;
    private String observacao;
    private String descricao;

    public ItemPedido(int id, int codigo, int id_item, double quantidade, double total, double unitario) {
        this.id = id;
        this.codigo = codigo;
        this.id_item = id_item;
        this.quantidade = quantidade;
        this.total = total;
        this.unitario = unitario;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public void setId_item(int id_item) {
        this.id_item = id_item;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public void setUnitario(double unitario) {
        this.unitario = unitario;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setTotal_liquido(double total_liquido) {
        this.total_liquido = total_liquido;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getId() {
        return id;
    }

    public int getCodigo() {
        return codigo;
    }

    public int getId_item() {
        return id_item;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public String getObservacao() {
        return observacao;
    }

    public double getDesconto() {
        return desconto;
    }

    public int getTipo() {
        return tipo;
    }

    public double getTotal_liquido() {
        return total_liquido;
    }

    public double getTotal() {
        return total;
    }

    public double getUnitario() {
        return unitario;
    }

    public String getDescricao() {
        return descricao;
    }
}
