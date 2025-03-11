package Banco;

import java.util.ArrayList;
import java.util.List;

public class Banco {

    private No raiz;
    public static final Banco instancia = new Banco();

    public Banco() {
        this.raiz = null;
    }

    public  void inserir(No no) {
        synchronized(Banco.class) {
            raiz = inserirRN(raiz, no);
            raiz.setCor(No.Cor.PRETO);
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
        return buscarRN(raiz, chave);
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
                System.out.println("Service Order atualizada: " + codigoServico);
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
}
