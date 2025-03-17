package Servidores;

import Impl.ImplServidorProxy;
import Impl.ImplServidorProxy2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorProxy2 {
    private ServerSocket socketServidor;
    private final int porta;
    private final int portaAplicacao;
    private final int portaBackup;

    public ServidorProxy2(int porta, int portaAplicacao, int portaBackup) {
        this.porta = porta;
        this.portaAplicacao = portaAplicacao;
        this.portaBackup = portaBackup;
        this.rodar();
    }

    private void rodar() {
        try {
            socketServidor = new ServerSocket(porta);
            System.out.println("Servidor proxy 1 rodando na porta " + porta);
            System.out.println("HostAddress = " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("HostName = " + InetAddress.getLocalHost().getHostName());
            System.out.println("Aguardando conexão do cliente...");

            while (true) {
                Socket cliente = socketServidor.accept(); // Aceita a conexão do cliente
                System.out.println("Novo cliente conectado: " + cliente.getInetAddress().getHostAddress());

                // Cria um novo socket para se comunicar com o servidor de aplicação
                Socket socketAplicacao = new Socket("127.0.0.1", portaAplicacao);

                // Cria um novo socket para se comunicar com o servidor de backup
                Socket socketBackup = new Socket("127.0.0.1", portaBackup);

                // Cria uma thread para tratar a conexão do cliente
                ImplServidorProxy2 servidorProxy = new ImplServidorProxy2(cliente, socketAplicacao, socketBackup, "Proxy2");
                Thread t = new Thread(servidorProxy);
                t.start(); // Inicia a thread para o cliente conectado
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor proxy 2: " + e.getMessage());
        } finally {
            encerrarServidor();
        }
    }

    private void encerrarServidor() {
        try {
            if (socketServidor != null && !socketServidor.isClosed()) {
                socketServidor.close();
                System.out.println("Servidor proxy 2 encerrado.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao encerrar servidor proxy 2: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ServidorProxy2(12354, 12322, 12310);
    }
}