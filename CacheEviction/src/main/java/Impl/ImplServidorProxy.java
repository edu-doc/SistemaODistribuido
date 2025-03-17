package Impl;

import Banco.Banco;
import Banco.No;
import Cache.Cache;
import Cache.NoCache;
import Exception.MyPickException;
import Logger.Logger;
import OrdemServico.ServiceOrder;
import RMI.ProxyRemoteInterface;

import java.io.*;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImplServidorProxy implements Runnable, ProxyRemoteInterface {
    private Socket socketCliente;
    private Socket socketAplicacao;
    private Socket socketBackup;
    private static int cont = 0;
    private boolean conexao = true;
    private BufferedReader entrada;
    private PrintWriter saida;
    private ObjectInputStream entradaAplicacao;
    private ObjectOutputStream saidaAplicacao;
    private Cache<Integer> cache;
    private Banco banco;
    private String nomeProxy;

    public ImplServidorProxy(Socket cliente, Socket aplicacao,Socket backup, String nomeProxy) {
        this.socketCliente = cliente;
        this.socketAplicacao = aplicacao;
        this.cache = Cache.instancia;
        this.banco = Banco.instancia;
        this.nomeProxy = nomeProxy;
        this.socketBackup = backup;
        cont++;
        Logger.info("Nova conexão estabelecida. Total de conexões: " + cont);

        // Registra o proxy no RMI Registry
        try {
            ProxyRemoteInterface stub = (ProxyRemoteInterface) UnicastRemoteObject.exportObject(this, 0);
            LocateRegistry.createRegistry(995);
            Registry registry = LocateRegistry.getRegistry("localhost", 995);
            registry.bind(nomeProxy, stub);
            System.out.println("proxy 1 registrado no rmi");
            Logger.info("Proxy registrado no RMI Registry como: " + nomeProxy);
        } catch (RemoteException | AlreadyBoundException e) {
            Logger.error("Erro ao registrar proxy no RMI Registry: " + e.getMessage(), e);
        }

    }

    @Override
    public void run() {
        System.out.println("Conexão " + cont + " com o cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress());
        Logger.info("Iniciando comunicação com o cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress());
        try {
            entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            saida = new PrintWriter(socketCliente.getOutputStream(), true);
            saidaAplicacao = new ObjectOutputStream(socketAplicacao.getOutputStream());
            entradaAplicacao = new ObjectInputStream(socketAplicacao.getInputStream());

            if (autenticar()) {
                Logger.info("Autenticação bem-sucedida para o cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress());
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
                Logger.warning("Autenticação falhou para o cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress());
                saida.println("Autenticação falhou. Conexão encerrada.");
            }

            fecharConexoes();

        } catch (IOException e) {
            Logger.error("Erro na comunicação com o cliente " + cont + ": " + e.getMessage(), e);
        } finally {
            try {
                fecharConexoes();
            } catch (IOException e) {
                Logger.error("Erro ao fechar conexões: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public ServiceOrder buscarEmOutroProxy(int id) throws RemoteException {
        try {
            return cache.get(id); // Busca a OS na cache local
        } catch (MyPickException e) {
            throw new RemoteException("Erro ao buscar OS no proxy remoto: " + e.getMessage());
        }
    }

    private void processarEscolha(String opcao) {
        try {
            List<Object> lista = new ArrayList<>();
            switch (opcao) {
                case "1":
                    Logger.info("Cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress() + " solicitou cadastro de OS.");
                    cadastrarOS(lista);
                    break;
                case "2":
                    Logger.info("Cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress() + " solicitou listagem da cache.");
                    listarCache();
                    break;
                case "3":
                    Logger.info("Cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress() + " solicitou listagem de todas as OSs.");
                    listarTodasOS();
                    break;
                case "4":
                    Logger.info("Cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress() + " solicitou alteração de OS.");
                    alterarOS(lista);
                    break;
                case "5":
                    Logger.info("Cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress() + " solicitou remoção de OS.");
                    removerOS(lista);
                    break;
                case "6":
                    Logger.info("Cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress() + " solicitou busca de OS.");
                    buscarOS(lista);
                    break;
                case "7":
                    Logger.info("Cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress() + " solicitou fechar conexão.");
                    saida.println("Conexão encerrada.");
                    conexao = false;
                    break;
                default:
                    saida.println("Opção inválida. Tente novamente.");
                    break;
            }
        } catch (IOException | ClassNotFoundException | MyPickException e) {
            Logger.error("Erro ao processar escolha do cliente " + cont + " e IP:" + socketCliente.getInetAddress().getHostAddress() + ": " + e.getMessage(), e);
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
            if (listaOS.isEmpty()) {
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

        // 1. Tenta buscar na cache local
        ServiceOrder so = cache.get(id);
        if (so != null) {
            saida.println("OS encontrada na cache local: " + so);
            return;
        } else {
            // 2. Tenta buscar no outro proxy via RMI
            try {
                Logger.info("Tentando conectar ao RMI Registry...");
                Registry registry = LocateRegistry.getRegistry("localhost", 996);
                Logger.info("RMI Registry encontrado. Procurando por 'Proxy2'...");
                ProxyRemoteInterface outroProxy = (ProxyRemoteInterface) registry.lookup("Proxy2");
                Logger.info("Conexão RMI estabelecida com 'Proxy2'. Buscando OS...");
                so = outroProxy.buscarEmOutroProxy(id);
                if (so != null) {
                    cache.put(id, so); // Armazena na cache local
                    saida.println("OS encontrada na cache de outro proxy2: " + so);
                    return;
                } else {
                    Logger.info("Tentando conectar ao RMI Registry...");
                    Registry registry1 = LocateRegistry.getRegistry("localhost", 997);
                    Logger.info("RMI Registry encontrado. Procurando por 'Proxy3'...");
                    ProxyRemoteInterface outroProxy1 = (ProxyRemoteInterface) registry1.lookup("Proxy3");
                    Logger.info("Conexão RMI estabelecida com 'Proxy3'. Buscando OS...");
                    so = outroProxy1.buscarEmOutroProxy(id);
                    if (so != null) {
                        cache.put(id, so);
                        saida.println("OS encontrada na cache de outro proxy3: " + so);
                        return;
                    }
                    Logger.info("OS não encontrada nas caches.");
                }
            } catch (Exception e) {
                Logger.warning("Erro ao buscar no outro proxy: " + e.getMessage());
                e.printStackTrace(); // Adiciona stack trace para depuração
            }
        }

        // 3. Se ainda não encontrou, busca no banco
        lista.add("buscar");
        lista.add(id);
        saidaAplicacao.writeObject(lista);
        saidaAplicacao.flush();

        Object objetoRecebido = entradaAplicacao.readObject();

        if (objetoRecebido instanceof No) {
            No no = (No) objetoRecebido;
            so = no.getOrder();
            cache.put(id, so); // Armazena na cache local
            saida.println("OS encontrada no banco: " + so);
        } else {
            saida.println("Erro: Resposta inválida do servidor de aplicação.");
        }
    }

    private void enviarMenu() {
        saida.println("");
        saida.println("===== MENU =====\n1. Cadastrar OS\n2. Listar Cache\n3. Listar Banco\n4. Alterar OS\n5. Remover OS\n6. Buscar OS\n7. Sair\nEscolha uma opção:");
    }

    private boolean autenticar() throws IOException {
        Map<String, String> usuarios = new HashMap<>();
        usuarios.put("admin", "admin");
        usuarios.put("eduardo", "eduardo");
        usuarios.put("paulo", "paulo");

        saida.println("Bem-vindo ao servidor proxy! Digite seu login:");
        String login = entrada.readLine();
        saida.println("Digite sua senha:");
        String senha = entrada.readLine();
        return usuarios.containsKey(login) && usuarios.get(login).equals(senha);
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