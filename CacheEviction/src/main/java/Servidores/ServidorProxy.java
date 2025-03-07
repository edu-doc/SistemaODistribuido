package Servidores;

import Impl.ImplServidorProxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorProxy {

    ServerSocket socketServidor;
    Socket socketAplicacao;
    Socket cliente;
    int porta;
    int portaAplicacao;

    public ServidorProxy(int porta, int portaAplicacao) {
        this.porta = porta;
        this.portaAplicacao = portaAplicacao;
        this.rodar();
    }

    private void rodar() {

        /*
         * Cria um socket na porta 54321
         */
        try {
            socketServidor = new ServerSocket(porta);
            socketAplicacao = new Socket("127.0.0.1", portaAplicacao);

            System.out.println("Servidor rodando na porta " +
                    socketServidor.getLocalPort());
            System.out.println("HostAddress = " +
                    InetAddress.getLocalHost().getHostAddress());
            System.out.println("HostName = " +
                    InetAddress.getLocalHost().getHostName());


   /*
    * Aguarda alguém se conectar.
    * A execução do servidor fica bloqueada na chamada
    * do método accept da classe ServerSocket.

Java
    * Quando alguém se conectar ao
    * servidor, o método desbloqueia e
    * retorna com um objeto da classe Socket, que
    * é uma porta da comunicação.
    */

            System.out.println("Aguardando conexão do cliente...");

            while (true) {

                cliente = socketServidor.accept();

                // Cria uma thread do servidor para tratar a conexão
                ImplServidorProxy servidorProxy = new ImplServidorProxy(cliente,socketAplicacao);

                Thread t = new Thread(servidorProxy);
                // Inicia a thread para o cliente conectado

                t.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        new ServidorProxy(12345,12321);

    }

}
