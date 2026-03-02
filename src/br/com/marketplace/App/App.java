package br.com.marketplace.App;

import java.util.Scanner;

import br.com.marketplace.model.Produto;
import br.com.marketplace.service.Deposito;

public class App {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Deposito deposito = new Deposito();

        boolean sair = false;

        while (!sair) {
            System.out.println("\n========= GESTOR Marketplace =========");
            System.out.println("1 - Adicionar Produto");
            System.out.println("2 - Remover Produto");
            System.out.println("3 - Repor Produto");
            System.out.println("4 - Vender Produto");
            System.out.println("5 - Listar Produtos Disponíveis");
            System.out.println("6 - Listar Todos os Produtos");
            System.out.println("0 - Sair");
            System.out.print("=============== Escolha uma opção: ============================= ");

            int opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1:
                    System.out.print("Digite o nome do produto: ");
                    String nomeAdd = sc.nextLine();
                    System.out.print("Digite a quantidade: ");
                    int qtdAdd = sc.nextInt();
                    System.out.print("Digite o valor: ");
                    double valorAdd = sc.nextDouble();
                    sc.nextLine();

                    boolean disponivelAdd = qtdAdd > 0;
                    deposito.adicionarProduto(new Produto(nomeAdd, qtdAdd, valorAdd, disponivelAdd));
                    System.out.println("Produto adicionado com sucesso!");
                    break;

                case 2:
                    System.out.print("Digite o nome do produto que deseja remover: ");
                    String nomeRemover = sc.nextLine();

                    Produto prodRemover = null;
                    for (Produto p : deposito.getProdutos()) {
                        if (p.getNome().equalsIgnoreCase(nomeRemover)) {
                            prodRemover = p;
                            break;
                        }
                    }

                    if (prodRemover != null) {
                        deposito.removerProduto(prodRemover);
                        System.out.println("Produto removido com sucesso!");
                    } else {
                        System.out.println("Produto não encontrado.");
                    }
                    break;

                case 3:
                    System.out.print("Digite o nome do produto que deseja repor: ");
                    String nomeRepor = sc.nextLine();

                    Produto prodRepor = null;
                    for (Produto p : deposito.getProdutos()) {
                        if (p.getNome().equalsIgnoreCase(nomeRepor)) {
                            prodRepor = p;
                            break;
                        }
                    }

                    if (prodRepor != null) {
                        System.out.print("Digite a quantidade para repor: ");
                        int qtdRepor = sc.nextInt();
                        sc.nextLine();
                        deposito.reporProduto(prodRepor, qtdRepor);
                        System.out.println("Produto reabastecido com sucesso!");
                    } else {
                        System.out.println("Produto não encontrado.");
                    }
                    break;

                case 4:
                    System.out.print("Digite o nome do produto que deseja vender: ");
                    String nomeVender = sc.nextLine();

                    Produto prodVender = null;
                    for (Produto p : deposito.getProdutos()) {
                        if (p.getNome().trim().equalsIgnoreCase(nomeVender.trim())) {
                            prodVender = p;
                            break;
                        }
                    }

                    if (prodVender != null) {
                        System.out.print("Digite a quantidade que deseja vender: ");
                        int qtdVender = sc.nextInt();
                        sc.nextLine();
                        deposito.venderProduto(prodVender, qtdVender);
                    } else {
                        System.out.println("Produto não encontrado.");
                    }
                    break;

                case 5:
                    System.out.println("========= Produtos Disponíveis: =========");
                    deposito.listarProdutosDisponiveis();
                    break;

                case 6:
                    System.out.println("========= Todos os Produtos: =========");
                    deposito.listarProdutos();
                    break;

                case 0:
                    System.out.println("Saindo do sistema...");
                    sair = true;
                    break;

                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        }

        sc.close();
    }
}