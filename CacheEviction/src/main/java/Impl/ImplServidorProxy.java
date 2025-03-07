package Impl;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Banco.Banco;
import Banco.No;
import Cache.Cache;
import OrdemServico.ServiceOrder;
import Cache.NoCache;
import Exception.MyPickException;

public class ImplServidorProxy implements Runnable {
    private Socket socketCliente;
    private Socket socketAplicacao;
    private static int cont = 0;
    private boolean conexao = true;
    private BufferedReader entrada;
    private PrintWriter saida;
    private ObjectInputStream entradaAplicacao;
    private ObjectOutputStream saidaAplicacao;
    private Cache<Integer> cache;
    private Banco banco;

    public ImplServidorProxy(Socket cliente, Socket aplicacao) {
        this.socketCliente = cliente;
        this.socketAplicacao = aplicacao;
        this.cache = Cache.instancia;
        this.banco = Banco.instancia;
        cont++;
    }

    public void run() {
        System.out.println("Conexão " + cont + " com o cliente " + socketCliente.getInetAddress().getHostAddress());

        try {
            entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            saida = new PrintWriter(socketCliente.getOutputStream(), true);
            saidaAplicacao = new ObjectOutputStream(socketAplicacao.getOutputStream());
            entradaAplicacao = new ObjectInputStream(socketAplicacao.getInputStream());

            if (autenticar()) {
                saida.println("Autenticação bem-sucedida!");
                enviarMenu();

                while (conexao) {
                    String mensagemRecebida = entrada.readLine();
                    if (mensagemRecebida == null || mensagemRecebida.equalsIgnoreCase("7")) {
                        conexao = false;
                    } else {
                        processarEscolha(mensagemRecebida);
                    }
                }
            } else {
                saida.println("Autenticação falhou. Conexão encerrada.");
            }

            fecharConexoes();

        } catch (IOException e) {
            System.err.println("Erro na conexão com o cliente: " + e.getMessage());
        }
    }

    private void processarEscolha(String opcao) {
        try {
            List<Object> lista = new ArrayList<>();
            switch (opcao) {
                case "1":
                    cadastrarOS(lista);
                    break;
                case "2":
                    listarCache();
                    break;
                case "3":
                    listarTodasOS();
                    break;
                case "4":
                    alterarOS(lista);
                    break;
                case "5":
                    removerOS(lista);
                    break;
                case "6":
                    buscarOS(lista);
                    break;
                default:
                    saida.println("Opção inválida. Tente novamente.");
                    break;
            }
        } catch (IOException | ClassNotFoundException | MyPickException e) {
            saida.println("Erro ao processar solicitação: " + e.getMessage());
        }
        enviarMenu();
    }

    private void cadastrarOS(List<Object> lista) throws IOException, ClassNotFoundException {
        saida.println("Digite o nome:");
        String nome = entrada.readLine();
        saida.println("Digite a descrição:");
        String descricao = entrada.readLine();

        ServiceOrder so = new ServiceOrder(nome, descricao);
        No no = new No(so);
        lista.add("cadastro");
        lista.add(no);
        saidaAplicacao.writeObject(lista);
        saidaAplicacao.flush();

        // Aguarda a confirmação do servidor de aplicação
        String resposta = (String) entradaAplicacao.readObject();
        saida.println(resposta); // Exibe a mensagem de sucesso
    }

    private void listarCache() {
        saida.println("===== LISTA DE OS NA CACHE =====");
        HashMap<Integer, NoCache<Integer, ServiceOrder>> mapaCache = cache.getMapa();
        if (mapaCache.isEmpty()) {
            saida.println("Nenhuma OS na cache.");
        } else {
            for (NoCache<Integer, ServiceOrder> no : mapaCache.values()) {
                ServiceOrder so = no.getValor();
                saida.println("ID: " + so.getCodigoServico() + " | Nome: " + so.getNome() + " | Descrição: " + so.getDescricao());
            }
        }
    }

