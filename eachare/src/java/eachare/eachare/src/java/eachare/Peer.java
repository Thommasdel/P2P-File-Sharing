package eachare;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


//Thomas Delfs 13837175

public class Peer {
    private String address;
    private int port;
    private PeerStatus status; // False = Offline; True = Online
    private List<Peer> PeersVizinhos = new ArrayList<>();
    private int clock;
    private ServerSocket serverSocket;
    private DiretorioCompartilhado diretorio;
    private CountDownLatch latch;
    private int chunk = 256;
    private final List<Arquivo> arquivosRecebidos = Collections.synchronizedList(new ArrayList<>());
    private GerenciadorArquivos gerenciadorArquivos = new GerenciadorArquivos();
    private final Map<String, CountDownLatch> latches = new ConcurrentHashMap<>(); // Latch que será usado para receber os chunks do arquivo requisitado
    private final Map<String, byte[]> chunksRecebidos = new ConcurrentHashMap<>();
    private List<Estatisticas> estatisticas = new ArrayList<>(); 
    
    

    //Construtor para casos que a lista de vizinhos já está definida
    public Peer(String address, int port, List<Peer> PeersVizinhos, DiretorioCompartilhado nomeDiretorio) {
        if (address == null) {
            throw new IllegalArgumentException("Argumento adress não pode ser nulo");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port não pode ser negativo");
        }

        this.address = address;
        this.port = port;
        this.status = PeerStatus.OFFLINE;
        this.clock = 0;
        this.diretorio = nomeDiretorio;
        

        if(PeersVizinhos == null) {
            throw new IllegalArgumentException("Lista de vizinhos não pode ser nula");
        }
        

        for(int i = 0; i < PeersVizinhos.size(); i++) {
            this.AddPeer(PeersVizinhos.get(i));
        }

    }


    //Construtor para casos onde a lista de vizinhos não está definida
    public Peer(String address, int port) {
        if (address == null) {
            throw new IllegalArgumentException("Argumento adress não pode ser nulo");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port não pode ser negativo");
        }

        this.address = address;
        this.port = port;
        this.status = PeerStatus.OFFLINE;
    }

    public Peer(String address, int port, int clock) {
        if (address == null) {
            throw new IllegalArgumentException("Argumento adress não pode ser nulo");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port não pode ser negativo");
        }

        this.clock = clock;
        this.address = address;
        this.port = port;
        this.status = PeerStatus.OFFLINE;
    }


    //Função para iniciar o Servidor
    public void StartServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                this.status = PeerStatus.ONLINE;
                System.out.println("Peer escutando na porta " + port + "...");

