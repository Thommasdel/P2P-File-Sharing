package eachare;

import java.io.*;
import java.util.*;

public class LerArquivo {
    
    public static List<String> lerArquivo(String caminho) {
        List<String> linhas = new ArrayList<>();
        List<Peer> peers = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                linhas.add(linha);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return linhas;
    } 



    public static void main(String[] args) {
        List<String> linhas = lerArquivo("caminho/do/arquivo.txt");
        
        // Exibindo as linhas lidas
        for (String linha : linhas) {
            System.out.println(linha);
        }
    }
}

