package Cache;

import OrdemServico.ServiceOrder;

public class NoCache<K, V> {
    private K chave;
    private ServiceOrder valor;
    private NoCache<K, V> anterior;
    private NoCache<K, V> proximo;

    public NoCache(K chave, ServiceOrder serviceOrder) {
        this.chave = chave;
        this.valor = serviceOrder;
        this.anterior = null;
        this.proximo = null;
    }

    public K getChave() {
        return chave;
    }

    public void setChave(K chave) {
        this.chave = chave;
    }

    public ServiceOrder getValor() {
        return valor;
    }

    public void setValor(ServiceOrder valor) {
        this.valor = valor;
    }

    public NoCache<K,V> getAnterior() {
        return anterior;
    }

    public void setAnterior(NoCache<K,V> anterior) {
        this.anterior = anterior;
    }

    public NoCache<K,V> getProximo() {
        return proximo;
    }

    public void setProximo(NoCache<K,V> proximo) {
        this.proximo = proximo;
    }
}