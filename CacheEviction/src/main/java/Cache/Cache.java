package Cache;

import Banco.No;
import Banco.Banco;
import OrdemServico.ServiceOrder;
import Exception.MyPickException;
import java.util.HashMap;

public class Cache<K> {
    private final int capacidade;
    private final HashMap<K, NoCache<K, ServiceOrder>> mapa;
    private NoCache<K, ServiceOrder> inicio;
    private NoCache<K, ServiceOrder> fim;
    public static final Cache<Integer> instancia = new Cache<Integer>();

    public Cache() {
        this.capacidade = 30;
        this.mapa = new HashMap<>();
        this.inicio = null;
        this.fim = null;
    }

    public void put(K chave, ServiceOrder ordem) {
        if (mapa.containsKey(chave)) {
            NoCache<K, ServiceOrder> no = mapa.get(chave);
            no.setValor(ordem);
            moverParaInicio(no);
        } else {
            NoCache<K, ServiceOrder> novoNo = new NoCache<K, ServiceOrder>(chave, ordem);
            if (mapa.size() >= capacidade) {
                removerUltimo();
            }
            adicionarNoInicio(novoNo);
            mapa.put(chave, novoNo);
        }
    }

    public ServiceOrder get(K chave, Banco banco) throws MyPickException {
        if (mapa.containsKey(chave)) {
            NoCache<K, ServiceOrder> no = mapa.get(chave);
            moverParaInicio(no); 
            return no.getValor();
        }

        No resultadoBanco = banco.buscar((Integer) chave);
        if (resultadoBanco != null) {
            ServiceOrder ordem = resultadoBanco.getOrder();
            put(chave, ordem);
            return ordem;
        } else {
            throw new MyPickException("Ordem não encontrada nem no banco.");
        }
    }

    public void atualizar(K chave, String novoNome, String novaDescricao) {
        if (mapa.containsKey(chave)) {
            NoCache<K, ServiceOrder> no = mapa.get(chave);
            ServiceOrder ordem = no.getValor();

            ordem.setNome(novoNome);
            ordem.setDescricao(novaDescricao);

            moverParaInicio(no);
        }
    }

    public void remover(K chave) {
        if (!mapa.containsKey(chave)) return;

        NoCache<K, ServiceOrder> no = mapa.get(chave);

        if (no.getAnterior() != null) {
            no.getAnterior().setProximo(no.getProximo());
        } else {
            inicio = no.getProximo();
        }

        if (no.getProximo() != null) {
            no.getProximo().setAnterior(no.getAnterior());
        } else {
            fim = no.getAnterior();
        }

        mapa.remove(chave);
    }

    private void moverParaInicio(NoCache<K, ServiceOrder> no) {
        if (no == inicio) return;

        if (no.getAnterior() != null) {
            no.getAnterior().setProximo(no.getProximo());
        }
        if (no.getProximo() != null) {
            no.getProximo().setAnterior(no.getAnterior());
        }
        if (no == fim) {
            fim = no.getAnterior();
        }

        adicionarNoInicio(no);
    }

    private void adicionarNoInicio(NoCache<K, ServiceOrder> no) {
        no.setProximo(inicio);
        no.setAnterior(null);

        if (inicio != null) {
            inicio.setAnterior(no);
        }
        inicio = no;

        if (fim == null) {
            fim = inicio;
        }
    }

    private void removerUltimo() {
        if (fim != null) {
            mapa.remove(fim.getChave());
            if (fim.getAnterior() != null) {
                fim.getAnterior().setProximo(null);
            } else {
                inicio = null;
            }
            fim = fim.getAnterior();
        }
    }

    public void imprimirCache() {
        NoCache<K, ServiceOrder> atual = inicio;
        while (atual != null) {
            System.out.println("Chave: " + atual.getChave() +
                    ", Nome: " + atual.getValor().getNome() +
                    ", Descrição: " + atual.getValor().getDescricao());
            atual = atual.getProximo();
        }
    }

    public HashMap<K, NoCache<K, ServiceOrder>> getMapa() {
        return mapa;
    }
}
