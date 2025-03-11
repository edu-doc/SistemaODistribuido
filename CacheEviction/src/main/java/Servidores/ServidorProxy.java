package Servidores;

import Impl.ImplServidorProxy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorProxy {
    private ServerSocket socketServidor;
    private int porta;
    private int portaAplicacao;

    public ServidorProxy(int porta, int portaAplicacao) {
        this.porta = porta;
        this.portaAplicacao = portaAplicacao;
        this.rodar();
    }

    private void rodar() {
        try {
            socketServidor = new ServerSocket(porta);
            System.out.println("Servidor rodando na porta " + socketServidor.getLocalPort());
            System.out.println("HostAddress = " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("HostName = " + InetAddress.getLocalHost().getHostName());
            System.out.println("Aguardando conexão do cliente...");

            while (true) {
                Socket cliente = socketServidor.accept(); // Aceita a conexão do cliente

                // Cria um novo socket para se comunicar com o servidor de aplicação
                Socket socketAplicacao = new Socket("127.0.0.1", portaAplicacao);

                // Cria uma thread para tratar a conexão do cliente
                ImplServidorProxy servidorProxy = new ImplServidorProxy(cliente, socketAplicacao);
                Thread t = new Thread(servidorProxy);
                t.start(); // Inicia a thread para o cliente conectado
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServidorProxy(12345, 12322);
    }
}
