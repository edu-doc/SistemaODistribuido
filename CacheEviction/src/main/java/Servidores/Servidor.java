package Servidores;

import Cache.Cache;
import Banco.Banco;
import Banco.No;
import OrdemServico.ServiceOrder;
import Exception.MyPickException;
import Banco.Log;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private Cache<Integer> mc;
    private Banco arv;
    private Log<Integer> log;

    public Servidor(Banco banco, Cache<Integer> cache) {
        this.arv = banco;
        this.mc = cache;
        this.log = new Log<Integer>();
    }

    public void inserir(No no) {
        arv.inserir(no);
        log.log("Inserido no banco", null, no.getOrder().getCodigoServico(), mc.getMapa());
    }

    public void remover(int codigo) throws MyPickException {
        ServiceOrder removida = mc.get(codigo, arv);
        if (removida != null) {
            arv.remover(codigo);
            mc.remover(codigo);
            log.log("Removido do banco e cache", null, codigo, mc.getMapa());
        } else {
            throw new MyPickException("Código não encontrado no banco.");
        }
    }

    public ServiceOrder buscar(int codigo) throws MyPickException {
        ServiceOrder resultado = mc.get(codigo, arv);
        if (resultado != null) {
            log.log("Buscado no banco e inserido na cache", null, codigo, mc.getMapa());
        } else {
            throw new MyPickException("Service order não encontrada");
        }
        return resultado;
    }

    public void listar() {
        listarBanco();
        System.out.println("");
        listarCache();
    }

    public void listarCache() {
        mc.imprimirCache();
    }

    public void listarBanco() {
        arv.listarElementos();
    }

    public void atualizar(Banco banco,int codigo, String nome, String descricao) throws MyPickException {
        ServiceOrder atualizado = arv.buscar(codigo).getOrder();
        if (atualizado != null) {
            arv.atualizar(codigo, nome, descricao);
            atualizado = mc.get(codigo, arv);
            if (atualizado != null) {
                mc.atualizar(codigo, nome, descricao);
            }
            log.log("Atualizado no banco e na cache", null, codigo, mc.getMapa());
        } else {
           throw new MyPickException("Código não encontrado para atualização.");
        }
    }

    public void inserirNoCacheComLimite(No no) {
        if (!mc.getMapa().containsKey(no.getOrder().getCodigoServico())) {
            if (mc.getMapa().size() < 30) {
                mc.put(no.getOrder().getCodigoServico(), no.getOrder());
                log.log("Inserido na cache", null, no.getOrder().getCodigoServico(), mc.getMapa());
            } else {
                System.out.println("Cache cheia! Máximo permitido: 30 elementos.");
            }
        }
    }

    public void inicializar() {
        String[] nomes = {
                "Afonso", "Eduardo", "Pedro", "Arthur", "Lucas", "Brenno", "Bruno", "Maria", "Clara", "João",
                "Guilherme", "Joana", "Margot", "Carlos", "Renato", "Alan", "José", "Henrique", "Gustavo", "Douglas",
                "Ana", "Lara", "Felipe", "Fernanda", "Mariana", "Thiago", "Beatriz", "Leonardo", "Raquel", "Sofia",
                "Igor", "Ricardo", "Carolina", "Gabriel", "Isabela", "Matheus", "André", "Amanda", "Rafael", "Juliana",
                "Bruna", "Thiago", "Julio", "Renata", "Patricia", "Sergio", "Leticia", "Diego", "Marcelo", "Camila",
                "Fábio", "Eliana", "Otávio", "Paula", "Helena", "Vicente", "Simone", "Anderson", "Luiza", "Miguel"
        };

        String[] erros = {
                "Erro: Internet instável", "Erro: Conexão perdida", "Erro: Rede indisponível", "Erro: Falha no DNS",
                "Erro: Timeout de conexão", "Erro: Wi-Fi desconectado", "Erro: Falha na autenticação",
                "Erro: Serviço não encontrado", "Erro: IP conflitante", "Erro: Proxy não responde",
                "Erro: Latência alta", "Erro: Banda limitada", "Erro: Conexão interrompida", "Erro: Porta bloqueada",
                "Erro: VPN falhou", "Erro: Conexão lenta", "Erro: Falha no roteador", "Erro: Gateway inacessível",
                "Erro: Servidor não encontrado", "Erro: DNS temporário", "Erro: Conexão instável", "Erro: Falha na rede",
                "Erro: Interrupção de serviço", "Erro: Falha na comunicação", "Erro: Erro de rede",
                "Erro: Falha na sincronização", "Erro: Pacotes perdidos", "Erro: Falha na resolução de nome",
                "Erro: Falha no handshake", "Erro: Endereço IP inválido", "Erro: Falha no TLS",
                "Erro: Falha na criptografia", "Erro: Ping não respondido", "Erro: Sessão expirada",
                "Erro: Limite de tempo atingido", "Erro: Certificado expirado", "Erro: Endereço MAC bloqueado",
                "Erro: Proxy não encontrado", "Erro: HTTP 404", "Erro: Serviço temporariamente indisponível",
                "Erro: Endereço IP não atribuído", "Erro: Configuração de rede inválida", "Erro: Conexão segura falhou",
                "Erro: Erro de autenticação", "Erro: Pacote de dados corrompido", "Erro: Problema no modem",
                "Erro: Falha no firewall", "Erro: Endereço IP duplicado", "Erro: Servidor sobrecarregado",
                "Erro: Falha na atualização de DNS", "Erro: Erro de SSL", "Erro: Conexão rejeitada",
                "Erro: Falha no roteamento", "Erro: Protocolo não suportado", "Erro: Conexão redefinida",
                "Erro: Erro de configuração do servidor", "Erro: Falha na transferência de dados",
                "Erro: Erro de sincronização de tempo", "Erro: Falha na autenticação de usuário",
                "Erro: Falha na de internet"
        };

        List<No> nos = new ArrayList<>();

        for (int i = 0; i < 60; i++) {
            ServiceOrder order = new ServiceOrder(nomes[i], erros[i]);
            No no = new No(order);
            nos.add(no);
            inserir(no); // Insere no banco
        }

        for (int i = 30; i < 60; i++) {
            inserirNoCacheComLimite(nos.get(i));
        }
    }

    @Override
    public String toString() {
        return "Servidor{" +
                "cache=" + mc +
                ", banco=" + arv +
                '}';
    }
}
