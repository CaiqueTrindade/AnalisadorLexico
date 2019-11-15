import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Principal {

    public static void main(String[] args) {

        File input_diretorio = new File("input");

        // Verifica se a pasta "input" existe no diretório do projeto
        if (input_diretorio.exists()) {

            // Verifica se a pasta "input" possui arquivos
            if (input_diretorio.listFiles().length > 0) {

                List<File> entradas_validas = new ArrayList();

                // Varre a lista de arquivos da pasta "input" em busca de arquivos de entrada válidos, isto é, que estejam dentro do padrão de entrada
                for (File input: input_diretorio.listFiles())
                    if (input.isFile() && input.getName().matches("^(entrada)\\d+(.txt)$"))
                        entradas_validas.add(input);

                // Se houver arquivos válidos
                if (entradas_validas.size() > 0) {

                    System.out.println("Arquivos válidos de entrada encontrados: " + entradas_validas.size());

                    // Percorre a lista das entradas válidas
                    Iterator it = entradas_validas.iterator();
                    while (it.hasNext()) {
                        File input = (File) it.next();
                        System.out.println("Lendo e analisando lexicalmente o arquivo: " + input.getName());
                        AnalisadorLexico al = null;
                        try {
                            // Passa o arquivo de entrada para a classe que fará a leitura e a análise léxica do código-fonte
                            al = new AnalisadorLexico(input);
                            // Solicita que o AnalisadorLexico faça a análise
                            al.executarAnalise();
                            // Solicita que o AnalisadorLexico faça a escrita do arquivo de saída com os tokens e os erros encontrados
                            al.escreverEmArquivo();
                        } catch (FileNotFoundException e) {
                            System.err.println("Arquivo de entrada não encontrado.");
                            e.printStackTrace();
                        } catch (IOException e) {
                            System.err.println("Falha na leitura do arquivo de entrada ou na escrita do arquivo de saída.");
                            e.printStackTrace();
                        }
                    }
                }
                else
                    System.out.print("Não foram encontrados arquivos válidos na pasta de entrada \"input\".");
            }
            else
                System.out.print("A pasta de entrada \"input\" está vazia.");
        }
        else
            System.err.print("A pasta de entrada \"input\" não foi localizada no diretório do projeto.");
    }
}