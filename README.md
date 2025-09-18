# Eachare  P2P-File-Sharing - Projeto de Sistemas Distribuídos

Este projeto foi desenvolvido como parte da disciplina **Desenvolvimento de Sistemas Distribuídos** da faculdade. Ele implementa um sistema de compartilhamento de arquivos entre peers em Java, permitindo que múltiplos nós se comuniquem, compartilhem e baixem arquivos de forma distribuída.

## Estrutura do Projeto

- Código-fonte: `eachare/src/main/java/eachare/`
- Arquivo principal: `Main.java`
- Classes auxiliares: todas as classes dentro do pacote `eachare`

## Compilação

Para compilar o código, execute o seguinte comando no terminal na raiz do projeto:

```bash
javac eachare/src/main/java/eachare/*.java
```

## Execução

O programa deve ser iniciado no terminal utilizando o comando:

java eachare.Main <endereço:porta> <vizinhos.txt> <diretorio_compartilhado>

- <endereço:porta>: indica o endereço e a porta do peer atual.  
- <vizinhos.txt>: arquivo contendo os peers vizinhos (endereço:porta) que o peer conhece.  
- <diretorio_compartilhado>: diretório onde os arquivos compartilhados serão armazenados.

Exemplo:

java eachare.Main 127.0.0.1:5000 vizinhos.txt shared_files
