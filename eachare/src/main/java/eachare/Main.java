package eachare;

import java.util.ArrayList;
import java.util.List;

//Thomas Delfs 13837175

public class Main {
    public static void main(String[] args) {
        
        if (args.length != 3) {
            System.out.println(args.length);
            System.out.println("Uso: java Main <endereço:porta> <vizinhos.txt> <diretorio_compartilhado>");
            // Menu menu = new Menu();

            return;
        }

        String fullAddress = args[0];
        List<String> vizinhos;
        vizinhos = LerArquivo.lerArquivo(args[1]);

        String[] parts =  fullAddress.split(":");
        System.out.println(parts[0] + parts[1]);
        
        int port = Integer.parseInt(parts[1]);
        String adress = parts[0];
        List<Peer> peersVizinhos = new ArrayList<>();

        for(int i = 0; i < vizinhos.size(); i++) {
            parts =  vizinhos.get(i).split(":");
            int vizinhoPort = Integer.parseInt(parts[1]);
            String vizinhoAdress = parts[0];

            Peer novoVizinho = new Peer(vizinhoAdress, vizinhoPort);
            peersVizinhos.add(novoVizinho);
        }
        int entrada = 0;

        DiretorioCompartilhado diretorio = new DiretorioCompartilhado(args[2]);
        Peer peer = new Peer(adress, port, peersVizinhos, diretorio);
        peer.StartServer();
        
        while(entrada != 9)
        {
            try {
                entrada = peer.StartMenu();   
            } catch (Exception e) {
                e.printStackTrace();
            }
             
        }
       


        // Peer peer = new Peer();


    }
}