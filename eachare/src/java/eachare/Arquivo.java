package eachare;

public class Arquivo {
    String nome;
    int tamanho;
    String peerOrigemAddress;

    public Arquivo(String nome, int tamanho, String peerOrigem) {
        this.nome = nome;
        this.tamanho = tamanho;
        this.peerOrigemAddress = peerOrigem;
    }

    public String getNome() {
        return this.nome;
    }

    public int getTamanho() {
        return this.tamanho;
    }

    public String getInfo() {
        return this.nome + ":" + this.tamanho + ":" + this.peerOrigemAddress;
    }


    public String getPeer() {
        return this.peerOrigemAddress;
    }
}
