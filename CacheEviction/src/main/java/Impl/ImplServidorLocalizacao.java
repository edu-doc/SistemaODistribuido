package Impl;

import java.io.*;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Logger.Logger;
import RMI.LocalizacaoRemoteInterface;
import RMI.ProxyRemoteInterface;

public class ImplServidorLocalizacao implements Runnable, LocalizacaoRemoteInterface {

    public Socket socketCliente;
    public static int cont = 0;
    private boolean conexao = true;
    private BufferedReader entrada;
    private PrintWriter saida;
    private final Logger log;
    public static int cont1 = 0;
    public static int cont2 = 0;
    public static int cont3 = 0;
    private static boolean chave = true;

    public ImplServidorLocalizacao(Socket cliente) {
        this.socketCliente = cliente;
        this.log = new Logger();
        cont++;  // Incrementa o contador de conexões

        if (chave == true) {
            // Registra o proxy no RMI Registry
            try {
                LocalizacaoRemoteInterface stub = (LocalizacaoRemoteInterface) UnicastRemoteObject.exportObject(this, 0);
                LocateRegistry.createRegistry(994);
                Registry registry = LocateRegistry.getRegistry("localhost", 994);
                registry.bind("Localizacao", stub);
                System.out.println("localização registrado no rmi");
                Logger.info("Localização registrado no RMI Registry como: " + "Localizacao");
            } catch (RemoteException | AlreadyBoundException e) {
                Logger.error("Erro ao registrar localização no RMI Registry: " + e.getMessage(), e);
            }
            chave = false;
        }

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
            System.out.println("Fim do cliente " + socketCliente.getInetAddress().getHostAddress());

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
                try {
                    saida.println("REDIRECT:"+retornaPorta());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
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


    @Override
    public synchronized int retornaPorta() throws RemoteException {
        int portaProxy; // Porta do servidor de proxy

        // Verifica a conectividade das portas em ordem de prioridade
        if (cont1 <= cont2 && cont1 <= cont3) {
            portaProxy = 12345;
            if (testarPortaAtiva(portaProxy)) {
                System.out.println("Proxy 1 selecionado (Porta " + portaProxy + ")");
                cont1++;
                return portaProxy;
            }
        }

        if (cont2 <= cont3) {
            portaProxy = 12354;
            if (testarPortaAtiva(portaProxy)) {
                System.out.println("Proxy 2 selecionado (Porta " + portaProxy + ")");
                cont2++;
                return portaProxy;
            }
        }

        portaProxy = 12355;
        if (testarPortaAtiva(portaProxy)) {
            System.out.println("Proxy 3 selecionado (Porta " + portaProxy + ")");
            cont3++;
            return portaProxy;
        }

        // Se nenhuma porta estiver ativa, lança uma exceção
        throw new RemoteException("Nenhum servidor proxy está disponível no momento.");
    }

    private boolean testarPortaAtiva(int porta) {
        try (Socket socket = new Socket("localhost", porta)) {
            // Se a conexão for bem-sucedida, a porta está ativa
            return true;
        } catch (IOException e) {
            // Se ocorrer uma exceção, a porta não está ativa
            System.err.println("Porta " + porta + " não está ativa: " + e.getMessage());
            return false;
        }
    }

}