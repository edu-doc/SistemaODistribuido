package Banco;
import Cache.NoCache;
import OrdemServico.ServiceOrder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Log<K> {
    private static final String path = "log.txt";

    public void log(String acao, NoCache<K, ServiceOrder> no, K chave, HashMap<K, NoCache<K, ServiceOrder>> mapa) {
        StringBuilder tabelaString = new StringBuilder();

        for (NoCache<K, ServiceOrder> entrada : mapa.values()) {
            tabelaString.append("[").append(entrada.getValor().getCodigoServico()).append("] ");
        }

        String message = String.format("Ação: %s, Chave: %s, Tabela de Cache: %s%n",
                acao,
                chave != null ? chave.toString() : "null",
                tabelaString.toString());

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            bw.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
