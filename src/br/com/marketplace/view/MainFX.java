package br.com.marketplace.view;

import br.com.marketplace.model.Produto;
import br.com.marketplace.service.Deposito;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Callback;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class MainFX extends Application {

    private Deposito deposito = new Deposito();
    private TextArea textAreaLog;
    private TableView<Produto> tableView;
    private ObservableList<Produto> produtosObservable;
    private TextArea areaRelatorio;
    private ListView<String> listViewDashboard;
    private Label lblEstoqueCabecalho;
    private TextField txtFiltro;
    private List<Venda> listaVendas = new ArrayList<>();
    private ObservableList<Venda> vendasObservable;
    
    // Componentes do formulário que precisam ser acessíveis globalmente
    private TextField txtNome;
    private TextField txtQuantidade;
    private TextField txtValor;
    private ComboBox<Produto> comboSelecionarProduto;

    // Cores do tema brasileiro premium
    private static final String VERDE_BRASIL = "#009c3b";
    private static final String AMARELO_BRASIL = "#ffdf00";
    private static final String AZUL_BRASIL = "#002776";
    private static final String BRANCO = "#ffffff";
    private static final String CINZA_CLARO = "#f5f5f5";
    private static final String CINZA_MEDIO = "#e0e0e0";
    private static final String TEXTO_ESCURO = "#333333";

    // Classe interna para Venda
    public class Venda {
        private String nomeProduto;
        private int quantidade;
        private double valorUnitario;
        private double valorTotal;
        private LocalDateTime dataHora;

        public Venda(String nomeProduto, int quantidade, double valorUnitario) {
            this.nomeProduto = nomeProduto;
            this.quantidade = quantidade;
            this.valorUnitario = valorUnitario;
            this.valorTotal = quantidade * valorUnitario;
            this.dataHora = LocalDateTime.now();
        }

        public String getNomeProduto() { return nomeProduto; }
        public int getQuantidade() { return quantidade; }
        public double getValorUnitario() { return valorUnitario; }
        public double getValorTotal() { return valorTotal; }
        public LocalDateTime getDataHora() { return dataHora; }

        public String getDataFormatada() {
            return dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        @Override
        public String toString() {
            return String.format("%s - %d x R$ %.2f = R$ %.2f (%s)",
                    nomeProduto, quantidade, valorUnitario, valorTotal, getDataFormatada());
        }
    }

    @Override
    public void start(Stage stage) {
        inicializarDados();

        stage.setTitle("🇧🇷 Mercatto Brasil - Gestão Premium");

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: " + BRANCO + ";");
        
        tabPane.getTabs().addAll(
                criarTabDashboard(),
                criarTabProdutos(),
                criarTabVendas(),
                criarTabRelatorios(),
                criarTabHistoricoVendas());

        VBox layoutPrincipal = new VBox(criarCabecalho(), tabPane);
        layoutPrincipal.setStyle("-fx-background-color: " + CINZA_CLARO + ";");

        Scene scene = new Scene(layoutPrincipal, 1300, 800);
        stage.setScene(scene);
        stage.show();
    }

    private HBox criarCabecalho() {
        HBox cabecalho = new HBox();
        cabecalho.setPadding(new Insets(20));
        cabecalho.setStyle("-fx-background-color: linear-gradient(to right, " + VERDE_BRASIL + ", " + AZUL_BRASIL + ");");
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("🇧🇷 MERCATTO BRASIL");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titulo.setTextFill(Color.WHITE);
        
        Label subTitulo = new Label("Sistema de Gestão Comercial Premium");
        subTitulo.setFont(Font.font("Segoe UI", 14));
        subTitulo.setTextFill(Color.web("#f0f0f0"));
        subTitulo.setPadding(new Insets(5, 0, 0, 10));

        VBox textos = new VBox(titulo, subTitulo);

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        VBox cardResumo = new VBox(5);
        cardResumo.setPadding(new Insets(10, 20, 10, 20));
        cardResumo.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 10;");
        
        lblEstoqueCabecalho = new Label();
        lblEstoqueCabecalho.setTextFill(Color.WHITE);
        lblEstoqueCabecalho.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        atualizarCabecalho();

        cardResumo.getChildren().add(lblEstoqueCabecalho);
        cabecalho.getChildren().addAll(textos, espaco, cardResumo);
        
        return cabecalho;
    }

    private Tab criarTabDashboard() {
        Tab tab = new Tab("🏠 Dashboard");
        tab.setClosable(false);
        tab.setStyle("-fx-font-weight: bold;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: " + CINZA_CLARO + ";");

        // Cards de resumo em grid
        GridPane gridCards = new GridPane();
        gridCards.setHgap(20);
        gridCards.setVgap(20);
        gridCards.setAlignment(Pos.CENTER);

        // Criar cards com valores atualizados
        atualizarCardsDashboard(gridCards);

        // Gráficos lado a lado
        HBox graficosBox = new HBox(20);
        graficosBox.setAlignment(Pos.CENTER);
        
        VBox boxGraficoProdutos = criarGraficoProdutosPremium();
        VBox boxGraficoVendas = criarGraficoVendasPremium();
        
        graficosBox.getChildren().addAll(boxGraficoProdutos, boxGraficoVendas);
        HBox.setHgrow(boxGraficoProdutos, Priority.ALWAYS);
        HBox.setHgrow(boxGraficoVendas, Priority.ALWAYS);

        // Lista de produtos em destaque
        VBox listaDestaque = new VBox(15);
        listaDestaque.setPadding(new Insets(20));
        listaDestaque.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label lblDestaque = new Label("📋 Produtos em Destaque");
        lblDestaque.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblDestaque.setTextFill(Color.web(TEXTO_ESCURO));

        listViewDashboard = new ListView<>();
        listViewDashboard.setPrefHeight(200);
        listViewDashboard.setStyle("-fx-background-color: transparent; -fx-border-color: " + CINZA_MEDIO + ";");
        
        // Listener para seleção na lista do dashboard
        listViewDashboard.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String nomeProduto = newVal.split(" • ")[0];
                preencherFormularioParaEdicao(nomeProduto);
            }
        });
        
        atualizarListViewDashboard();

        HBox botoesDashboard = new HBox(10);
        botoesDashboard.setAlignment(Pos.CENTER);
        
        Button btnAtualizar = criarBotaoPremium("🔄 Atualizar Dashboard", VERDE_BRASIL);
        btnAtualizar.setOnAction(e -> {
            atualizarDashboard();
            // Recriar os cards com valores atualizados
            gridCards.getChildren().clear();
            atualizarCardsDashboard(gridCards);
        });

        botoesDashboard.getChildren().add(btnAtualizar);

        listaDestaque.getChildren().addAll(lblDestaque, listViewDashboard, botoesDashboard);

        container.getChildren().addAll(gridCards, graficosBox, listaDestaque);
        scrollPane.setContent(container);
        tab.setContent(scrollPane);

        return tab;
    }

    private void atualizarCardsDashboard(GridPane gridCards) {
        VBox cardTotalProdutos = criarCardPremium("📦 Total Produtos",
                String.valueOf(deposito.getProdutos().size()), VERDE_BRASIL);
        VBox cardDisponiveis = criarCardPremium("✅ Disponível",
                String.valueOf(deposito.getProdutos().stream().filter(Produto::isDisponivel).count()), AZUL_BRASIL);
        VBox cardValorEstoque = criarCardPremium("💰 Valor Estoque",
                "R$ " + String.format("%,.2f", calcularValorTotalEstoque()), AMARELO_BRASIL);
        VBox cardVendasHoje = criarCardPremium("💸 Vendas Hoje",
                "R$ " + String.format("%,.2f", calcularTotalVendasHoje()), "#e74c3c");

        gridCards.add(cardTotalProdutos, 0, 0);
        gridCards.add(cardDisponiveis, 1, 0);
        gridCards.add(cardValorEstoque, 2, 0);
        gridCards.add(cardVendasHoje, 3, 0);
    }

    private VBox criarGraficoProdutosPremium() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label lblTitulo = new Label("📊 Top 5 Produtos");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTitulo.setTextFill(Color.web(TEXTO_ESCURO));

        PieChart pieChart = new PieChart();
        pieChart.setTitle("");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setPrefSize(400, 300);
        pieChart.setStyle("-fx-font-size: 12px;");

        atualizarGraficoProdutos(pieChart);

        container.getChildren().addAll(lblTitulo, pieChart);
        return container;
    }

    private void atualizarGraficoProdutos(PieChart pieChart) {
        pieChart.getData().clear();
        
        List<Produto> produtosOrdenados = new ArrayList<>(deposito.getProdutos());
        produtosOrdenados.sort((p1, p2) -> Integer.compare(p2.getQuantidade(), p1.getQuantidade()));

        int count = 0;
        for (Produto p : produtosOrdenados) {
            if (count < 5 && p.getQuantidade() > 0) {
                PieChart.Data slice = new PieChart.Data(p.getNome() + " (" + p.getQuantidade() + ")", p.getQuantidade());
                pieChart.getData().add(slice);
                count++;
            }
        }
    }

    private VBox criarGraficoVendasPremium() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label lblTitulo = new Label("💰 Vendas do Dia");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTitulo.setTextFill(Color.web(TEXTO_ESCURO));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Produtos");
        yAxis.setLabel("Quantidade");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setPrefSize(400, 300);
        barChart.setLegendVisible(false);
        barChart.setAnimated(true);
        barChart.setStyle("-fx-font-size: 11px;");

        atualizarGraficoVendas(barChart);

        if (!barChart.getData().isEmpty()) {
            container.getChildren().addAll(lblTitulo, barChart);
        } else {
            Label lblSemVendas = new Label("Nenhuma venda registrada hoje");
            lblSemVendas.setFont(Font.font("Segoe UI", 14));
            lblSemVendas.setTextFill(Color.GRAY);
            lblSemVendas.setPadding(new Insets(50, 0, 50, 0));
            container.getChildren().addAll(lblTitulo, lblSemVendas);
        }

        return container;
    }

    private void atualizarGraficoVendas(BarChart<String, Number> barChart) {
        barChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Vendas Hoje");

        LocalDate hoje = LocalDate.now();
        for (Produto p : deposito.getProdutos()) {
            long vendasProduto = listaVendas.stream()
                    .filter(v -> v.getNomeProduto().equals(p.getNome()) &&
                            v.getDataHora().toLocalDate().equals(hoje))
                    .count();
            if (vendasProduto > 0) {
                series.getData().add(new XYChart.Data<>(p.getNome(), vendasProduto));
            }
        }

        if (!series.getData().isEmpty()) {
            barChart.getData().add(series);
        }
    }

    private Tab criarTabProdutos() {
        Tab tab = new Tab("📦 Produtos");
        tab.setClosable(false);
        tab.setStyle("-fx-font-weight: bold;");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: " + CINZA_CLARO + ";");

        // Painel de busca e seleção
        HBox painelSelecao = new HBox(15);
        painelSelecao.setAlignment(Pos.CENTER_LEFT);
        painelSelecao.setPadding(new Insets(15));
        painelSelecao.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblSelecionar = new Label("🔍 Selecionar Produto:");
        lblSelecionar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        comboSelecionarProduto = new ComboBox<>();
        comboSelecionarProduto.setPromptText("Clique para selecionar um produto...");
        comboSelecionarProduto.setPrefWidth(350);
        comboSelecionarProduto.setStyle(criarEstiloCampo());
        
        // Configurar como mostrar o produto no ComboBox
        comboSelecionarProduto.setCellFactory(new Callback<ListView<Produto>, ListCell<Produto>>() {
            @Override
            public ListCell<Produto> call(ListView<Produto> param) {
                return new ListCell<Produto>() {
                    @Override
                    protected void updateItem(Produto item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getNome() + " (Qtd: " + item.getQuantidade() + ")");
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
        
        comboSelecionarProduto.setButtonCell(new ListCell<Produto>() {
            @Override
            protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getNome() + " (Qtd: " + item.getQuantidade() + ")");
                } else {
                    setText(null);
                }
            }
        });
        
        // Atualizar combo box
        atualizarComboSelecaoProdutos();
        
        // Listener para seleção
        comboSelecionarProduto.setOnAction(e -> {
            Produto selecionado = comboSelecionarProduto.getValue();
            if (selecionado != null) {
                preencherFormularioComProduto(selecionado);
            }
        });

        painelSelecao.getChildren().addAll(lblSelecionar, comboSelecionarProduto);

        // Formulário de cadastro
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblFormTitulo = new Label("➕ Cadastro / Edição de Produtos");
        lblFormTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblFormTitulo.setTextFill(Color.web(VERDE_BRASIL));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);

        txtNome = new TextField();
        txtNome.setPromptText("Ex: Café do Brasil");
        txtNome.setPrefWidth(300);
        txtNome.setStyle(criarEstiloCampo());

        txtQuantidade = new TextField();
        txtQuantidade.setPromptText("0");
        txtQuantidade.setPrefWidth(150);
        txtQuantidade.setStyle(criarEstiloCampo());

        txtValor = new TextField();
        txtValor.setPromptText("0.00");
        txtValor.setPrefWidth(150);
        txtValor.setStyle(criarEstiloCampo());

        form.add(new Label("Nome do Produto:"), 0, 0);
        form.add(txtNome, 1, 0, 2, 1);
        form.add(new Label("Quantidade:"), 0, 1);
        form.add(txtQuantidade, 1, 1);
        form.add(new Label("Valor Unitário (R$):"), 2, 1);
        form.add(txtValor, 3, 1);

        HBox botoes = new HBox(10);
        botoes.setAlignment(Pos.CENTER);
        botoes.setPadding(new Insets(20, 0, 0, 0));

        Button btnAdicionar = criarBotaoPremium("➕ Adicionar Novo", VERDE_BRASIL);
        Button btnAtualizar = criarBotaoPremium("✏️ Atualizar Selecionado", AZUL_BRASIL);
        Button btnRemover = criarBotaoPremium("🗑️ Remover Selecionado", "#e74c3c");
        Button btnLimpar = criarBotaoPremium("🧹 Limpar Formulário", "#95a5a6");
        Button btnExportar = criarBotaoPremium("📤 Exportar CSV", "#8e44ad");

        btnAdicionar.setOnAction(e -> adicionarProduto(txtNome, txtQuantidade, txtValor));
        btnAtualizar.setOnAction(e -> atualizarProdutoSelecionado());
        btnRemover.setOnAction(e -> removerProdutoSelecionado());
        btnLimpar.setOnAction(e -> limparFormularioProduto());
        btnExportar.setOnAction(e -> exportarParaCSV());

        botoes.getChildren().addAll(btnAdicionar, btnAtualizar, btnRemover, btnLimpar, btnExportar);

        formContainer.getChildren().addAll(lblFormTitulo, form, botoes);

        // Painel de busca rápida
        HBox painelBusca = new HBox(15);
        painelBusca.setAlignment(Pos.CENTER_LEFT);
        painelBusca.setPadding(new Insets(15));
        painelBusca.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblBusca = new Label("🔍 Filtrar:");
        lblBusca.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        txtFiltro = new TextField();
        txtFiltro.setPromptText("Digite para filtrar produtos...");
        txtFiltro.setPrefWidth(350);
        txtFiltro.setStyle(criarEstiloCampo());

        Button btnLimparBusca = criarBotaoPremium("🧹 Limpar Filtro", "#95a5a6");
        btnLimparBusca.setOnAction(e -> txtFiltro.clear());

        painelBusca.getChildren().addAll(lblBusca, txtFiltro, btnLimparBusca);

        // Tabela de produtos
        VBox tabelaContainer = new VBox(10);
        tabelaContainer.setPadding(new Insets(20));
        tabelaContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblTabela = new Label("📋 Produtos em Estoque");
        lblTabela.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        tableView = new TableView<>();
        produtosObservable = FXCollections.observableArrayList(deposito.getProdutos());
        
        // Listener para seleção na tabela
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                preencherFormularioComProduto(newVal);
            }
        });
        
        configurarFiltro();
        configurarColunasTabela();
        
        tableView.setStyle("-fx-font-size: 13px;");
        tableView.setPrefHeight(300);

        tabelaContainer.getChildren().addAll(lblTabela, tableView);

        layout.getChildren().addAll(painelSelecao, formContainer, painelBusca, tabelaContainer);
        tab.setContent(layout);

        return tab;
    }

    private void preencherFormularioComProduto(Produto produto) {
        if (produto != null) {
            txtNome.setText(produto.getNome());
            txtQuantidade.setText(String.valueOf(produto.getQuantidade()));
            txtValor.setText(String.format("%.2f", produto.getValor()));
        }
    }

    private void preencherFormularioParaEdicao(String nomeProduto) {
        Produto produto = buscarProdutoPorNome(nomeProduto);
        if (produto != null) {
            preencherFormularioComProduto(produto);
            
            // Selecionar o produto no combo box também
            comboSelecionarProduto.setValue(produto);
        }
    }

    private void limparFormularioProduto() {
        txtNome.clear();
        txtQuantidade.clear();
        txtValor.clear();
        comboSelecionarProduto.setValue(null);
        tableView.getSelectionModel().clearSelection();
    }

    private void atualizarProdutoSelecionado() {
        Produto produtoSelecionado = comboSelecionarProduto.getValue();
        
        if (produtoSelecionado == null) {
            // Tenta pegar da tabela
            produtoSelecionado = tableView.getSelectionModel().getSelectedItem();
        }
        
        if (produtoSelecionado == null) {
            mostrarAlerta("Atenção", "Selecione um produto no combo box ou na tabela!");
            return;
        }

        try {
            boolean atualizado = false;
            String nome = txtNome.getText().trim();
            String qtd = txtQuantidade.getText().trim();
            String valor = txtValor.getText().trim();

            if (!nome.isEmpty() && !nome.equals(produtoSelecionado.getNome())) {
                produtoSelecionado.setNome(nome);
                atualizado = true;
            }

            if (!qtd.isEmpty()) {
                int quantidade = Integer.parseInt(qtd);
                if (quantidade > 0 && quantidade != produtoSelecionado.getQuantidade()) {
                    produtoSelecionado.setQuantidade(quantidade);
                    atualizado = true;
                }
            }

            if (!valor.isEmpty()) {
                double valorDouble = Double.parseDouble(valor);
                if (valorDouble > 0 && valorDouble != produtoSelecionado.getValor()) {
                    produtoSelecionado.setValor(valorDouble);
                    atualizado = true;
                }
            }

            if (atualizado) {
                tableView.refresh();
                if (areaRelatorio != null) {
                    areaRelatorio.appendText("✏️ Produto atualizado: " + produtoSelecionado.getNome() + "\n");
                }
                atualizarComboSelecaoProdutos();
                atualizarDashboard();
                limparFormularioProduto();
                mostrarAlerta("Sucesso", "Produto atualizado!");
            } else {
                mostrarAlerta("Atenção", "Nenhuma alteração detectada!");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Quantidade e valor devem ser números válidos!");
        }
    }

    private void removerProdutoSelecionado() {
        Produto produtoSelecionado = comboSelecionarProduto.getValue();
        
        if (produtoSelecionado == null) {
            produtoSelecionado = tableView.getSelectionModel().getSelectedItem();
        }
        
        if (produtoSelecionado == null) {
            mostrarAlerta("Atenção", "Selecione um produto no combo box ou na tabela!");
            return;
        }

        // Confirmar remoção
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Remoção");
        confirm.setHeaderText("Remover produto: " + produtoSelecionado.getNome());
        confirm.setContentText("Tem certeza que deseja remover este produto?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            deposito.removerProduto(produtoSelecionado);
            produtosObservable.remove(produtoSelecionado);
            
            if (areaRelatorio != null) {
                areaRelatorio.appendText("❌ Produto removido: " + produtoSelecionado.getNome() + "\n");
            }
            
            atualizarComboSelecaoProdutos();
            atualizarDashboard();
            limparFormularioProduto();
            mostrarAlerta("Sucesso", "Produto removido!");
        }
    }

    private void atualizarComboSelecaoProdutos() {
        if (comboSelecionarProduto != null) {
            ObservableList<Produto> produtos = FXCollections.observableArrayList(deposito.getProdutos());
            produtos.sort(Comparator.comparing(Produto::getNome));
            comboSelecionarProduto.setItems(produtos);
        }
    }

    private void configurarColunasTabela() {
        TableColumn<Produto, String> colNome = new TableColumn<>("Produto");
        colNome.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNome()));
        colNome.setPrefWidth(250);

        TableColumn<Produto, Integer> colQuantidade = new TableColumn<>("Quantidade");
        colQuantidade.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getQuantidade()).asObject());
        colQuantidade.setPrefWidth(120);
        colQuantidade.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Produto, Double> colValor = new TableColumn<>("Valor Unit.");
        colValor.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getValor()).asObject());
        colValor.setPrefWidth(120);
        colValor.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Produto, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().isDisponivel() ? "✅ Disponível" : "❌ Esgotado"));
        colStatus.setPrefWidth(120);
        colStatus.setStyle("-fx-alignment: CENTER;");

        TableColumn<Produto, Double> colValorTotal = new TableColumn<>("Valor Total");
        colValorTotal.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(
                cellData.getValue().getQuantidade() * cellData.getValue().getValor()).asObject());
        colValorTotal.setPrefWidth(150);
        colValorTotal.setStyle("-fx-alignment: CENTER-RIGHT;");

        tableView.getColumns().addAll(colNome, colQuantidade, colValor, colStatus, colValorTotal);
    }

    private Tab criarTabVendas() {
        Tab tab = new Tab("💰 Vendas");
        tab.setClosable(false);
        tab.setStyle("-fx-font-weight: bold;");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: " + CINZA_CLARO + ";");

        // Painel de venda
        VBox painelVenda = new VBox(20);
        painelVenda.setPadding(new Insets(25));
        painelVenda.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label lblTituloVenda = new Label("💰 Realizar Nova Venda");
        lblTituloVenda.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTituloVenda.setTextFill(Color.web(VERDE_BRASIL));

        GridPane gridVenda = new GridPane();
        gridVenda.setHgap(20);
        gridVenda.setVgap(15);
        gridVenda.setAlignment(Pos.CENTER);

        ComboBox<Produto> comboProdutos = new ComboBox<>();
        comboProdutos.setPromptText("Selecione um produto...");
        comboProdutos.setPrefWidth(350);
        comboProdutos.setStyle(criarEstiloCampo());
        
        // Configurar como mostrar o produto no ComboBox
        comboProdutos.setCellFactory(new Callback<ListView<Produto>, ListCell<Produto>>() {
            @Override
            public ListCell<Produto> call(ListView<Produto> param) {
                return new ListCell<Produto>() {
                    @Override
                    protected void updateItem(Produto item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getNome() + " (Estoque: " + item.getQuantidade() + ")");
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
        
        comboProdutos.setButtonCell(new ListCell<Produto>() {
            @Override
            protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getNome() + " (Estoque: " + item.getQuantidade() + ")");
                } else {
                    setText(null);
                }
            }
        });
        
        atualizarComboProdutos(comboProdutos);

        TextField txtQuantidadeVenda = new TextField();
        txtQuantidadeVenda.setPromptText("Digite a quantidade");
        txtQuantidadeVenda.setPrefWidth(200);
        txtQuantidadeVenda.setStyle(criarEstiloCampo());

        gridVenda.add(new Label("Produto:"), 0, 0);
        gridVenda.add(comboProdutos, 1, 0);
        gridVenda.add(new Label("Quantidade:"), 2, 0);
        gridVenda.add(txtQuantidadeVenda, 3, 0);

        // Informações do produto selecionado
        VBox infoProduto = new VBox(10);
        infoProduto.setPadding(new Insets(15));
        infoProduto.setStyle("-fx-background-color: " + CINZA_CLARO + "; -fx-background-radius: 8;");
        infoProduto.setVisible(false);

        Label lblInfoEstoque = new Label();
        lblInfoEstoque.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        Label lblInfoValor = new Label();
        Label lblInfoTotal = new Label();

        infoProduto.getChildren().addAll(lblInfoEstoque, lblInfoValor, lblInfoTotal);

        comboProdutos.setOnAction(e -> {
            Produto produto = comboProdutos.getValue();
            if (produto != null) {
                lblInfoEstoque.setText("📦 Estoque disponível: " + produto.getQuantidade() + " unidades");
                lblInfoEstoque.setTextFill(Color.web(VERDE_BRASIL));
                lblInfoValor.setText("💰 Valor unitário: R$ " + String.format("%,.2f", produto.getValor()));
                lblInfoTotal.setText("💎 Total em estoque: R$ " + 
                    String.format("%,.2f", produto.getQuantidade() * produto.getValor()));
                infoProduto.setVisible(true);
            }
        });

        HBox botoesVenda = new HBox(15);
        botoesVenda.setAlignment(Pos.CENTER);
        botoesVenda.setPadding(new Insets(10, 0, 0, 0));

        Button btnVender = criarBotaoPremium("🛒 Confirmar Venda", VERDE_BRASIL);
        Button btnLimparVenda = criarBotaoPremium("🧹 Limpar", "#95a5a6");

        btnVender.setOnAction(e -> {
            Produto produto = comboProdutos.getValue();
            String quantidadeStr = txtQuantidadeVenda.getText();

            if (produto == null) {
                mostrarAlerta("Atenção", "Selecione um produto!");
                return;
            }

            if (quantidadeStr.isEmpty()) {
                mostrarAlerta("Atenção", "Digite a quantidade!");
                return;
            }

            realizarVenda(produto, quantidadeStr);
            comboProdutos.setValue(null);
            txtQuantidadeVenda.clear();
            infoProduto.setVisible(false);
            atualizarComboProdutos(comboProdutos);
        });

        btnLimparVenda.setOnAction(e -> {
            comboProdutos.setValue(null);
            txtQuantidadeVenda.clear();
            infoProduto.setVisible(false);
        });

        botoesVenda.getChildren().addAll(btnVender, btnLimparVenda);

        painelVenda.getChildren().addAll(lblTituloVenda, gridVenda, infoProduto, botoesVenda);

        // Log de vendas
        VBox logContainer = new VBox(10);
        logContainer.setPadding(new Insets(20));
        logContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblLog = new Label("📝 Últimas Vendas Realizadas");
        lblLog.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        textAreaLog = new TextArea();
        textAreaLog.setEditable(false);
        textAreaLog.setPrefHeight(200);
        textAreaLog.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff00;");

        logContainer.getChildren().addAll(lblLog, textAreaLog);

        layout.getChildren().addAll(painelVenda, logContainer);
        tab.setContent(layout);

        return tab;
    }

    private Tab criarTabRelatorios() {
        Tab tab = new Tab("📈 Relatórios");
        tab.setClosable(false);
        tab.setStyle("-fx-font-weight: bold;");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: " + CINZA_CLARO + ";");

        // Painel de botões
        VBox botoesContainer = new VBox(15);
        botoesContainer.setPadding(new Insets(20));
        botoesContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblTitulo = new Label("📊 Gerar Relatórios");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web(AZUL_BRASIL));

        FlowPane botoesFlow = new FlowPane();
        botoesFlow.setHgap(10);
        botoesFlow.setVgap(10);
        botoesFlow.setAlignment(Pos.CENTER);

        Button btnCompleto = criarBotaoPremium("📋 Estoque Completo", AZUL_BRASIL);
        Button btnDisponiveis = criarBotaoPremium("✅ Disponíveis", VERDE_BRASIL);
        Button btnBaixoEstoque = criarBotaoPremium("⚠️ Baixo Estoque", AMARELO_BRASIL);
        Button btnVendas = criarBotaoPremium("💰 Vendas", "#9b59b6");
        Button btnExportar = criarBotaoPremium("📤 Exportar TXT", "#8e44ad");
        Button btnLimpar = criarBotaoPremium("🧹 Limpar", "#95a5a6");

        btnCompleto.setOnAction(e -> gerarRelatorioCompleto());
        btnDisponiveis.setOnAction(e -> gerarRelatorioDisponiveis());
        btnBaixoEstoque.setOnAction(e -> gerarRelatorioBaixoEstoque());
        btnVendas.setOnAction(e -> gerarRelatorioVendas());
        btnExportar.setOnAction(e -> exportarRelatorioParaArquivo());
        btnLimpar.setOnAction(e -> areaRelatorio.clear());

        botoesFlow.getChildren().addAll(btnCompleto, btnDisponiveis, btnBaixoEstoque, btnVendas, btnExportar, btnLimpar);
        botoesContainer.getChildren().addAll(lblTitulo, botoesFlow);

        // Área de relatório
        VBox relatorioContainer = new VBox(10);
        relatorioContainer.setPadding(new Insets(20));
        relatorioContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblRelatorio = new Label("📄 Visualização do Relatório");
        lblRelatorio.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        areaRelatorio = new TextArea();
        areaRelatorio.setEditable(false);
        areaRelatorio.setPrefHeight(400);
        areaRelatorio.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-control-inner-background: #f8f9fa;");

        relatorioContainer.getChildren().addAll(lblRelatorio, areaRelatorio);

        layout.getChildren().addAll(botoesContainer, relatorioContainer);
        tab.setContent(layout);

        return tab;
    }

    private Tab criarTabHistoricoVendas() {
        Tab tab = new Tab("📜 Histórico");
        tab.setClosable(false);
        tab.setStyle("-fx-font-weight: bold;");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: " + CINZA_CLARO + ";");

        // Resumo e botões
        VBox controleContainer = new VBox(15);
        controleContainer.setPadding(new Insets(20));
        controleContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblResumo = new Label();
        lblResumo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblResumo.setTextFill(Color.web(VERDE_BRASIL));
        atualizarResumoHistorico(lblResumo);

        FlowPane botoesHistorico = new FlowPane();
        botoesHistorico.setHgap(10);
        botoesHistorico.setVgap(10);
        botoesHistorico.setAlignment(Pos.CENTER);

        Button btnAtualizar = criarBotaoPremium("🔄 Atualizar", AZUL_BRASIL);
        Button btnExportar = criarBotaoPremium("📤 Exportar CSV", "#8e44ad");
        Button btnLimpar = criarBotaoPremium("🧹 Limpar Histórico", "#e74c3c");

        btnAtualizar.setOnAction(e -> {
            vendasObservable.setAll(listaVendas);
        });

        btnExportar.setOnAction(e -> exportarHistoricoCSV());

        btnLimpar.setOnAction(e -> {
            listaVendas.clear();
            vendasObservable.clear();
            mostrarAlerta("Sucesso", "Histórico de vendas limpo!");
            atualizarDashboard();
            atualizarResumoHistorico(lblResumo);
        });

        botoesHistorico.getChildren().addAll(btnAtualizar, btnExportar, btnLimpar);
        controleContainer.getChildren().addAll(lblResumo, botoesHistorico);

        // Tabela de histórico
        VBox tabelaContainer = new VBox(10);
        tabelaContainer.setPadding(new Insets(20));
        tabelaContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblTabela = new Label("📋 Vendas Realizadas");
        lblTabela.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        TableView<Venda> tabelaHistorico = new TableView<>();
        vendasObservable = FXCollections.observableArrayList(listaVendas);
        tabelaHistorico.setItems(vendasObservable);
        tabelaHistorico.setPrefHeight(400);
        tabelaHistorico.setStyle("-fx-font-size: 13px;");

        TableColumn<Venda, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNomeProduto()));
        colProduto.setPrefWidth(200);

        TableColumn<Venda, Integer> colQuantidade = new TableColumn<>("Qtd");
        colQuantidade.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getQuantidade()).asObject());
        colQuantidade.setPrefWidth(80);
        colQuantidade.setStyle("-fx-alignment: CENTER;");

        TableColumn<Venda, Double> colValorUnit = new TableColumn<>("Valor Unit.");
        colValorUnit.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getValorUnitario()).asObject());
        colValorUnit.setPrefWidth(120);
        colValorUnit.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Venda, Double> colValorTotal = new TableColumn<>("Valor Total");
        colValorTotal.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getValorTotal()).asObject());
        colValorTotal.setPrefWidth(120);
        colValorTotal.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Venda, String> colData = new TableColumn<>("Data/Hora");
        colData.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDataFormatada()));
        colData.setPrefWidth(150);
        colData.setStyle("-fx-alignment: CENTER;");

        tabelaHistorico.getColumns().addAll(colProduto, colQuantidade, colValorUnit, colValorTotal, colData);
        tabelaContainer.getChildren().addAll(lblTabela, tabelaHistorico);

        layout.getChildren().addAll(controleContainer, tabelaContainer);
        tab.setContent(layout);

        return tab;
    }

    // ===== COMPONENTES VISUAIS PREMIUM =====

    private VBox criarCardPremium(String titulo, String valor, String cor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25, 20, 25, 20));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, " + cor + ", " + cor + "80);" +
                "-fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");
        card.setPrefSize(250, 120);
        card.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setTextFill(Color.WHITE);
        lblTitulo.setFont(Font.font("Segoe UI", 14));

        Label lblValor = new Label(valor);
        lblValor.setTextFill(Color.WHITE);
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        card.getChildren().addAll(lblTitulo, lblValor);
        
        card.setOnMouseEntered(e -> 
            card.setStyle("-fx-background-color: linear-gradient(to bottom right, " + cor + ", " + cor + ");" +
                "-fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 8);"));
        card.setOnMouseExited(e -> 
            card.setStyle("-fx-background-color: linear-gradient(to bottom right, " + cor + ", " + cor + "80);" +
                "-fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);"));
        
        return card;
    }

    private Button criarBotaoPremium(String texto, String cor) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-background-color: " + cor + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12 25; -fx-background-radius: 25; -fx-font-size: 13px; " +
                "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        btn.setOnMouseEntered(e -> 
            btn.setStyle("-fx-background-color: derive(" + cor + ", -20%); -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 25; " +
                "-fx-font-size: 13px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);"));
        
        btn.setOnMouseExited(e -> 
            btn.setStyle("-fx-background-color: " + cor + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12 25; -fx-background-radius: 25; -fx-font-size: 13px; " +
                "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"));
        
        return btn;
    }

    private String criarEstiloCampo() {
        return "-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; " +
                "-fx-border-color: " + CINZA_MEDIO + "; -fx-border-width: 1;";
    }

    // ===== LÓGICA DE NEGÓCIO =====

    private void inicializarDados() {
        deposito.adicionarProduto(new Produto("Café do Brasil Gourmet", 100, 19.90, true));
        deposito.adicionarProduto(new Produto("Queijo Canastra Artesanal", 50, 45.90, true));
        deposito.adicionarProduto(new Produto("Cachaça Mineira Envelhecida", 30, 89.90, true));
        deposito.adicionarProduto(new Produto("Açaí Premium com Guaraná", 80, 25.50, true));
        deposito.adicionarProduto(new Produto("Doce de Leite Viçosa", 120, 15.90, true));
        deposito.adicionarProduto(new Produto("Arroz Agroecológico", 200, 8.90, true));
        deposito.adicionarProduto(new Produto("Feijão Tropeiro Mineiro", 150, 12.90, true));
        deposito.adicionarProduto(new Produto("Farinha de Mandioca", 90, 6.90, true));
        deposito.adicionarProduto(new Produto("Mel Orgânico do Sul", 40, 35.90, true));
        deposito.adicionarProduto(new Produto("Castanha do Pará", 60, 42.90, true));
    }

    private void configurarFiltro() {
        FilteredList<Produto> filteredData = new FilteredList<>(produtosObservable, p -> true);
        txtFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(produto -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return produto.getNome().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Produto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private void atualizarComboProdutos(ComboBox<Produto> combo) {
        ObservableList<Produto> produtosDisponiveis = FXCollections.observableArrayList(
            deposito.getProdutos().stream()
                .filter(Produto::isDisponivel)
                .toList()
        );
        produtosDisponiveis.sort(Comparator.comparing(Produto::getNome));
        combo.setItems(produtosDisponiveis);
    }

    private void adicionarProduto(TextField txtNome, TextField txtQuantidade, TextField txtValor) {
        String nome = txtNome.getText().trim();
        String qtd = txtQuantidade.getText().trim();
        String valor = txtValor.getText().trim();

        if (nome.isEmpty() || qtd.isEmpty() || valor.isEmpty()) {
            mostrarAlerta("Atenção", "Preencha todos os campos!");
            return;
        }

        try {
            int quantidade = Integer.parseInt(qtd);
            double valorDouble = Double.parseDouble(valor);

            if (quantidade <= 0 || valorDouble <= 0) {
                mostrarAlerta("Erro", "Valores devem ser maiores que zero!");
                return;
            }

            // Verificar se já existe produto com mesmo nome
            if (buscarProdutoPorNome(nome) != null) {
                mostrarAlerta("Erro", "Já existe um produto com este nome!");
                return;
            }

            Produto novoProduto = new Produto(nome, quantidade, valorDouble, true);
            deposito.adicionarProduto(novoProduto);
            produtosObservable.add(novoProduto);

            if (areaRelatorio != null) {
                areaRelatorio.appendText("✅ Produto adicionado: " + nome + " (Qtd: " + quantidade + ")\n");
            }
            
            atualizarComboSelecaoProdutos();
            limparFormularioProduto();
            atualizarDashboard();
            mostrarAlerta("Sucesso", "Produto cadastrado com sucesso!");

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Quantidade e valor devem ser números válidos!");
        }
    }

    private void realizarVenda(Produto produto, String qtdStr) {
        try {
            int quantidade = Integer.parseInt(qtdStr);
            if (quantidade <= 0) {
                mostrarAlerta("Erro", "Quantidade deve ser maior que zero!");
                return;
            }

            if (produto != null) {
                if (produto.getQuantidade() >= quantidade) {
                    deposito.venderProduto(produto, quantidade);
                    
                    Venda venda = new Venda(produto.getNome(), quantidade, produto.getValor());
                    listaVendas.add(venda);
                    
                    tableView.refresh();
                    
                    String mensagemLog = String.format("💰 VENDA REALIZADA: %s x%d | Total: R$ %,.2f | Estoque: %d | %s\n",
                            produto.getNome(), quantidade, quantidade * produto.getValor(), 
                            produto.getQuantidade(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    
                    textAreaLog.appendText(mensagemLog);
                    
                    atualizarDashboard();
                    mostrarAlerta("Sucesso", String.format("Venda realizada!\n\nProduto: %s\nQuantidade: %d\nTotal: R$ %,.2f",
                            produto.getNome(), quantidade, quantidade * produto.getValor()));
                } else {
                    mostrarAlerta("Estoque insuficiente",
                            String.format("Estoque disponível: %d\nQuantidade solicitada: %d",
                                    produto.getQuantidade(), quantidade));
                }
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Quantidade deve ser um número válido!");
        }
    }

    // ===== RELATÓRIOS =====

    private void gerarRelatorioCompleto() {
        areaRelatorio.clear();
        areaRelatorio.appendText("=".repeat(80) + "\n");
        areaRelatorio.appendText("📊 RELATÓRIO COMPLETO DE ESTOQUE\n");
        areaRelatorio.appendText("=".repeat(80) + "\n");
        areaRelatorio.appendText("Data: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n");
        areaRelatorio.appendText("Hora: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "\n");
        areaRelatorio.appendText("-".repeat(80) + "\n\n");
        
        areaRelatorio.appendText("📦 RESUMO GERAL:\n");
        areaRelatorio.appendText("Total de produtos: " + deposito.getProdutos().size() + "\n");
        areaRelatorio.appendText("Valor total em estoque: R$ " + String.format("%,.2f", calcularValorTotalEstoque()) + "\n");
        areaRelatorio.appendText("Vendas hoje: " + contarVendasHoje() + 
                " (R$ " + String.format("%,.2f", calcularTotalVendasHoje()) + ")\n");
        areaRelatorio.appendText("Produtos com baixo estoque: " + contarProdutosBaixoEstoque() + "\n\n");

        areaRelatorio.appendText("📋 LISTA DE PRODUTOS:\n");
        areaRelatorio.appendText(String.format("%-30s %10s %15s %15s %12s\n",
                "PRODUTO", "QTD", "VALOR UNIT.", "VALOR TOTAL", "STATUS"));
        areaRelatorio.appendText("-".repeat(82) + "\n");

        for (Produto p : deposito.getProdutos()) {
            double valorTotal = p.getQuantidade() * p.getValor();
            areaRelatorio.appendText(String.format("%-30s %10d %15.2f %15.2f %12s\n",
                    p.getNome(), p.getQuantidade(), p.getValor(), valorTotal,
                    p.isDisponivel() ? "DISPONÍVEL" : "ESGOTADO"));
        }
    }

    private void gerarRelatorioDisponiveis() {
        areaRelatorio.clear();
        areaRelatorio.appendText("=".repeat(60) + "\n");
        areaRelatorio.appendText("✅ PRODUTOS DISPONÍVEIS\n");
        areaRelatorio.appendText("=".repeat(60) + "\n\n");

        int contador = 0;
        for (Produto p : deposito.getProdutos()) {
            if (p.isDisponivel()) {
                contador++;
                areaRelatorio.appendText(String.format("%-30s | Quantidade: %4d | Valor: R$ %8.2f\n",
                        p.getNome(), p.getQuantidade(), p.getValor()));
            }
        }

        areaRelatorio.appendText("\n" + "-".repeat(60) + "\n");
        areaRelatorio.appendText("Total de produtos disponíveis: " + contador + "\n");
    }

    private void gerarRelatorioBaixoEstoque() {
        areaRelatorio.clear();
        areaRelatorio.appendText("=".repeat(60) + "\n");
        areaRelatorio.appendText("⚠️ PRODUTOS COM BAIXO ESTOQUE (menos de 20 unidades)\n");
        areaRelatorio.appendText("=".repeat(60) + "\n\n");

        int contador = 0;
        for (Produto p : deposito.getProdutos()) {
            if (p.getQuantidade() < 20 && p.getQuantidade() > 0) {
                contador++;
                areaRelatorio.appendText(String.format("%-30s | Quantidade: %4d | Valor: R$ %8.2f\n",
                        p.getNome(), p.getQuantidade(), p.getValor()));
            }
        }

        if (contador == 0) {
            areaRelatorio.appendText("Todos os produtos estão com estoque adequado.\n");
        } else {
            areaRelatorio.appendText("\n" + "-".repeat(60) + "\n");
            areaRelatorio.appendText("Total de produtos com baixo estoque: " + contador + "\n");
        }
    }

    private void gerarRelatorioVendas() {
        areaRelatorio.clear();
        areaRelatorio.appendText("=".repeat(70) + "\n");
        areaRelatorio.appendText("💰 RELATÓRIO DE VENDAS\n");
        areaRelatorio.appendText("=".repeat(70) + "\n\n");
        
        double totalGeral = listaVendas.stream().mapToDouble(Venda::getValorTotal).sum();
        
        areaRelatorio.appendText("📊 ESTATÍSTICAS GERAIS:\n");
        areaRelatorio.appendText("Total de vendas: " + listaVendas.size() + "\n");
        areaRelatorio.appendText("Valor total vendido: R$ " + String.format("%,.2f", totalGeral) + "\n");
        areaRelatorio.appendText("Média por venda: R$ " + 
                String.format("%,.2f", listaVendas.isEmpty() ? 0 : totalGeral / listaVendas.size()) + "\n");
        areaRelatorio.appendText("Vendas hoje: " + contarVendasHoje() + 
                " (R$ " + String.format("%,.2f", calcularTotalVendasHoje()) + ")\n\n");

        if (!listaVendas.isEmpty()) {
            areaRelatorio.appendText("📋 ÚLTIMAS 20 VENDAS:\n");
            areaRelatorio.appendText(String.format("%-25s %8s %12s %12s %15s\n",
                    "PRODUTO", "QTD", "VALOR UNIT.", "TOTAL", "DATA/HORA"));
            areaRelatorio.appendText("-".repeat(72) + "\n");

            int count = 0;
            for (int i = listaVendas.size() - 1; i >= 0 && count < 20; i--) {
                Venda v = listaVendas.get(i);
                areaRelatorio.appendText(String.format("%-25s %8d %12.2f %12.2f %15s\n",
                        v.getNomeProduto(), v.getQuantidade(), v.getValorUnitario(),
                        v.getValorTotal(), v.getDataFormatada()));
                count++;
            }
        }
    }

    // ===== UTILITÁRIOS =====

    private double calcularValorTotalEstoque() {
        return deposito.getProdutos().stream()
                .mapToDouble(p -> p.getQuantidade() * p.getValor())
                .sum();
    }

    private int contarProdutosBaixoEstoque() {
        return (int) deposito.getProdutos().stream()
                .filter(p -> p.getQuantidade() < 20 && p.getQuantidade() > 0)
                .count();
    }

    private int contarVendasHoje() {
        LocalDate hoje = LocalDate.now();
        return (int) listaVendas.stream()
                .filter(v -> v.getDataHora().toLocalDate().equals(hoje))
                .count();
    }

    private double calcularTotalVendasHoje() {
        LocalDate hoje = LocalDate.now();
        return listaVendas.stream()
                .filter(v -> v.getDataHora().toLocalDate().equals(hoje))
                .mapToDouble(Venda::getValorTotal)
                .sum();
    }

    private void atualizarCabecalho() {
        if (lblEstoqueCabecalho != null) {
            lblEstoqueCabecalho.setText("📦 " + deposito.getProdutos().size() + " produtos | " +
                    "💰 R$ " + String.format("%,.2f", calcularValorTotalEstoque()) + " em estoque | " +
                    "💸 Hoje: R$ " + String.format("%,.2f", calcularTotalVendasHoje()));
        }
    }

    private void atualizarDashboard() {
        atualizarCabecalho();
        atualizarListViewDashboard();
        if (tableView != null) {
            tableView.refresh();
        }
        if (comboSelecionarProduto != null) {
            atualizarComboSelecaoProdutos();
        }
    }

    private void atualizarListViewDashboard() {
        if (listViewDashboard != null) {
            listViewDashboard.getItems().clear();
            for (Produto p : deposito.getProdutos().stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getQuantidade(), p1.getQuantidade()))
                    .limit(10)
                    .toList()) {
                listViewDashboard.getItems().add(
                    String.format("%s • %d und • R$ %,.2f", 
                        p.getNome(), p.getQuantidade(), p.getValor()));
            }
        }
    }

    private void atualizarResumoHistorico(Label label) {
        double totalVendas = listaVendas.stream().mapToDouble(Venda::getValorTotal).sum();
        label.setText(String.format("📊 Histórico de Vendas - Total: R$ %,.2f | Quantidade: %d vendas",
                totalVendas, listaVendas.size()));
    }

    private Produto buscarProdutoPorNome(String nome) {
        return deposito.getProdutos().stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome.trim()))
                .findFirst()
                .orElse(null);
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    // ===== EXPORTAÇÃO =====

    private void exportarParaCSV() {
        String nomeArquivo = "estoque_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            writer.println("Produto;Quantidade;Valor Unitário;Valor Total;Status");
            for (Produto p : deposito.getProdutos()) {
                writer.printf("%s;%d;%.2f;%.2f;%s%n",
                        p.getNome(), p.getQuantidade(), p.getValor(),
                        p.getQuantidade() * p.getValor(),
                        p.isDisponivel() ? "Disponível" : "Esgotado");
            }
            mostrarAlerta("Sucesso", "Arquivo exportado: " + nomeArquivo);
            if (areaRelatorio != null) {
                areaRelatorio.appendText("📤 Arquivo exportado: " + nomeArquivo + "\n");
            }
        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível exportar: " + e.getMessage());
        }
    }

    private void exportarHistoricoCSV() {
        String nomeArquivo = "vendas_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            writer.println("Produto;Quantidade;Valor Unitário;Valor Total;Data;Hora");
            for (Venda v : listaVendas) {
                writer.printf("%s;%d;%.2f;%.2f;%s;%s%n",
                        v.getNomeProduto(), v.getQuantidade(), v.getValorUnitario(),
                        v.getValorTotal(), 
                        v.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        v.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
            mostrarAlerta("Sucesso", "Histórico exportado: " + nomeArquivo);
        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível exportar: " + e.getMessage());
        }
    }

    private void exportarRelatorioParaArquivo() {
        String nomeArquivo = "relatorio_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            writer.println("RELATÓRIO COMPLETO - MERCATTO BRASIL");
            writer.println("=".repeat(60));
            writer.println("Data: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            writer.println();
            
            writer.println("📦 RESUMO DO ESTOQUE:");
            writer.println("-".repeat(30));
            writer.println("Total de produtos: " + deposito.getProdutos().size());
            writer.printf("Valor total em estoque: R$ %,.2f%n", calcularValorTotalEstoque());
            writer.println("Produtos com baixo estoque: " + contarProdutosBaixoEstoque());
            writer.println();
            
            writer.println("💰 RESUMO DE VENDAS:");
            writer.println("-".repeat(30));
            writer.println("Total de vendas: " + listaVendas.size());
            writer.printf("Valor total vendido: R$ %,.2f%n",
                    listaVendas.stream().mapToDouble(Venda::getValorTotal).sum());
            writer.println("Vendas hoje: " + contarVendasHoje() +
                    " (R$ " + String.format("%,.2f", calcularTotalVendasHoje()) + ")");
            writer.println();
            
            writer.println("📋 PRODUTOS EM ESTOQUE:");
            writer.println("-".repeat(60));
            for (Produto p : deposito.getProdutos()) {
                writer.printf("%-25s | Quantidade: %4d | Valor: R$ %8.2f | %s%n",
                        p.getNome(), p.getQuantidade(), p.getValor(),
                        p.isDisponivel() ? "DISPONÍVEL" : "ESGOTADO");
            }

            mostrarAlerta("Sucesso", "Relatório exportado: " + nomeArquivo);
            if (areaRelatorio != null) {
                areaRelatorio.appendText("📤 Relatório exportado: " + nomeArquivo + "\n");
            }
        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao exportar: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}