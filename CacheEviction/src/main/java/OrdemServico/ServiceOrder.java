package OrdemServico;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServiceOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    static int count = 0000;
    private int codigoServico;
    private String nome;
    private String descricao;

    private LocalDateTime data = LocalDateTime.now();

    private String hora;

    public ServiceOrder(String nome, String descricao){
        DateTimeFormatter horaFormatada = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.codigoServico = count;
        setNome(nome);
        setDescricao(descricao);
        setHora(horaFormatada.format(data));
        increCount();
    }

    public static int increCount(){
        return ++count;
    }

    public static int getCount() {
        int c = count;
        ServiceOrder.increCount();
        return c;
    }

    public int getCodigoServico(){
        return codigoServico;
    }

    public String getNome(){
        return nome;
    }

    public String getDescricao(){
        return descricao;
    }

    public String getHora(){
        return hora;
    }

    public static void setCount(int count) {
        ServiceOrder.count = count;
    }

    public void setCodigoServico(int codigoServico) {
        this.codigoServico = codigoServico;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    // public DateTimeFormatter getHoraFormatada() {
    //    return horaFormatada;
    //}

    // public void setHoraFormatada(DateTimeFormatter horaFormatada) {
    //    this.horaFormatada = horaFormatada;
    //}

    public void setHora(String hora) {
        this.hora = hora;
    }

    @Override
    public String toString() {
        return "ServiceOrder [Codigo de Serviço=" + getCodigoServico() + ", Nome=" + getNome() + ", Descrição=" + getDescricao()
                + ", Hora=" + getHora() + "]";
    }

    

}
