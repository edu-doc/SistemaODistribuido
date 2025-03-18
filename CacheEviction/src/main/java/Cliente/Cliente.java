package Cliente;

import Impl.ImplCliente;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Cliente {

    Socket socket;
    InetAddress inet;
    String ip;
    int porta;

    public Cliente(String ip, int porta) {

        this.ip = ip;
        this.porta = porta;
        this.rodar();

    }

    private void rodar() {

        /*
         * Para se conectar ao servidor,
         * cria-se objeto Socket.
         * O primeiro parâmetro é o
         * IP ou endereço da máquina que
         * se quer conectar e o segundo é
         * a porta da aplicação.
         * Neste caso, usa-se o IP da
         * máquina local (127.0.0.1) e a porta da
         * aplicação Servidor de Eco (54321).
         */

        try {

            socket = new Socket(ip, porta);
            inet = socket.getInetAddress();

            System.out.println("HostAddress = " + inet.getHostAddress());
            System.out.println("HostName = " + inet.getHostName());

   /*
    * Criar um novo objeto Cliente
    * com a conexão socket para que
    * seja executado em
    * um novo processo.
    * Permitindo assim a conexão de
    * vários clientes com o
    * servidor.

Java
    */

            ImplCliente c = new ImplCliente(socket);
            Thread t = new Thread(c);
            t.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        new Cliente("26.50.141.176", 54321);

    }

}
