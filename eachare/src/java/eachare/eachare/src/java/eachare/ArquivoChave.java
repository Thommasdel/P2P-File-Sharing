package eachare;

import java.util.Objects;

public class ArquivoChave {
    String nome;
    int tamanho;

    public ArquivoChave(String nome, int tamanho) {
        this.nome = nome;
        this.tamanho = tamanho;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArquivoChave)) return false;
        ArquivoChave that = (ArquivoChave) o;
        return tamanho == that.tamanho && nome.equals(that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, tamanho);
    }

    @Override
    public String toString() {
        return nome + ", " + tamanho;
    }
}
