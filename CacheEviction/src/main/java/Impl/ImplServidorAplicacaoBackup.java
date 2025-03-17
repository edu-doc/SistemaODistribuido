package Impl;

import Banco.Banco;
import Banco.No;
import Logger.Logger;
import RMI.AplicacaoRemoteInterface;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ImplServidorAplicacaoBackup implements Runnable, AplicacaoRemoteInterface {
    public Socket socketProxy;
    public static int cont = 0;
    private boolean conexao = true;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;
    private Banco banco; // Banco compartilhado
    private Logger log;

    public ImplServidorAplicacaoBackup(Socket proxy) {
        this.socketProxy = proxy;
        this.banco = Banco.instancia;
        this.log = new Logger();
        cont++;  // Incrementa o contador de conexões

        // Registra o proxy no RMI Registry
        try {
            AplicacaoRemoteInterface stub = (AplicacaoRemoteInterface) UnicastRemoteObject.exportObject(this, 0);
            LocateRegistry.createRegistry(992);
            Registry registry = LocateRegistry.getRegistry("localhost", 992);
            registry.bind("Backup", stub);
            System.out.println("Backup registrado no rmi");
            log.info("Backup registrado no rmi");
        } catch (RemoteException | AlreadyBoundException e) {
            Logger.error("Erro ao registrar Backup no RMI Registry: " + e.getMessage(), e);
        }

    }

    public void run() {
        System.out.println("Conexão " + cont + " com o proxy " + socketProxy.getInetAddress().getHostAddress());
        log.info("Conexão " + cont + " estabelecida com o proxy " + socketProxy.getInetAddress().getHostAddress());

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
                    log.error("Erro na leitura do objeto: " + e.getMessage(), e);
                }
            }

            entrada.close();
            saida.close();
            socketProxy.close();

        } catch (IOException e) {
            System.err.println("Erro na conexão com o proxy: " + e.getMessage());
            log.error("Erro na conexão com o proxy: " + e.getMessage(), e);
        } finally {
            try {
                socketProxy.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar conexão do proxy: " + e.getMessage());
            }
        }
    }

    private void processarEscolha(String comando, List<Object> lista) throws IOException {
        switch (comando) {
            case "cadastro":
                No no = (No) lista.get(1);
                banco.inserir(no);
                saida.writeObject("Cadastro realizado com sucesso!");
                log.info("Cadastro realizado com sucesso!");
                saida.flush();
                break;
            case "remover":
                int idRemover = (Integer) lista.get(1);
                banco.remover(idRemover);
                saida.writeObject("Remoção realizada com sucesso!");
                log.info("Remoção realizada com sucesso!");
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
                log.info("Alteração realizada com sucesso!");
                saida.flush();
                break;
            default:
                saida.writeObject("Comando inválido!");
                saida.flush();
                break;
        }
    }

    @Override
    public List<String> inserirBackup(No no) throws RemoteException {

        return null;
    }
}
