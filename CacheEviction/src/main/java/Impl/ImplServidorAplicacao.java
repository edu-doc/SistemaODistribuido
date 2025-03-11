package Impl;

import Banco.No;
import Cache.NoCache;
import Exception.MyPickException;
import Banco.Banco;
import Cache.Cache;
import Logger.Logger;
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
    private Logger log;

    public ImplServidorAplicacao(Socket proxy) {
        this.socketProxy = proxy;
        this.banco = Banco.instancia;
        this.log = new Logger();
        if(cont == 0){
            inicializar();
        }
        cont++;  // Incrementa o contador de conexões
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
    public void inicializar(){
        List<No> nos = new ArrayList<>(); // Lista para armazenar os nós

        String[][] dados = {
                {"Paulo", "Erro: Internet instável"},
                {"Eduardo", "Erro: Conexão perdida"},
                {"Pedro", "Erro: Rede indisponível"},
                {"Arthur", "Erro: Falha no DNS"},
                {"Lucas", "Erro: Timeout de conexão"},
                {"Brenno", "Erro: Wi-Fi desconectado"},
                {"Bruno", "Erro: Falha na autenticação"},
                {"Maria", "Erro: Serviço não encontrado"},
                {"Clara", "Erro: IP conflitante"},
                {"Joao", "Erro: Proxy não responde"},
                {"Guilherme", "Erro: Latência alta"},
                {"Joana", "Erro: Banda limitada"},
                {"Margot", "Erro: Conexão interrompida"},
                {"Carlos", "Erro: Porta bloqueada"},
                {"Renato", "Erro: VPN falhou"},
                {"Alan", "Erro: Conexão lenta"},
                {"Jose", "Erro: Falha no roteador"},
                {"Henrique", "Erro: Gateway inacessível"},
                {"Gustavo", "Erro: Servidor não encontrado"},
                {"Douglas", "Erro: DNS temporário"},
                {"Ana", "Erro: Conexão instável"},
                {"Lara", "Erro: Falha na rede"},
                {"Felipe", "Erro: Interrupção de serviço"},
                {"Fernanda", "Erro: Falha na comunicação"},
                {"Mariana", "Erro: Erro de rede"},
                {"Thiago", "Erro: Falha na sincronização"},
                {"Beatriz", "Erro: Pacotes perdidos"},
                {"Leonardo", "Erro: Falha na resolução de nome"},
                {"Raquel", "Erro: Falha no handshake"},
                {"Sofia", "Erro: Endereço IP inválido"},
                {"Igor", "Erro: Falha no TLS"},
                {"Ricardo", "Erro: Falha na criptografia"},
                {"Carolina", "Erro: Ping não respondido"},
                {"Gabriel", "Erro: Sessão expirada"},
                {"Isabela", "Erro: Limite de tempo atingido"},
                {"Matheus", "Erro: Certificado expirado"},
                {"Lucas", "Erro: Endereço MAC bloqueado"},
                {"Amanda", "Erro: Proxy não encontrado"},
                {"Rafael", "Erro: HTTP 404"},
                {"Juliana", "Erro: Serviço temporariamente indisponível"},
                {"Bruna", "Erro: Endereço IP não atribuído"},
                {"Thiago", "Erro: Configuração de rede inválida"},
                {"Julio", "Erro: Conexão segura falhou"},
                {"Renata", "Erro: Erro de autenticação"},
                {"Patricia", "Erro: Pacote de dados corrompido"},
                {"Sergio", "Erro: Problema no modem"},
                {"Leticia", "Erro: Falha no firewall"},
                {"Diego", "Erro: Endereço IP duplicado"},
                {"Marcelo", "Erro: Servidor sobrecarregado"},
                {"Camila", "Erro: Falha na atualização de DNS"},
                {"Fabio", "Erro: Erro de SSL"},
                {"Eliana", "Erro: Conexão rejeitada"},
                {"Otavio", "Erro: Falha no roteamento"},
                {"Paula", "Erro: Protocolo não suportado"},
                {"Helena", "Erro: Conexão redefinida"},
                {"Vicente", "Erro: Erro de configuração do servidor"},
                {"Simone", "Erro: Falha na transferência de dados"},
                {"Anderson", "Erro: Erro de sincronização de tempo"},
                {"Luiza", "Erro: Falha na autenticação de usuário"},
                {"Miguel", "Erro: Serviço temporariamente inacessível"}
        };

        for (String[] dado : dados) {
            ServiceOrder ordem = new ServiceOrder(dado[0], dado[1]);
            No no = new No(ordem); // Criando um nó para cada ordem
            nos.add(no); // Adiciona o nó à lista
        }

        // Exemplo de saída para verificar os nós criados
        for (No no : nos) {
            banco.inserir(no);
        }
    }
}
