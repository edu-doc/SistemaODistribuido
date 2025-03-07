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
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImplServidorAplicacao implements Runnable {
    public Socket socketProxy;
    public static int cont = 0;
    private boolean conexao = true;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;
    private Banco banco; // Banco compartilhado

    public ImplServidorAplicacao(Socket proxy) {
        this.socketProxy = proxy;
        this.banco = Banco.instancia;
        cont++;  // Incrementa o contador de conexões
    }

    public void run() {
        System.out.println("Conexão " + cont + " com o proxy " + socketProxy.getInetAddress().getHostAddress());

        try {
            saida = new ObjectOutputStream(socketProxy.getOutputStream());
            entrada = new ObjectInputStream(socketProxy.getInputStream());

            while (conexao) {
                try {
                    List<Object> lista = (List<Object>) entrada.readObject();
                    String comando = (String) lista.get(0);
                    processarEscolha(comando, lista);
                } catch (ClassNotFoundException e) {
                    System.err.println("Erro na leitura do objeto: " + e.getMessage());
                }
            }

            entrada.close();
            saida.close();
            socketProxy.close();

        } catch (IOException e) {
            System.err.println("Erro na conexão com o proxy: " + e.getMessage());
        }
    }

    private void processarEscolha(String comando, List<Object> lista) throws IOException {
        switch (comando) {
            case "cadastro":
                No no = (No) lista.get(1);
                banco.inserir(no);
                saida.writeObject("Cadastro realizado com sucesso!");
                saida.flush();
                break;
            case "remover":
                int idRemover = (Integer) lista.get(1);
                banco.remover(idRemover);
                saida.writeObject("Remoção realizada com sucesso!");
                saida.flush();
                break;
            case "listar":
                List<String> listaOS = banco.listarElementos(); // Obtém a lista de OS do banco
                saida.writeObject(listaOS); // Envia a lista de OS para o proxy
                saida.flush();
                break;
            case "buscar":
                int idBuscar = (Integer) lista.get(1);
                No resultado = banco.buscar(idBuscar);
                if (resultado != null) {
                    saida.writeObject(resultado); // Retorna um objeto do tipo Banco.No
                } else {
                    saida.writeObject(null); // Retorna null se não encontrar
                }
                saida.flush();
                break;
            case "alterar":
                int idAlterar = (Integer) lista.get(1);
                String nome = (String) lista.get(2);
                String descricao = (String) lista.get(3);
                banco.atualizar(idAlterar, nome, descricao);
                saida.writeObject("Alteração realizada com sucesso!");
                saida.flush();
                break;
            default:
                saida.writeObject("Comando inválido!");
                saida.flush();
                break;
        }
    }
}
