package eachare;


public class Menu {
    
    public static int StartMenu() {
        MostrarMenu();

        int entrada = Integer.parseInt(InputReader.lerEntrada());

        return entrada;

        
        
    }
    

    public static void MostrarMenu() {
        System.out.println("Escolha um comando: \n  [1] Listar Peers\n  [2] Obter Peers\n  [3] Listar arquivos locais\n  [4] Buscar arquivos\n  [5] Exibir estátisticas\n  [6] Alterar tamanho de chunk\n  [9] Sair");
    }

    
}
