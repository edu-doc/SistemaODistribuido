package Cache;

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
    private final LogCache<K> logCache;

    public Cache() {
        this.capacidade = 30;
        this.mapa = new HashMap<>();
        this.inicio = null;
        this.fim = null;
        this.logCache = new LogCache<K>();
    }

    public void put(K chave, ServiceOrder ordem) {
        synchronized(Cache.class) {
            if (mapa.containsKey(chave)) {
                NoCache<K, ServiceOrder> no = mapa.get(chave);
                no.setValor(ordem);
                moverParaInicio(no);
                logCache.log("Service Order já na Cache e movido para o inicio",chave,getMapa());
            } else {
                NoCache<K, ServiceOrder> novoNo = new NoCache<K, ServiceOrder>(chave, ordem);
                if (mapa.size() >= capacidade) {
                    removerUltimo();
                }
                adicionarNoInicio(novoNo);
                mapa.put(chave, novoNo);
                logCache.log("Service Order adicionada pelo Banco e movido para o inicio",chave,getMapa());
            }
        }
    }

    public ServiceOrder get(K chave) throws MyPickException {
        if (mapa.containsKey(chave)) {
            NoCache<K, ServiceOrder> no = mapa.get(chave);
            moverParaInicio(no);
            logCache.log("Service Order buscado e movido para o inicio",chave,getMapa());
            return no.getValor();
        }
        return null;
    }

    public void atualizar(K chave, String novoNome, String novaDescricao) {
        synchronized(Cache.class) {
            if (mapa.containsKey(chave)) {
                NoCache<K, ServiceOrder> no = mapa.get(chave);
                ServiceOrder ordem = no.getValor();

                ordem.setNome(novoNome);
                ordem.setDescricao(novaDescricao);

                moverParaInicio(no);
                logCache.log("Service Order atualizado e movido para o inicio",chave,getMapa());
            }
        }
    }

    public void remover(K chave) {
        synchronized(Cache.class) {
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
            logCache.log("Service Order removido da cache",chave,getMapa());
        }
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

    public HashMap<K, NoCache<K, ServiceOrder>> getMapa() {
        return mapa;
    }
}

