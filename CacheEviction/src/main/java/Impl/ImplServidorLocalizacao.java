package Impl;

import java.io.*;
import java.net.Socket;
import Logger.Logger;

public class ImplServidorLocalizacao implements Runnable {

    public Socket socketCliente;
    public static int cont = 0;
    private boolean conexao = true;
    private BufferedReader entrada;
    private PrintWriter saida;
    private final Logger log;
    public static int cont1 = 0;
    public static int cont2 = 0;
    public static int cont3 = 0;

    public ImplServidorLocalizacao(Socket cliente) {
        this.socketCliente = cliente;
        this.log = new Logger();
        cont++;  // Incrementa o contador de conexões
    }

    public void run() {
        String mensagemRecebida;

        System.out.println("Conexão " +
                cont +
                " com o cliente " +
                socketCliente.getInetAddress().getHostAddress() +
                "/" +
                socketCliente.getInetAddress().getHostName()
        );

        log.info("Cliente " + socketCliente.getInetAddress().getHostAddress() + " se conectou no servidor de localizacao");

        try {
            // Prepara para leitura das mensagens do cliente
            entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            saida = new PrintWriter(socketCliente.getOutputStream(), true);

            // Envia o menu ao cliente
            enviarMenu();

            // Processa as escolhas do cliente
            while (conexao) {
                mensagemRecebida = entrada.readLine();

                if (mensagemRecebida == null || mensagemRecebida.equalsIgnoreCase("fim")) {
                    conexao = false;
                } else {
                    processarEscolha(mensagemRecebida);
                }
            }

            // Finaliza os recursos
            entrada.close();
            saida.close();
            System.out.println("Fim do cliente " +
                    socketCliente.getInetAddress().getHostAddress());

            socketCliente.close();

        } catch (IOException e) {
            System.err.println("Erro na conexão com o cliente: " + e.getMessage());
        }
    }

    private void enviarMenu() {
        saida.println("Bem-vindo! Escolha uma opção:");
        saida.println("1. Conectar com servidor de proxy");
        saida.println("2. Finalizar conexão");
    }

    private void processarEscolha(String escolha) {
        switch (escolha) {
            case "1":
                saida.println("Conectado");
                log.info("Cliente " + socketCliente.getInetAddress().getHostAddress() + " foi redirecionado para servidor de proxy");
                redirecionarParaProxy();
                break;
            case "2":
                saida.println("Conexão Finalizada");
                conexao = false; // Encerra o loop de processamento
                break;
            default:
                saida.println("Opção inválida. Tente novamente.");
                break;
        }
    }

    private void redirecionarParaProxy() {
        String enderecoProxy = "127.0.0.1"; // Endereço do servidor de proxy
        int portaProxy; // Porta do servidor de proxy

        if (cont1 <= cont2 && cont1 <= cont3){
            portaProxy = 12345;
            System.out.println("Proxy 1");
            cont1++;
        } else if (cont2 <= cont3) {
            portaProxy = 12354;
            System.out.println("Proxy 2");
            cont2++;
        } else {
            portaProxy = 12355;
            System.out.println("Proxy 3");
            cont3++;
        }

        try {
            // Cria um novo socket para se conectar ao proxy
            Socket socketProxy = new Socket(enderecoProxy, portaProxy);

            // Cria streams para comunicação entre cliente e proxy
            BufferedReader proxyEntrada = new BufferedReader(new InputStreamReader(socketProxy.getInputStream()));
            PrintWriter proxySaida = new PrintWriter(socketProxy.getOutputStream(), true);

            BufferedReader clienteEntrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter clienteSaida = new PrintWriter(socketCliente.getOutputStream(), true);

            System.out.println("Cliente redirecionado para o servidor proxy.");

            // Criar threads para comunicação bidirecional
            Thread threadClienteParaProxy = new Thread(() -> {
                try {
                    String mensagem;
                    while ((mensagem = clienteEntrada.readLine()) != null) {
                        proxySaida.println(mensagem);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao encaminhar mensagem do cliente para o proxy: " + e.getMessage());
                }
            });

            Thread threadProxyParaCliente = new Thread(() -> {
                try {
                    String mensagem;
                    while ((mensagem = proxyEntrada.readLine()) != null) {
                        clienteSaida.println(mensagem);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao encaminhar mensagem do proxy para o cliente: " + e.getMessage());
                }
            });

            // Iniciar as threads
            threadClienteParaProxy.start();
            threadProxyParaCliente.start();

            // Aguardar a finalização das threads
            threadClienteParaProxy.join();
            threadProxyParaCliente.join();

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao redirecionar o cliente para o proxy: " + e.getMessage());
        } finally {
            try {
                socketCliente.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar conexão do cliente: " + e.getMessage());
            }
            conexao = false; // Finaliza a conexão
        }
    }

}