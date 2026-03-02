package br.com.marketplace.service;

import br.com.marketplace.model.Produto;
import java.util.ArrayList;
import java.util.List;

public class Deposito {
    private List<Produto> produtos;

    public Deposito() {
        this.produtos = new ArrayList<>();
    }

    public void adicionarProduto(Produto produto) {
        produtos.add(produto);
    }

    public void removerProduto(Produto produto) {
        produtos.remove(produto);
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void venderProduto(Produto produto, int quantidade) {
        if (produto != null && produto.getQuantidade() >= quantidade) {
            produto.setQuantidade(produto.getQuantidade() - quantidade);
        }
    }
}