    private void listarTodasOS() throws IOException, ClassNotFoundException {
        saidaAplicacao.writeObject(List.of("listar")); // Envia o comando "listar" para o servidor de aplicação
        saidaAplicacao.flush();

        // Recebe a lista de OS do servidor de aplicação
        Object resposta = entradaAplicacao.readObject();
        if (resposta instanceof List) {
            List<String> listaOS = (List<String>) resposta;
            if (listaOS.isEmpty()){
                saida.println("===== LISTA DE TODAS AS OS =====");
                saida.println("Nenhuma OS no banco.");
            } else {
                saida.println("===== LISTA DE TODAS AS OS =====");
                for (String os : listaOS) {
                    saida.println(os); // Exibe cada OS
                }
            }
        } else {
            saida.println("Erro: Resposta inválida do servidor de aplicação.");
        }
    }

    private void alterarOS(List<Object> lista) throws IOException, ClassNotFoundException {
        saida.println("Digite o ID da OS:");
        int id = Integer.parseInt(entrada.readLine());
        saida.println("Digite o novo nome:");
        String nome = entrada.readLine();
        saida.println("Digite a nova descrição:");
        String descricao = entrada.readLine();

        lista.add("alterar");
        lista.add(id);
        lista.add(nome);
        lista.add(descricao);
        saidaAplicacao.writeObject(lista);
        saidaAplicacao.flush();

        // Aguarda a confirmação do servidor de aplicação
        String resposta = (String) entradaAplicacao.readObject();
        cache.atualizar(id, nome, descricao);
        saida.println(resposta); // Exibe a mensagem de sucesso
    }

    private void removerOS(List<Object> lista) throws IOException, ClassNotFoundException {
        saida.println("Digite o ID para remover:");
        int id = Integer.parseInt(entrada.readLine());
        cache.remover(id);
        lista.add("remover");
        lista.add(id);
        saidaAplicacao.writeObject(lista);
        saidaAplicacao.flush();

        // Aguarda a confirmação do servidor de aplicação
        String resposta = (String) entradaAplicacao.readObject();
        saida.println(resposta); // Exibe a mensagem de sucesso
    }

    private void buscarOS(List<Object> lista) throws IOException, ClassNotFoundException, MyPickException {
        saida.println("Digite o ID para buscar:");
        int id = Integer.parseInt(entrada.readLine());

        // Busca na cache
        ServiceOrder so = cache.get(id);
        if (so == null) {
            // Se não estiver na cache, busca no banco
            lista.add("buscar");
            lista.add(id);
            saidaAplicacao.writeObject(lista);
            saidaAplicacao.flush();

            // Recebe o objeto do servidor de aplicação
            Object objetoRecebido = entradaAplicacao.readObject();
            if (objetoRecebido instanceof No) {
                No no = (No) objetoRecebido;
                if (no != null) {
                    so = no.getOrder();
                    cache.put(id, so); // Armazena na cache
                    saida.println("OS encontrada: " + so);
                } else {
                    saida.println("OS não encontrada.");
                }
            } else {
                saida.println("Erro: Objeto recebido não é do tipo Banco.No.");
            }
        } else {
            saida.println("OS encontrada na cache: " + so);
        }
    }

    private void enviarMenu() {
        saida.println("===== MENU =====\n1. Cadastrar OS\n2. Listar Cache\n3. Listar todas as OS\n4. Alterar OS\n5. Remover OS\n6. Buscar OS\n7. Sair\nEscolha uma opção:");
    }

    private boolean autenticar() throws IOException {
        saida.println("Bem-vindo ao servidor proxy! Digite seu login:");
        String login = entrada.readLine();
        saida.println("Digite sua senha:");
        String senha = entrada.readLine();
        return "admin".equals(login) && "admin".equals(senha);
    }

    private void fecharConexoes() throws IOException {
        entrada.close();
        saida.close();
        entradaAplicacao.close();
        saidaAplicacao.close();
        socketCliente.close();
        socketAplicacao.close();
    }
}
