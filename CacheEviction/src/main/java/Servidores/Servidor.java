package Servidores;

import Impl.ImplServidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    ServerSocket socketServidor;
    Socket cliente;
    int porta;

    public Servidor(int porta) {
        this.porta = porta;
        this.rodar();
    }

    private void rodar() {

        /*
         * Cria um socket na porta 54321
         */
        try {
            socketServidor = new ServerSocket(porta);

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
                ImplServidor servidor = new ImplServidor(cliente);

                Thread t = new Thread(servidor);
                // Inicia a thread para o cliente conectado

                t.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        new Servidor(54321);

    }

}
