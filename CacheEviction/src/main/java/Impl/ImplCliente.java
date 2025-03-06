package Impl;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ImplCliente implements Runnable {

    private Socket cliente;
    private boolean conexao = true;
    private PrintWriter saida;
    private BufferedReader entrada;

    public ImplCliente(Socket c) {
        this.cliente = c;
    }

    public void run() {
        try {
            System.out.println("O cliente conectou ao servidor");

            // Prepara para leitura do teclado
            Scanner teclado = new Scanner(System.in);

            // Cria objeto para enviar a mensagem ao servidor
            saida = new PrintWriter(cliente.getOutputStream(), true);

            // Cria objeto para ler as mensagens do servidor
            entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

            // Thread para ler as mensagens do servidor
            new Thread(() -> {
                try {
                    String mensagemServidor;
                    while ((mensagemServidor = entrada.readLine()) != null) {
                        System.out.println("Servidor: " + mensagemServidor);
                    }
                } catch (IOException e) {
                    System.out.println("Conexão com o servidor foi fechada.");
                }
            }).start();

            // Envia mensagem ao servidor
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
            saida.close();
            entrada.close();
            teclado.close();
            cliente.close();
            System.out.println("Cliente finaliza conexão.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}