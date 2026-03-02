package br.com.marketplace.model;

public class Produto {
    private String nome;
    private int quantidade;
    private double valor;
    private boolean disponivel;

    public Produto(String nome, int quantidade, double valor, boolean disponivel) {
        this.nome = nome;
        this.quantidade = quantidade;
        this.valor = valor;
        this.disponivel = disponivel;
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getValor() {
        return valor;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    // Setters (adicionados para permitir atualização)
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
        this.disponivel = this.quantidade > 0;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    @Override
    public String toString() {
        return String.format("%s (Qtd: %d, R$ %.2f)", nome, quantidade, valor);
    }
}