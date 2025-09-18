package eachare;

import java.util.Scanner;

public class InputReader {
    private static final Scanner scanner = new Scanner(System.in); // Reutilizar a instância

    public static String lerEntrada() {
        // System.out.print("Digite algo: ");
        if (scanner.hasNextLine()) { // Verifica se há entrada disponível
            return scanner.nextLine();
        }
        return ""; // Retorna vazio se não houver entrada
    }

    public static void main(String[] args) {
        String entrada = lerEntrada();
        System.out.println("Você digitou: " + entrada);
    }
}
