package Banco;
import OrdemServico.ServiceOrder;

public class No {

    private No proximo;
    private ServiceOrder order;

    private No esquerda;
    private No direita;
    private No pai;
    private Cor cor;

    public No(ServiceOrder order) {
        this.order = order;
        this.cor = Cor.VERMELHO;
    }

    public ServiceOrder getOrder() {
        return order;
    }

    public void setOrder(ServiceOrder order) {
        this.order = order;
    }

    public No getProximo() {
        return proximo;
    }

    public void setProximo(No proximo) {
        this.proximo = proximo;
    }

    public No getEsquerda() {
        return esquerda;
    }

    public void setEsquerda(No esquerda) {
        this.esquerda = esquerda;
    }

    public No getDireita() {
        return direita;
    }

    public void setDireita(No direita) {
        this.direita = direita;
    }

    public No getPai() {
        return pai;
    }

    public void setPai(No pai) {
        this.pai = pai;
    }

    public Cor getCor() {
        return cor;
    }

    public void setCor(Cor cor) {
        this.cor = cor;
    }

    public boolean isVermelho() {
        return this.cor == Cor.VERMELHO;
    }

    public boolean isPreto() {
        return this.cor == Cor.PRETO;
    }

    @Override
    public String toString() {
        return "[" + getOrder().getCodigoServico() + "]";
    }


    public enum Cor {
        VERMELHO, PRETO
    }
}