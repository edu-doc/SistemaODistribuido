package Impl;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import Banco.Banco;
import Banco.No;
import Cache.Cache;
import OrdemServico.ServiceOrder;
import Cache.NoCache;
import Exception.MyPickException;

public class ImplServidorProxy implements Runnable {
    public Socket socketCliente;
    public Socket socketAplicacao;
    public static int cont = 0;
    private boolean conexao = true;
    private BufferedReader entrada;
    private PrintWriter saida;
    private ObjectInputStream entradaAplicacao;
    private ObjectOutputStream  saidaAplicacao;
    private Cache<Integer> cache; // Cache compartilhada
    private Banco banco; // Banco compartilhado

    public ImplServidorProxy(Socket cliente, Socket aplicacao) {
        this.socketCliente = cliente;
        this.socketAplicacao = aplicacao;
        this.cache = Cache.instancia; // Obtém a instância única da Cache
        this.banco = Banco.instancia; // Obtém a instância única do Banco
        cont++;  // Incrementa o contador de conexões
    }

    public void run() {
        String mensagemRecebida;
        No noAplicacao;
        String mensagemAplicacao = null;

        System.out.println("Conexão " +
                cont +
                " com o cliente " +
                socketCliente.getInetAddress().getHostAddress() +
                "/" +
                socketCliente.getInetAddress().getHostName()
        );

        try {

            // Prepara para leitura das mensagens do cliente
            entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            saida = new PrintWriter(socketCliente.getOutputStream(), true);

            // Realiza a autenticação
            if (autenticar()) {
                saida.println("Autenticação bem-sucedida!");
                enviarMenu();

                // Processa as escolhas do cliente
                while (conexao) {
                    mensagemRecebida = entrada.readLine();

                    if (mensagemRecebida == null || mensagemRecebida.equalsIgnoreCase("fim")) {
                        conexao = false;
                    } else {
                        processarEscolha(mensagemRecebida, mensagemAplicacao);
                    }
                }
            } else {
                saida.println("Autenticação falhou. Conexão encerrada.");
            }

            // Finaliza os recursos
            entradaAplicacao.close();
            saidaAplicacao.close();
            entrada.close();
            saida.close();
            System.out.println("Fim do cliente " +
                    socketCliente.getInetAddress().getHostAddress());

            socketAplicacao.close();
            socketCliente.close();

        } catch (IOException e) {
            System.err.println("Erro na conexão com o cliente: " + e.getMessage());
        }
    }

    private void enviarMenu() {
        saida.println();
        saida.println("===== MENU =====");
        saida.println("1. Cadastrar OS");
        saida.println("2. Listar a Cache");
        saida.println("3. Listar todas as OS");
        saida.println("4. Alterar OS");
        saida.println("5. Remover OS");
        saida.println("6. Buscar OS");
        saida.println("7. Sair");
        saida.println("Escolha uma opção:");
    }

    private boolean autenticar() {
        try {
            saida.println("Bem-vindo ao servidor de proxy!");
            saida.println("Digite seu login: ");
            String login = entrada.readLine();
            saida.println("Digite sua senha: ");
            String senha = entrada.readLine();

            // Comparação correta de strings usando .equals()
            if ("admin".equals(login) && "admin".equals(senha)) {
                return true; // Autenticação bem-sucedida
            } else {
                saida.println("Login ou senha incorretos.");
                return false; // Autenticação falhou
            }
        } catch (IOException e) {
            System.err.println("Erro durante a autenticação: " + e.getMessage());
            return false;
        }
    }

    private void processarEscolha(String mensagemRecebida , String mensagemAplicacao) {
        switch (mensagemRecebida) {
            case "1":
                cadastrarOS();
                break;
            case "2":
                listarCache();
                break;
            case "3":
                listarTodasOS();
                break;
            case "4":
                alterarOS();
                break;
            case "5":
                removerOS();
                break;
            case "6":
                buscarOS();
                break;
            case "7":
                saida.println("Fim da conexão");
                conexao = false; // Encerra o loop de processamento
                break;
            default:
                saida.println("Opção inválida. Tente novamente.");
                break;
        }
    }

