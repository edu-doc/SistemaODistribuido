package Banco;

import OrdemServico.ServiceOrder;

import java.util.ArrayList;
import java.util.List;

public class Banco {

    private No raiz;
    public static final Banco instancia = new Banco();
    private final LogBanco logBanco;

    public Banco() {
        this.raiz = null;
        this.logBanco = new LogBanco();
        inicializar();
    }

    public  void inserir(No no) {
        synchronized(Banco.class) {
            raiz = inserirRN(raiz, no);
            raiz.setCor(No.Cor.PRETO);
            logBanco.log("Inserido no banco", no);
        }
    }

    private No inserirRN(No no, No novoNo) {
        if (no == null) {
            return novoNo;
        }

        if (novoNo.getOrder().getCodigoServico() < no.getOrder().getCodigoServico()) {
            no.setEsquerda(inserirRN(no.getEsquerda(), novoNo));
            no.getEsquerda().setPai(no);
        } else if (novoNo.getOrder().getCodigoServico() > no.getOrder().getCodigoServico()) {
            no.setDireita(inserirRN(no.getDireita(), novoNo));
            no.getDireita().setPai(no);
        } else {
            return no;
        }

        return balancear(no);
    }

    public No buscar(int chave) {
        synchronized (Banco.class) {
            logBanco.log("Buscando no banco", buscarRN(raiz, chave));
            return buscarRN(raiz, chave);
        }
    }

    private No buscarRN(No no, int chave) {
        if (no == null || no.getOrder().getCodigoServico() == chave) {
            return no;
        }

        if (chave < no.getOrder().getCodigoServico()) {
            return buscarRN(no.getEsquerda(), chave);
        } else {
            return buscarRN(no.getDireita(), chave);
        }
    }

    public No remover(int chave) {
        synchronized(Banco.class) {
            No noRemovido = buscarRN(raiz, chave);
            if (noRemovido != null) {
                raiz = removerRN(raiz, chave);
                if (raiz != null) {
                    raiz.setCor(No.Cor.PRETO);
                }
                logBanco.log("Removido no banco", noRemovido);
                return noRemovido;
            }
            return null;
        }
    }

    private No removerRN(No no, int chave) {
        if (no == null) {
            return null;
        }

        if (chave < no.getOrder().getCodigoServico()) {
            no.setEsquerda(removerRN(no.getEsquerda(), chave));
        } else if (chave > no.getOrder().getCodigoServico()) {
            no.setDireita(removerRN(no.getDireita(), chave));
        } else {
            if (no.getEsquerda() == null || no.getDireita() == null) {
                No temp = (no.getEsquerda() != null) ? no.getEsquerda() : no.getDireita();
                return temp;
            } else {
                No sucessor = menorNo(no.getDireita());
                no.setOrder(sucessor.getOrder());
                no.setDireita(removerRN(no.getDireita(), sucessor.getOrder().getCodigoServico()));
            }
        }

        return balancear(no);
    }

    private No balancear(No no) {
        if (isVermelho(no.getDireita()) && !isVermelho(no.getEsquerda())) {
            no = rotacaoEsquerda(no);
        }

        if (isVermelho(no.getEsquerda()) && isVermelho(no.getEsquerda().getEsquerda())) {
            no = rotacaoDireita(no);
        }

        if (isVermelho(no.getEsquerda()) && isVermelho(no.getDireita())) {
            inverterCores(no);
        }

        return no;
    }

    private boolean isVermelho(No no) {
        return no != null && no.getCor() == No.Cor.VERMELHO;
    }

    private void inverterCores(No no) {
        no.setCor((no.getCor() == No.Cor.VERMELHO) ? No.Cor.PRETO : No.Cor.VERMELHO);
        no.getEsquerda().setCor((no.getEsquerda().getCor() == No.Cor.VERMELHO) ? No.Cor.PRETO : No.Cor.VERMELHO);
        no.getDireita().setCor((no.getDireita().getCor() == No.Cor.VERMELHO) ? No.Cor.PRETO : No.Cor.VERMELHO);
    }

    private No rotacaoEsquerda(No no) {
        No temp = no.getDireita();
        no.setDireita(temp.getEsquerda());
        temp.setEsquerda(no);
        temp.setCor(no.getCor());
        no.setCor(No.Cor.VERMELHO);
        return temp;
    }

    private No rotacaoDireita(No no) {
        No temp = no.getEsquerda();
        no.setEsquerda(temp.getDireita());
        temp.setDireita(no);
        temp.setCor(no.getCor());
        no.setCor(No.Cor.VERMELHO);
        return temp;
    }

