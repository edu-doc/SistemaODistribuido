package Impl;

import Logger.Logger;
import RMI.LocalizacaoRemoteInterface;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ImplCliente implements Runnable {

    private Socket cliente;
    private boolean conexao = true;
    private PrintWriter saida;
    private BufferedReader entrada;

    public ImplCliente(Socket c) {
        this.cliente = c;
    }

    @Override
    public void run() {
        conectarServidor(cliente);

        // Loop para enviar mensagens ao servidor
        Scanner teclado = new Scanner(System.in);
        String mensagem;
        while (conexao) {
            mensagem = teclado.nextLine();
            if (mensagem.equalsIgnoreCase("fim")) {
                conexao = false;
            } else {
                saida.println(mensagem);
            }
        }

        // Fecha os recursos
        fecharConexao();
        System.out.println("Cliente finaliza conexão.");
    }

    private void conectarServidor(Socket socket) {
        try {
            this.cliente = socket;
            this.saida = new PrintWriter(cliente.getOutputStream(), true);
            this.entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

            System.out.println("O cliente conectou ao servidor");

            // Thread para escutar mensagens do servidor
            new Thread(() -> {
                try {
                    String mensagemServidor;
                    while ((mensagemServidor = entrada.readLine()) != null) {
                        System.out.println(mensagemServidor);

                        // Verifica se há redirecionamento para outro proxy
                        if (mensagemServidor.startsWith("REDIRECT:")) {
                            int novaPorta = Integer.parseInt(mensagemServidor.split(":")[1].trim());
                            redirecionarParaNovoProxy(novaPorta);
                            break; // Sai do loop de leitura para reiniciar a conexão
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Erro na leitura do servidor: " + e.getMessage());
                    tentarNovoProxy();
                }
            }).start();

        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            tentarNovoProxy();
        }
    }

    private void redirecionarParaNovoProxy(int novaPorta) {
        try {
            System.out.println("Redirecionando para a porta " + novaPorta);
            Logger.info("Redirecionando cliente para proxy na porta " + novaPorta);

            fecharConexao();
            cliente = new Socket(cliente.getInetAddress(), novaPorta);
            conectarServidor(cliente);
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao novo proxy: " + e.getMessage());
            tentarNovoProxy();
        }
    }

    private void tentarNovoProxy() {
        try {
            Logger.info("Consultando o servidor de localização para um novo proxy...");
            Registry registry = LocateRegistry.getRegistry("localhost", 994);
            LocalizacaoRemoteInterface localizacao = (LocalizacaoRemoteInterface) registry.lookup("Localizacao");

            int novaPorta = localizacao.retornaPorta();
            System.out.println("Obtida nova porta do proxy: " + novaPorta);
            Logger.info("Tentando conectar ao novo proxy na porta " + novaPorta);

            cliente = new Socket(cliente.getInetAddress(), novaPorta);
            conectarServidor(cliente);
        } catch (IOException | NotBoundException e) {
            System.err.println("Falha ao conectar ao novo proxy: " + e.getMessage());
        }
    }

    private void fecharConexao() {
        try {
            if (saida != null) saida.close();
            if (entrada != null) entrada.close();
            if (cliente != null) cliente.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}