    private void cadastrarOS() {
        try {
            saida.println("Digite o nome: ");
            String nome = entrada.readLine();
            saida.println("Digite a descrição do problema: ");
            String descricao = entrada.readLine();

            ServiceOrder so = new ServiceOrder(nome, descricao);
            No no = new No(so);

            if(socketAplicacao.isConnected()){
                System.out.println("Ainda conectado " + socketCliente.getInetAddress().getHostAddress());
            }


            entradaAplicacao = new ObjectInputStream(socketAplicacao.getInputStream());
            saidaAplicacao = new ObjectOutputStream(socketAplicacao.getOutputStream());

            saidaAplicacao.writeObject("Cadastro");
            saidaAplicacao.flush();

            saidaAplicacao.writeObject(no);
            saidaAplicacao.flush();

            // saida.println("OS cadastrada com sucesso! ID: " + no.getOrder().getCodigoServico());
        } catch (IOException e) {
            saida.println("Erro ao cadastrar OS: " + e.getMessage());
        }
        enviarMenu();
    }

    private void listarCache() {
        try {
            saida.println("===== LISTA DE OS NA CACHE =====");
            HashMap<Integer, NoCache<Integer, ServiceOrder>> mapaCache = cache.getMapa(); // Obtém o mapa da cache

            if (mapaCache.isEmpty()) {
                saida.println("Nenhuma OS na cache.");
            } else {
                for (NoCache<Integer, ServiceOrder> no : mapaCache.values()) {
                    ServiceOrder so = no.getValor();
                    saida.println("ID: " + so.getCodigoServico() +
                            ", Nome: " + so.getNome() +
                            ", Descrição: " + so.getDescricao());
                }
            }
        } catch (Exception e) {
            saida.println("Erro ao listar cache: " + e.getMessage());
        }
        enviarMenu();
    }

    private void listarTodasOS() {
        try {
            saida.println("===== LISTA DE OS =====");
            List<String> listaOS = banco.listarElementos(); // Obtém a lista de OS

            if (listaOS.isEmpty()) {
                saida.println("Nenhuma OS cadastrada.");
            } else {
                for (String os : listaOS) {
                    saida.println(os); // Envia cada OS ao cliente
                }
            }
        } catch (Exception e) {
            saida.println("Erro ao listar OS: " + e.getMessage());
        }
        enviarMenu();
    }

    private void alterarOS() {
        try {
            saida.println("Digite o ID da OS que deseja alterar: ");
            int id = Integer.parseInt(entrada.readLine());
            saida.println("Digite o novo nome: ");
            String nome = entrada.readLine();
            saida.println("Digite a nova descrição: ");
            String descricao = entrada.readLine();

            // Atualiza no banco
            boolean atualizado = banco.atualizar(id, nome, descricao);
            if (atualizado) {
                // Atualiza na cache
                ServiceOrder so = cache.get(id, banco);
                if (so != null) {
                    so.setNome(nome);
                    so.setDescricao(descricao);
                }
                saida.println("OS atualizada com sucesso!");
            } else {
                saida.println("OS não encontrada para atualização.");
            }
        } catch (IOException | MyPickException e) {
            saida.println("Erro ao alterar OS: " + e.getMessage());
        }
        enviarMenu();
    }

    private void removerOS() {
        try {
            saida.println("Digite o ID da OS que deseja remover: ");
            int id = Integer.parseInt(entrada.readLine());

            // Remove do banco
            No noRemovido = banco.remover(id);
            if (noRemovido != null) {
                // Remove da cache
                cache.remover(id);
                saida.println("OS removida com sucesso!");
            } else {
                saida.println("OS não encontrada para remoção.");
            }
        } catch (IOException e) {
            saida.println("Erro ao remover OS: " + e.getMessage());
        }
        enviarMenu();
    }

    private void buscarOS() {
        try {
            saida.println("Digite o ID da OS que deseja buscar: ");
            int id = Integer.parseInt(entrada.readLine());

            // Busca na cache ou no banco
            ServiceOrder so = cache.get(id, banco);
            if (so != null) {
                saida.println("OS encontrada: " +
                        "ID: " + id +
                        ", Nome: " + so.getNome() +
                        ", Descrição: " + so.getDescricao());
            } else {
                saida.println("OS não encontrada.");
            }
        } catch (IOException | MyPickException e) {
            saida.println("Erro ao buscar OS: " + e.getMessage());
        }
        enviarMenu();
    }
}