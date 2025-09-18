package eachare;


public class Estatisticas {
    private long tempoDownloadMs;
    private int tamanhoArquivoBytes;
    private int tamanhoChunk;
    private int numeroChunks;
    private int numeroPeers;

    public Estatisticas(long tempoDownloadMs, int tamanhoArquivoBytes, int numeroChunks, int numeroPeers, int tamanhoChunk) {
        this.tempoDownloadMs = tempoDownloadMs;
        this.tamanhoArquivoBytes = tamanhoArquivoBytes;
        this.numeroChunks = numeroChunks;
        this.numeroPeers = numeroPeers;
        this.tamanhoChunk = tamanhoChunk;
    }

    public long getTempoDownloadMs() {
        return tempoDownloadMs;
    }

    public double getTempoSegundos() {
        return tempoDownloadMs / 1000.0;
    }

    public int getTamanhoArquivoBytes() {
        return tamanhoArquivoBytes;
    }

    public int getNumeroChunks() {
        return numeroChunks;
    }

    public int getNumeroPeers() {
        return numeroPeers;
    }

    public int getTamanhoChunk() {
        return tamanhoChunk;
    }

    @Override
    public String toString() {
        return String.format(
            "Tempo: %d ms | Tamanho: %d bytes | Chunks: %d | Peers: %d",
            tempoDownloadMs, tamanhoArquivoBytes, numeroChunks, numeroPeers
        );
    }
}
