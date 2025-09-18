package eachare;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class DiretorioCompartilhado {
    private String directoryPath;

    public DiretorioCompartilhado(String directoryPath) {
        this.directoryPath = directoryPath;
        initialize();
    }

    private void initialize() {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                System.out.println("Diretório de arquivos compartilhados criado: " + directoryPath);
            } else {
                System.err.println("Falha ao criar o diretório.");
            }
        }
    }


    public String getNomeDiretorio() {
        File directory = new File(directoryPath);
        return directory.getName();
    }

    public void listarArquivos() {
    File directory = new File(directoryPath);
    if (directory.exists() && directory.isDirectory()) {
        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            System.out.println("Arquivos no diretório " + directoryPath + ":");
            for (File file : files) {
                System.out.println("- " + file.getName());
            }
        } else {
            System.out.println("O diretório está vazio.");
        }
        } else {
            System.err.println("O diretório não existe ou não é um diretório válido.");
        }
    }


    public String[] listarArquivosComTamanho() {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                String[] resultado = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    String nome = files[i].getName();
                    long tamanho = files[i].length(); // tamanho em bytes
                    resultado[i] = nome + ":" + tamanho;
                }
                return resultado;
            }
        }
        return new String[0]; // retorna array vazio se não houver arquivos
    }


    public String getArquivoComoBase64(String nomeArquivo) {
        File arquivo = new File(directoryPath, nomeArquivo);
        if (!arquivo.exists() || !arquivo.isFile()) {
            System.err.println("Arquivo não encontrado: " + nomeArquivo);
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(arquivo.toPath());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            return null;
        }
    }

    public List<byte[]> dividirEmChunks(byte[] dados, int tamanhoChunk) {
        List<byte[]> chunks = new ArrayList<>();
        int offset = 0;

        while (offset < dados.length) {
            int tamanhoAtual = Math.min(tamanhoChunk, dados.length - offset);
            byte[] chunk = Arrays.copyOfRange(dados, offset, offset + tamanhoAtual);
            chunks.add(chunk);
            offset += tamanhoAtual;
        }

        return chunks;
    }

    public List<String> getArquivoEmChunksBase64(String nomeArquivo, int tamanhoChunk) {
        File arquivo = new File(directoryPath, nomeArquivo);
        if (!arquivo.exists() || !arquivo.isFile()) {
            System.err.println("Arquivo não encontrado: " + nomeArquivo);
            return null;
        }
    
        try {
            byte[] bytes = Files.readAllBytes(arquivo.toPath());
            List<String> chunksBase64 = new ArrayList<>();
    
            int offset = 0;
            while (offset < bytes.length) {
                int tamanhoAtual = Math.min(tamanhoChunk, bytes.length - offset);
                byte[] chunk = Arrays.copyOfRange(bytes, offset, offset + tamanhoAtual);
                String chunkBase64 = Base64.getEncoder().encodeToString(chunk);
                chunksBase64.add(chunkBase64);
                offset += tamanhoAtual;
            }
    
            return chunksBase64;
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            return null;
        }
    }

    public void salvarArquivo(byte[] dados, String nomeArquivo) throws IOException {
        Path caminho = Paths.get(this.directoryPath, nomeArquivo);
        Files.write(caminho, dados);
    }


    public static void main(String[] args) {
        String directoryName = (args.length > 0) ? args[0] : "shared_files";
        new DiretorioCompartilhado(directoryName);
    }
}
