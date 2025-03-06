package Impl;

import java.io.*;
import java.net.Socket;

public class ImplServidor implements Runnable {

    public Socket socketCliente;
    public static int cont = 0;
    private boolean conexao = true;
    private BufferedReader entrada;
    private PrintWriter saida;

    public ImplServidor(Socket cliente) {
        this.socketCliente = cliente;
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
        saida.println("1. Opção 1");
        saida.println("2. Finalizar conexão");
    }

    private void processarEscolha(String escolha) {
        switch (escolha) {
            case "1":
                saida.println("Você escolheu a Opção 1. Executando ação...");
                // Aqui você pode adicionar a lógica para a Opção 1
                break;
            case "2":
                saida.println("Você escolheu a Opção 2. Finalizando conexão...");
                conexao = false; // Encerra o loop de processamento
                break;
            default:
                saida.println("Opção inválida. Tente novamente.");
                break;
        }
    }
}