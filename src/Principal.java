import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Classe principal do projeto do Analisador Léxico.
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */
public class Principal {

    /**
     * Método principal (main) do programa. É responsável por buscar entradas válidas na pasta "input" do projeto,
     * arquivos que estejam dentro do padrão de entrada especificado pelo problema (entrada[0-9]+.txt), e submetê-las
     * ao processo de análise léxica realizado pela classe "AnalisadorLexico". Além disso, ao longo da execução, informa
     * pelo terminal da aplicação os processos que estão sendo realizados.
     */
    public static void main(String[] args) {

        File input_diretorio = new File("input");

        // Verifica se a pasta "input" existe no diretório do projeto
        if (input_diretorio.exists()) {

            // Verifica se a pasta "input" possui arquivos
            if (input_diretorio.listFiles().length > 0) {

                List<File> entradas_validas = new ArrayList();

                // Varre a lista de arquivos da pasta "input" em busca de arquivos de entrada válidos, isto é, que estejam dentro do padrão de entrada, e os armaezena em uma estrutura de dados
                for (File input: input_diretorio.listFiles())
                    if (input.isFile() && input.getName().matches("^entrada[0-9]+\\.txt$"))
                        entradas_validas.add(input);

                // Se houver arquivos válidos
                if (entradas_validas.size() > 0) {
                    System.out.println("Arquivos válidos de entrada encontrados: " + entradas_validas.size() + "\n");

                    // Percorre a lista das entradas válidas
                    Iterator it = entradas_validas.iterator();
                    while (it.hasNext()) {
                        File input = (File) it.next();
                        System.out.println("Lendo e analisando o arquivo: " + input.getName());
                        AnalisadorLexico al = null;
                        AnalisadorSintatico as = null;
                        try {
                            // Passa o arquivo de entrada para a classe que fará a leitura e a análise léxica do código-fonte
                            al = new AnalisadorLexico(input);
                            // Solicita que o AnalisadorLexico faça a análise
                            al.executarAnalise();
                            // Solicita que o AnalisadorLexico faça a escrita do arquivo de saída com os tokens e os erros encontrados
                            al.escreverEmArquivo();

                            if(al.errosIsVazio())
                                System.out.print("O arquivo "+ input.getName() +" não possui erros léxicos! ");
                            System.out.println("Análise léxica feita com sucesso!");

                            // Passa a lista de tokens provenientes da análise léxica para o analisador sintático
                            as = new AnalisadorSintatico(al.getTokens());
                            // Solicita que o AnalisadorSintatico faça a análise
                            as.executarAnalise();
                            // Solicita que o AnalisadorSintatico faça a escrita do arquivo de saída
                            as.escreverEmArquivo(input.getName());

                            if(as.errosIsVazio())
                                System.out.print("O arquivo "+ input.getName() +" não possui erros sintáticos! ");
                            System.out.println("Análise sintática feita com sucesso!");

                            System.out.println("Leitura do arquivo " + input.getName() + " finalizada!\n\n");
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