                while (true) {
                    Socket socket = serverSocket.accept(); // Aguarda conexões de outros peers
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                    StringBuilder mensagemBuilder = new StringBuilder();
                    String linha;
                
                    while ((linha = reader.readLine()) != null) {
                        mensagemBuilder.append(linha).append("\n");
                    }
                
                    String mensagem = mensagemBuilder.toString().trim();
                    System.out.println("Mensagem recebida: " + mensagem);
                    Peer sender = getSenderPeer(mensagem);
                
                    String comando = getMessageCommand(mensagem);
                    int clockVizinho = getMessageClock(mensagem);

                    UpdateClock(getMessageClock(mensagem));
                    sender.UpdateClock(getMessageClock(mensagem));
                
                    switch (comando) {
                        case "GET_PEERS":
                            // Peer sender = getSenderPeer(mensagem);
                            int resposta = SendPeers(sender);
                            break;
                    
                        case "PEER_LIST":
                            AddPeerList(mensagem);
                            break;
                    
                        case "BYE":
                            // Peer sender = getSenderPeer(mensagem);
                            sender.setStatus(PeerStatus.OFFLINE);
                            System.out.println("Atualizando peer " + sender.getFullAdress() + " status " + sender.GetStatus());
                            break;
                    
                        case "HELLO":
                            sender.setStatus(PeerStatus.ONLINE);
                            System.out.println("Atualizando peer " + sender.getFullAdress() + " status " + sender.GetStatus());
                            break;

                        case "LS":
                            sender.setStatus(PeerStatus.ONLINE);
                            String[] arquivos = this.diretorio.listarArquivosComTamanho();
                            enviarInfoArquivos(arquivos, sender);
                            break;

                        case "LS_LIST":
                            //Não precisa mudar status para online visto que todos os peers que responderam LS estão Online.
                            //List<Arquivo> arquivosList = new ArrayList<>();
                            processarLSList(mensagem);
                            break;
                        case "DL":
                            uploadArquivo(sender, mensagem);
                            break;
                        case "FILE":
                            onChunkRecebido(mensagem);


                    
                        default:
                            // comando desconhecido, se quiser tratar
                            break;
                    }
                    
                
                    
                    
                    socket.close();
                }
                
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }).start();
    }


    //Função para parar o funcionamento do peer
    public void stopServer() {
        this.status = PeerStatus.OFFLINE;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Função para inicializar o menu
    public int StartMenu() {
        int resposta = Menu.StartMenu();
        switch (resposta) {
            case 1:
                ListarPeers();
                break;
            case 2:
                GetPeers();
                break;
            case 3:
                diretorio.listarArquivos();
                break;
            case 4:
                try {
                    buscar();
                    break;
                } catch (Exception e) {
                    
                }
            case 5:
                imprimirTabelaEstatisticas(this.estatisticas);
                break;
            case 6:
                try{
                    lerChunk();
                    break;
                } catch(Exception e) {
                    e.printStackTrace();
                }
            

                
                break;
            case 9: 
                
                stopPeer();
                break;
            default:
                throw new AssertionError("Comando em construção. \n");


        }
        return resposta;
    }


    private String getMessageCommand(String message) {
        String termos[] = message.split(" ");

        return termos[2];
    }


    private int getMessageClock(String message) {
        String termos[] = message.split(" ");

        return Integer.parseInt(termos[1]);
    }


    private Peer getSenderPeer(String message) {

        String[] termos = message.split(" ");
        String address = termos[0].split(":")[0];
        int port = Integer.parseInt(termos[0].split(":")[1]);


        //Loop para achar o peer entre os já conhecidos
        for(int i = 0; i < this.PeersVizinhos.size(); i++) {
            Peer atual = PeersVizinhos.get(i);
            if(atual.GetAddress().equals(address) && atual.GetPort() == port) {
                atual.setStatus(PeerStatus.ONLINE);
                return atual;
            }
        }

        //Caso não encontre o Peer na lista de vizinhos, então deve adicionar ele à lista:
        Peer novo = new Peer(address, port);
        novo.setStatus(PeerStatus.ONLINE); // Visto que ele acabou de enviar uma mensagem
        this.PeersVizinhos.add(novo);
        return novo;
    }


    //Função para Listar os Peers Vizinhos
    private void ListarPeers() {

        ListarPeersVizinhos();  

        int entrada = Integer.parseInt(InputReader.lerEntrada());

        if(entrada == 0){
            return;
        }
        
        int resposta = SendMessage(this.PeersVizinhos.get(entrada-1), "HELLO");
        
    }


    //Função para Obter Peers
    private void GetPeers() {
        ListarPeersVizinhos();

        int entrada = Integer.parseInt(InputReader.lerEntrada());

        if(entrada == 0){
            return;
        }

        int resposta = SendMessage(this.PeersVizinhos.get(entrada-1), "GET_PEERS");
    }

    private void lerChunk() {

        System.out.println("Digite o novo tamanho de chunk: ");
        String entrada = InputReader.lerEntrada();

        int chunk = Integer.parseInt(entrada);

        if(chunk < 0) {
            throw new IllegalArgumentException("Chunk não pode ser negativo");
        }

        setChunk(chunk);
    }

    
    private void setChunk(int chunk) {
        this.chunk = chunk;
        System.out.println("Chunk Alterado para "+chunk);
    }


    //Função para Enviar os peers vizinhos para outro peer
    private int SendPeers(Peer vizinho) {
        
        StringBuilder peers = new StringBuilder();
        int totalVizinhos = 0;
        

        for(int i = 0; i < this.PeersVizinhos.size(); i++) {
            Peer atual = PeersVizinhos.get(i);

            //Desconsiderar o vizinho que requisitou a lista de peers.
            if(atual.getFullAdress().equals(vizinho.getFullAdress()) == false) {
                totalVizinhos++;
                peers.append(atual.GetAddress());
                peers.append(":");
                peers.append(atual.GetPort());
                peers.append(":");

                //Enviar o status do peer vizinho
                if(atual.status == PeerStatus.ONLINE) {
                    peers.append("ONLINE:");
                }
                else {
                    peers.append("OFFLINE:");
                }
                peers.append(atual.getClock()+" ");
                //peers.append(" \n");
            }

            
        }

        String message = peers.toString();

        SendMessage(vizinho,"PEER_LIST" + " " + totalVizinhos + " " + message);

        return 1;
    }


    private void stopPeer() {

        System.out.println("Saindo...");
        for(int i = 0; i < this.PeersVizinhos.size(); i++) {

            if(PeersVizinhos.get(i).status == PeerStatus.ONLINE) {

                SendMessage(PeersVizinhos.get(i), "BYE");
            }
        }
        stopServer();
        
    }


    private void ListarPeersVizinhos() {
        System.out.println("Escolha um comando: \n  [0] Sair para o menu");
        for(int i = 0; i < this.PeersVizinhos.size(); i++) {
            Peer vizinho = this.PeersVizinhos.get(i);
            int soma = i + 1;
            System.out.println("  ["+soma+"] "+vizinho.getFullAdress()+" "+vizinho.GetStatus() + " " + vizinho.getClock());
        }
    }


    private void buscar() throws InterruptedException {
        List<Peer> peersAtivos = getPeersAtivos();
    
        latch = new CountDownLatch(peersAtivos.size());
        arquivosRecebidos.clear();
        gerenciadorArquivos.limpar();
    
        for (Peer vizinho : peersAtivos) {
            SendMessage(vizinho, "LS");
        }
    
        
        latch.await();  // espera todas as respostas chegarem

        for(int i = 0; i < arquivosRecebidos.size(); i++) {
            gerenciadorArquivos.adicionarArquivo(arquivosRecebidos.get(i));
        }

        gerenciadorArquivos.imprimirArquivos();
        imprimirMenuDeArquivosFormatado();
    
        
    }


    


    private void processarLSList(String mensagem) {
        List<Arquivo> arquivos = parseArquivosDaMensagem(mensagem); // extrai os arquivos da mensagem
    
        synchronized (arquivosRecebidos) {
            arquivosRecebidos.addAll(arquivos);
        }
    
        if (latch != null) {
            latch.countDown();  // decrementa o contador para indicar que recebeu uma resposta
        }
    }


    public void imprimirMenuDeArquivosFormatado() {
        List<String> linhas = gerenciadorArquivos.listarArquivosComoStrings();

        System.out.printf("%-30s | %-8s | %s%n", "Nome", "Tamanho", "Peers");
        System.out.println("[0] Sair");

        int indice = 1;
        for (String linha : linhas) {
            // Divide a string formatada como: "nome, tamanho, peers..."
            String[] partes = linha.split(",\\s*", 3); // divide em até 3 partes
            if (partes.length < 3) continue; // ignora caso mal formatado

            String nome = partes[0];
            String tamanho = partes[1];
            String peers = partes[2];

            System.out.printf("[%d] %-26s | %-8s | %s%n", indice, nome, tamanho, peers);
            indice++;
        }

        int entrada = Integer.parseInt(InputReader.lerEntrada());
        long inicio = System.currentTimeMillis();

        // String peerEscolhido = arquivosRecebidos.get(entrada-1).getPeer();
        String linhaEscolhida = linhas.get(entrada-1);

        String partes[] = linhaEscolhida.split(",\\s");
        String nome = partes[0];
        int tamanho = Integer.parseInt(partes[1]);
        String peers[] = partes[2].split(" ");

        int qntChunks = ((tamanho + this.chunk - 1) / this.chunk);
        int sobra = tamanho % this.chunk;
        int qntPeers = peers.length;

        ExecutorService executor = Executors.newFixedThreadPool(qntPeers);
        List<Future<byte[]>> resultados = new ArrayList<>();

        for (int i = 0; i < qntChunks; i++) {
            final int chunkIndex = i;
            final String peerResponsavel = peers[chunkIndex % qntPeers];
            final int tamanhoChunkAtual = (i == qntChunks - 1 && sobra != 0) ? sobra : this.chunk;
        
            Future<byte[]> futuro = executor.submit(() -> requisitarChunk(peerResponsavel, partes[0], tamanhoChunkAtual, chunkIndex));
            resultados.add(futuro);
        }

        // Aguardar todos os chunks serem recebidos
        List<byte[]> chunks = new ArrayList<>();
        for (Future<byte[]> futuro : resultados) {
            try {
                byte[] chunk = futuro.get();
                chunks.add(chunk);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace(); // ou trate de outra forma
            }
        }


        executor.shutdown();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long fim = System.currentTimeMillis();
        long tempoTotal = fim - inicio; // em milissegundos
        Estatisticas estatistica = new Estatisticas(tempoTotal, tamanho, qntChunks, qntPeers, this.chunk);
        this.estatisticas.add(estatistica);
        System.out.println("Download do arquivo " + partes[0] + " finalizado com sucesso.");

        try {
            for (byte[] chunk : chunks) {
                output.write(chunk);
            }

            byte[] dadosCompletos = output.toByteArray();
            this.diretorio.salvarArquivo(dadosCompletos, nome);
        }
        catch(IOException e) {
            e.printStackTrace();
        }


    }


    

    byte[] requisitarChunk(String peer, String nomeArquivo, int tamanhoChunk, int indexChunk) throws IOException{
        try {
            String chave = nomeArquivo + "-" + indexChunk;
            CountDownLatch latch = new CountDownLatch(1);
            latches.put(chave, latch);

            Peer peerDestino = searchPeerAddress(peer);

            // Envia a requisição
            SendMessage(peerDestino, "DL " + nomeArquivo + " " + tamanhoChunk + " " + indexChunk);

            // Espera até o chunk ser recebido ou timeout
            boolean recebido = latch.await(10, TimeUnit.SECONDS);
            latches.remove(chave); // limpar após uso

            if (!recebido) {
                System.out.println("Timeout aguardando chunk " + chave);
                throw new IOException("Timeout");
            }

            byte[] dados = chunksRecebidos.remove(chave); // remove após uso
            return dados != null ? dados : new byte[0];

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new byte[0];
        }
    }
    
    public void imprimirTabelaEstatisticas(List<Estatisticas> lista) {
        record Chave(int chunkSize, int numPeers, int arquivoSize) {}

        Map<Chave, List<Estatisticas>> agrupados = new HashMap<>();

        // Agrupa estatísticas com os mesmos parâmetros
        for (Estatisticas e : lista) {
            Chave chave = new Chave(e.getTamanhoChunk(), e.getNumeroPeers(), e.getTamanhoArquivoBytes());
            agrupados.computeIfAbsent(chave, k -> new ArrayList<>()).add(e);
        }

        // Cabeçalho da tabela
        System.out.printf("%-11s | %-8s | %-13s | %-3s | %-10s | %s%n",
                        "Tam. chunk", "N peers", "Tam. arquivo", "N", "Tempo [s]", "Desvio");

        // Para cada grupo, calcula média e desvio padrão
        for (Map.Entry<Chave, List<Estatisticas>> entry : agrupados.entrySet()) {
            Chave chave = entry.getKey();
            List<Estatisticas> grupo = entry.getValue();
            int n = grupo.size();

            double media = grupo.stream()
                .mapToDouble(Estatisticas::getTempoSegundos)
                .average().orElse(0.0);

            double desvio = 0.0;
            if (n > 1) {
                double somaQuadrados = grupo.stream()
                    .mapToDouble(e -> Math.pow(e.getTempoSegundos() - media, 2))
                    .sum();
                desvio = Math.sqrt(somaQuadrados / n);
            }

            System.out.printf("%-11d | %-8d | %-13d | %-3d | %-10.5f | %.5f%n",
                            chave.chunkSize(), chave.numPeers(), chave.arquivoSize(),
                            n, media, desvio);
        }
    }



    void onChunkRecebido(String mensagem) {
        String partes[] = mensagem.split(" ");
        String nomeArquivo = partes[3];
        int tamanhoChunk = Integer.parseInt(partes[4]);
        int indexChunk = Integer.parseInt(partes[5]);
        String conteudo = partes[6];
        byte[] dados = Base64.getDecoder().decode(conteudo); 

        String chave = nomeArquivo + "-" + indexChunk;
        chunksRecebidos.put(chave, dados);

        CountDownLatch latch = latches.get(chave);
        if (latch != null) {
            latch.countDown();
        }
    }

    


    
    
    



    private List<Arquivo> parseArquivosDaMensagem(String mensagem) {
        String[] partes = mensagem.split(" ");
        List <Arquivo> arquivos = new ArrayList<>();

        String peerOrigemAddress = partes[0];
        int totalArquivos = Integer.parseInt(partes[3]);
        
        for(int i = 0; i < totalArquivos; i++) {
            String infos = partes[4 + i];
            String nome = infos.split(":")[0];
            String tamanho = infos.split(":")[1];
            Arquivo arquivo = new Arquivo(nome, Integer.parseInt(tamanho), peerOrigemAddress);
            arquivos.add(arquivo);
        }

        return arquivos;
    }


    private void enviarInfoArquivos(String[] arquivos, Peer destino) {
        StringBuilder mensagem = new StringBuilder();

        int totalArquivos = arquivos.length;

        for(int i = 0; i < totalArquivos; i++) {
            mensagem.append(arquivos[i]);
            mensagem.append(" ");
        }

        String finalMessage = mensagem.toString();

        SendMessage(destino, "LS_LIST " + totalArquivos + " " + finalMessage);
    }


    //Funçao para adicionar peers na lista de vizinhos
    public final void AddPeer(Peer peer) {
        this.PeersVizinhos.add(peer);
        System.out.println("Adicionando novo peer " + peer.GetAddress() + ":" + peer.GetPort() + " status " + peer.GetStatus());
    }


    private void AddPeerList(String peerList) {
        String[] termos = peerList.split(" ");

        int totalPeers = Integer.parseInt(termos[3]);

        for(int i = 0; i < totalPeers; i++) {
            StringBuilder fullAddress = new StringBuilder();

            //Dividir cada linha da mensagem
            String address = termos[4+i].split(":")[0];
            int port = Integer.parseInt(termos[4+i].split(":")[1]);
            String status = termos[4+i].split(":")[2];
            int clock = Integer.parseInt(termos[4+i].split(":")[3]);

            fullAddress.append(address);
            fullAddress.append(":");
            fullAddress.append(port);

            String finalAddress = fullAddress.toString();
            Peer peerVizinho = searchPeerAddress(finalAddress);
             

            //Verifica se o endereço já não existe na lista dos vizinhos, caso não exista, adicionar nela
            if(peerVizinho == null) {
                Peer newPeer = new Peer(address, port, clock);
                if(status.equals("ONLINE")) {
                    newPeer.setStatus(PeerStatus.ONLINE);
                }
                AddPeer(newPeer);
            }
            else {
                if(clock > peerVizinho.getClock()) {
                    peerVizinho.setClock(clock);
                    if(!peerVizinho.GetStatus().toString().equals(status)) {
                        if(status.equals("ONLINE")) {
                            peerVizinho.setStatus(PeerStatus.ONLINE);
                            System.out.println("Atualizando peer "+ peerVizinho.getFullAdress() + " status " + peerVizinho.GetStatus());
                        }
                        else {
                            peerVizinho.setStatus(PeerStatus.OFFLINE);
                            System.out.println("Atualizando peer "+ peerVizinho.getFullAdress() + " status " + peerVizinho.GetStatus());
                        }
                    }
                }
            }
            
            

        }

    }


    private Peer searchPeerAddress(String address) {
        for(int i = 0; i < this.PeersVizinhos.size(); i++) {
            if(this.PeersVizinhos.get(i).getFullAdress().equals(address)) {
                return this.PeersVizinhos.get(i);
            }
        }

        return null;
    }


    private void uploadArquivo(Peer sender, String mensagem) {
        String[] partes = mensagem.split(" ");
        String nomeArquivo = partes[3];
        int chunk = Integer.parseInt(partes[4]);
        int index = Integer.parseInt(partes[5]);

        List<String> base64 = this.diretorio.getArquivoEmChunksBase64(nomeArquivo, chunk);

        SendMessage(sender, "FILE " + nomeArquivo + " " + chunk + " " + index + " " + base64.get(index));

    }    

    private void UpdateClock(int clock_mensagem) {
        int novo_clock = Math.max(this.clock, clock_mensagem);
        this.clock = novo_clock + 1;
        System.out.println("=> Atualizando clock de " + this.getFullAdress() + " para: " + this.clock);
        
    }


    private void setClock(int newClock) {
        this.clock = newClock;
    }


    // Função responsável por enviar mensagem a outro peer
    public int SendMessage(Peer vizinho, String message) {
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("Endereço IP não pode ser nulo ou vazio");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("Porta do destino não pode ser negativa ou zero");
        }

        String address = vizinho.GetAddress();
        int port = vizinho.GetPort();

        try (Socket socket = new Socket(address, port)) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            UpdateClock(0); //Atualiza o clock ANTES do envio da mensagem
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append(this.getFullAdress() + " " + this.getClock() + " " + message);
            String finalMessage = messageBuilder.toString();
            
            writer.println(finalMessage);


            System.out.println("Encaminhando mensagem para " + address + ":" + port + " " + getMessageCommand(finalMessage));
            if(vizinho.GetStatus() == PeerStatus.OFFLINE) {
                
                vizinho.setStatus(PeerStatus.ONLINE);
                System.out.println("Atualizando peer "+ vizinho.getFullAdress() + " status " + vizinho.GetStatus());
            }
            
            return 1;
            
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem para " + address + ":" + port);
            if(vizinho.status == PeerStatus.OFFLINE) {
                
                vizinho.setStatus(PeerStatus.OFFLINE);
                System.out.println("Atualizando peer "+ vizinho.getFullAdress() + " status " + vizinho.GetStatus());
            }
            // e.printStackTrace();
            return -1;
        }

    }


    public String GetAddress() {
        return this.address;
    }

    public String getFullAdress() {
        String resposta = this.address + ":" + this.port;
        return resposta;
    }


    public int GetPort() {
        return this.port;
    }


    public PeerStatus GetStatus() {
        return this.status;
    }


    public List<Peer> getPeersAtivos() {
        List<Peer> peersAtivos = new ArrayList<>();

        for(int i = 0; i < this.PeersVizinhos.size(); i++) {
            Peer vizinho = this.PeersVizinhos.get(i);

            if(vizinho.status.toString().equals("ONLINE")) {
                peersAtivos.add(vizinho);
            }
        }

        return peersAtivos;
    }




    public int getClock() {
        return this.clock;
    }


    public void setStatus(PeerStatus status) {
        this.status = status;
        
    }

    @Override
    public String toString() {
        String mensagem = String.format("endereço: %s \nstatus: %s", this.address, this.status);

        return mensagem;
    }
}