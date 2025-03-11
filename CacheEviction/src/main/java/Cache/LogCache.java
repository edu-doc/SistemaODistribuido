package Cache;

import OrdemServico.ServiceOrder;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class LogCache<K> {
    private static final String path = "log_cache.txt";

    public void log(String acao, K chave, HashMap<K, NoCache<K, ServiceOrder>> mapa) {
        StringBuilder tabelaString = new StringBuilder();

        for (NoCache<K, ServiceOrder> entrada : mapa.values()) {
            tabelaString.append(formatarOrdemServico(entrada.getValor())).append(" ");
        }

        String message = String.format("Ação: %s, Chave: %s, Estado da Cache: %s%n",
                acao,
                chave != null ? chave.toString() : "null",
                tabelaString.toString());

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            bw.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatarOrdemServico(ServiceOrder order) {
        return String.format("[ID: %s, Nome: %s, Descrição: %s]",
                order.getCodigoServico(),
                order.getNome(),
                order.getDescricao());
    }
}
