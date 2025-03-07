package Impl;

import Banco.No;
import Cache.NoCache;
import Exception.MyPickException;
import Banco.Banco;
import Cache.Cache;
import OrdemServico.ServiceOrder;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class ImplServidorAplicacao implements Runnable {
    public Socket socketProxy;
    public static int cont = 0;
    private boolean conexao = true;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;
    private Cache<Integer> cache; // Cache compartilhada
    private Banco banco; // Banco compartilhado

    public ImplServidorAplicacao(Socket proxy) {
        this.socketProxy = proxy;
        // this.cache = Cache.instancia; // Obtém a instância única da Cache
        this.banco = Banco.instancia; // Obtém a instância única do Banco
        cont++;  // Incrementa o contador de conexões
    }

    public void run() {
        String mensagemRecebida;
        System.out.println("Conexão " +
                cont +
                " com o aplicação " +
                socketProxy.getInetAddress().getHostAddress() +
                "/" +
                socketProxy.getInetAddress().getHostName()
        );

        try {
            // Prepara para leitura das mensagens do proxy

            saida = new ObjectOutputStream(socketProxy.getOutputStream());
            entrada = new ObjectInputStream(socketProxy.getInputStream());

            // Processa as escolhas do proxy
            while (conexao) {
                try {
                    System.out.println("Conectei");
                    mensagemRecebida =  (String) entrada.readObject();
                    System.out.println(mensagemRecebida);
                    No no = (No) entrada.readObject();

                    cadastrarOS(no);

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                if (mensagemRecebida == null || mensagemRecebida.equalsIgnoreCase("fim")) {
                    conexao = false;
                }
            }

            // Finaliza os recursos
            entrada.close();
            saida.close();
            System.out.println("Fim do proxy " +
                    socketProxy.getInetAddress().getHostAddress());

            socketProxy.close();

        } catch (IOException e) {
            System.err.println("Erro na conexão com o proxy: " + e.getMessage());
        }
    }


    private void processarEscolha(String escolha) {
        switch (escolha) {
            case "1":
                break;
            case "7":
                System.out.println("Fim da conexão");
                conexao = false; // Encerra o loop de processamento
                break;
            default:
                System.out.println("Opção inválida. Tente novamente.");
                break;
        }
    }

    private void cadastrarOS(No no) {
        banco.inserir(no);
        System.out.println("OS cadastrada com sucesso! ID: " + no.getOrder().getCodigoServico());
    }

}
