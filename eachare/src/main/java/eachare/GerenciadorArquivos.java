package eachare;

import java.util.*;

public class GerenciadorArquivos {
    private Map<ArquivoChave, List<String>> arquivosMap = new HashMap<>();

    public void adicionarArquivo(Arquivo arquivo) {
        ArquivoChave chave = new ArquivoChave(arquivo.nome, arquivo.tamanho);
        arquivosMap.putIfAbsent(chave, new ArrayList<>());

        List<String> peers = arquivosMap.get(chave);
        if (!peers.contains(arquivo.peerOrigemAddress)) {
            peers.add(arquivo.peerOrigemAddress);
        }
    }

    public void imprimirArquivos() {
        for (Map.Entry<ArquivoChave, List<String>> entry : arquivosMap.entrySet()) {
            System.out.println(entry.getKey() + ", " + String.join(" ", entry.getValue()));
        }
    }

    public List<String> listarArquivosComoStrings() {
        List<String> resultado = new ArrayList<>();
    
        for (Map.Entry<ArquivoChave, List<String>> entry : arquivosMap.entrySet()) {
            ArquivoChave chave = entry.getKey();
            List<String> peers = entry.getValue();
            String linha = chave.nome + ", " + chave.tamanho + ", " + String.join(" ", peers);
            resultado.add(linha);
        }
    
        return resultado;
    }

    public void limpar() {
        arquivosMap.clear();
    }
    
    
}