    private No menorNo(No no) {
        No atual = no;
        while (atual.getEsquerda() != null) {
            atual = atual.getEsquerda();
        }
        return atual;
    }

    public List<String> listarElementos() {
        List<String> listaOS = new ArrayList<>();
        inOrder(raiz, listaOS);
        return listaOS;
    }

    private void inOrder(No no, List<String> listaOS) {
        if (no != null) {
            inOrder(no.getEsquerda(), listaOS);
            listaOS.add(formatarNo(no));
            inOrder(no.getDireita(), listaOS);
        }
    }

    private String formatarNo(No no) {
        return "ID: " + no.getOrder().getCodigoServico() +
                ", Nome: " + no.getOrder().getNome() +
                ", Descrição: " + no.getOrder().getDescricao();
    }

    public boolean atualizar(int codigoServico, String nome, String descricao) {
        synchronized(Banco.class) {
            No no = buscarRN(raiz, codigoServico);
            if (no != null) {
                no.getOrder().setNome(nome);
                no.getOrder().setDescricao(descricao);
                logBanco.log("Atualizao no Banco",no);
                return true;
            }
            System.out.println("Service Order não encontrada para atualização: " + codigoServico);
            return false;
        }
    }

    public int contarNos() {
        return contarNos(raiz);
    }

    private int contarNos(No no) {
        if (no == null) {
            return 0;
        }
        return 1 + contarNos(no.getEsquerda()) + contarNos(no.getDireita());
    }

    public No getRaiz() {
        return raiz;
    }

    public void setRaiz(No raiz) {
        this.raiz = raiz;
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
                {"Miguel", "Erro: Serviço temporariamente inacessível"},
                {"Rodrigo", "Erro: Cache de DNS corrompido"},
                {"Tatiane", "Erro: Falha no handshake TLS"},
                {"Emanuel", "Erro: Conflito de IP detectado"},
                {"Cristina", "Erro: Conexão interrompida inesperadamente"},
                {"Nelson", "Erro: Erro ao resolver domínio"},
                {"Flávia", "Erro: Acesso negado ao servidor"},
                {"André", "Erro: Requisição expirou"},
                {"Vanessa", "Erro: Falha na negociação de SSL"},
                {"Fábia", "Erro: Firewall bloqueou o tráfego"},
                {"Maurício", "Erro: Servidor DNS não disponível"},
                {"Brenda", "Erro: Erro de autenticação WPA2"},
                {"Luís", "Erro: Resposta inválida do servidor"},
                {"Roberta", "Erro: Tempo de resposta excedido"},
                {"Gerson", "Erro: Serviço de rede desativado"},
                {"Cristiano", "Erro: Porta de conexão fechada"},
                {"Aline", "Erro: Múltiplas conexões simultâneas detectadas"},
                {"Jorge", "Erro: Falha na criptografia de dados"},
                {"Isis", "Erro: Configuração de proxy incorreta"},
                {"Filipe", "Erro: Falha na negociação de protocolo"},
                {"Vivian", "Erro: Servidor sobrecarregado"},
                {"Renan", "Erro: Interferência na conexão Wi-Fi"},
                {"Nathalia", "Erro: Configuração de DNS inválida"},
                {"Douglas", "Erro: Ping alto detectado"},
                {"Davi", "Erro: Perda excessiva de pacotes"},
                {"Sandra", "Erro: Requisição de IP falhou"},
                {"Guilherme", "Erro: Sessão de usuário expirada"},
                {"Débora", "Erro: Conflito entre IPv4 e IPv6"},
                {"Victor", "Erro: Nenhuma resposta do gateway"},
                {"Tainá", "Erro: Servidor remoto não encontrado"},
                {"Caio", "Erro: Erro de roteamento"},
                {"Lilian", "Erro: Falha na conversão de endereço"},
                {"Otávio", "Erro: Permissão de acesso negada"},
                {"Hugo", "Erro: O servidor não suporta esse protocolo"},
                {"Marisa", "Erro: Sincronização de tempo falhou"},
                {"João", "Erro: Chave de criptografia inválida"},
                {"Letícia", "Erro: Proxy não autenticado"},
                {"Geraldo", "Erro: Servidor de cache corrompido"},
                {"Eduarda", "Erro: Não foi possível acessar a VPN"}
        };

        for (String[] dado : dados) {
            ServiceOrder ordem = new ServiceOrder(dado[0], dado[1]);
            No no = new No(ordem); // Criando um nó para cada ordem
            nos.add(no); // Adiciona o nó à lista
        }

        // Exemplo de saída para verificar os nós criados
        for (No no : nos) {
            inserir(no);
        }
    }
}
