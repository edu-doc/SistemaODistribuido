package Banco;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogBancoBackup {
    private static final String path = "log_banco_backup.txt";

    public void log(String acao, No no) {
        StringBuilder arvoreString = new StringBuilder();
        List<String> elementos = new ArrayList<>();
        inOrder(no, elementos);

        for (String elemento : elementos) {
            arvoreString.append("[").append(elemento).append("] ");
        }

        String message = String.format("Ação: %s, Nó: %s, Estado da Árvore: %s%n",
                acao,
                no != null ? formatarNo(no) : "null",
                arvoreString.toString());

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            bw.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void inOrder(No no, List<String> elementos) {
        if (no != null) {
            inOrder(no.getEsquerda(), elementos);
            elementos.add(formatarNo(no));
            inOrder(no.getDireita(), elementos);
        }
    }

    private String formatarNo(No no) {
        return "ID: " + no.getOrder().getCodigoServico() +
                ", Nome: " + no.getOrder().getNome() +
                ", Descrição: " + no.getOrder().getDescricao();
    }
}
