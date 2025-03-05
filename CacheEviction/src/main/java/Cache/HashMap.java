package Cache;

public class HashMap<K, V> {
    private static final int CAPACIDADE_INICIAL = 16;
    private static final float FATOR_CARGA = 0.75f;

    class Entry<K, V> {
        K chave;
        V valor;
        Entry<K, V> proximo;

        public Entry(K chave, V valor) {
            this.chave = chave;
            this.valor = valor;
            this.proximo = null;
        }
    }

    private Entry<K, V>[] tabela;
    private int tamanho;

    public HashMap() {
        this.tabela = new Entry[CAPACIDADE_INICIAL];
        this.tamanho = 0;
    }

    private int calcularIndice(K chave) {
        if (chave == null) {
            return 0;
        }
        return Math.abs(chave.hashCode() % tabela.length);
    }

    public void put(K chave, V valor) {
        int indice = calcularIndice(chave);
        Entry<K, V> entrada = tabela[indice];

        while (entrada != null) {
            if (entrada.chave.equals(chave)) {
                entrada.valor = valor;
                return;
            }
            entrada = entrada.proximo;
        }

        Entry<K, V> novaEntrada = new Entry<>(chave, valor);
        novaEntrada.proximo = tabela[indice];
        tabela[indice] = novaEntrada;
        tamanho++;

        if ((float) tamanho / tabela.length > FATOR_CARGA) {
            redimensionar();
        }
    }

    public V get(K chave) {
        int indice = calcularIndice(chave);
        Entry<K, V> entrada = tabela[indice];

        while (entrada != null) {
            if (entrada.chave.equals(chave)) {
                return entrada.valor;
            }
            entrada = entrada.proximo;
        }

        return null;
    }

    public V remove(K chave) {
        int indice = calcularIndice(chave);
        Entry<K, V> entrada = tabela[indice];
        Entry<K, V> anterior = null;

        while (entrada != null) {
            if (entrada.chave.equals(chave)) {
                if (anterior == null) {
                    tabela[indice] = entrada.proximo;
                } else {
                    anterior.proximo = entrada.proximo;
                }
                tamanho--;
                return entrada.valor;
            }
            anterior = entrada;
            entrada = entrada.proximo;
        }
        return null;
    }


    private void redimensionar() {
        Entry<K, V>[] tabelaAntiga = tabela;
        tabela = new Entry[tabelaAntiga.length * 2];
        tamanho = 0;

        for (Entry<K, V> entrada : tabelaAntiga) {
            while (entrada != null) {
                put(entrada.chave, entrada.valor);
                entrada = entrada.proximo;
            }
        }
    }

    public boolean containsKey(K chave) {
        return get(chave) != null;
    }

    public int size() {
        return tamanho;
    }

    public void imprimir() {
        for (int i = 0; i < tabela.length; i++) {
            System.out.print("Índice " + i + ": ");
            Entry<K, V> entrada = tabela[i];
            while (entrada != null) {
                System.out.print("[" + entrada.chave + "=" + entrada.valor + "] -> ");
                entrada = entrada.proximo;
            }
            System.out.println("null");
        }
    }

    public Entry<K,V>[] getTabela() {
        return tabela;
    }
}