import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe que representa um erro proveniente da análise léxica realizada pela classe AnalisadorLexico.
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */
public class Erro {

    private String lexema;                                  // Lexema que contém o erro
    private int nLinha;                                     // Linha onde o lexema se encontra
    private int tipo;                                       // Tipo do erro (lexema com má formação)
    private static final Map<Integer, String> idTokens;     // HashMap estático dos possíveis tipos de erro

    // Inserção dos possíveis tipos de erro no HashMap de forma estática
    static {
        Map<Integer, String> temp = new HashMap<Integer, String>();
        temp.put(1, "Número Real");
        temp.put(2, "Número Inteiro");
        temp.put(3, "Identificador");
        temp.put(4, "Operador lógico");
        temp.put(10, "Delimitador de Comentário");
        temp.put(11, "Cadeia de Caracteres");
        idTokens = Collections.unmodifiableMap(temp);
    }

    /**
     * Construtor da classe Erro
     * @param lexema lexema que possui um erro léxico
     * @param nLinha linha onde o lexema está localizado
     * @param tipo tipo do erro léxico
     */
    public Erro(String lexema, int nLinha, int tipo) {
        this.lexema = lexema;
        this.nLinha = nLinha;
        this.tipo = tipo;

    }

    /**
     * Método get do atributo Lexema.
     * @return o lexema do erro.
     */
    public String getLexema() {
        return lexema;
    }

    /**
     * Método get do atributo nLinha.
     * @return a linha onde está localizado o lexema.
     */
    public int getnLinha() {
        return nLinha;
    }

    /**
     * Método get do atributo Tipo.
     * @return o tipo do erro léxico (representação numérica).
     */
    public int getTipo() {
        return tipo;
    }

    /**
     * Método toString da classe Erro.
     * @return String no padrão de saída da lista de erros usado pelo Analisador Léxico.
     */
    public String toString(){
        return nLinha + " " + lexema + " " + "Mal formação de " +this.idToToken(tipo) + ";\n";
    }

    /**
     * Método responsável por retornar o tipo do erro de forma nominal.
     * @param id representação númerica (identificador) do tipo de erro
     */
    public static String idToToken(int id){
        return idTokens.get(id);
    }
}
