import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ErroSintatico {
    private String nTerminal;
    private String mensagem;
    private int nLinha;



    /**
     * Construtor da classe Erro
     * @param nTerminal lexema que possui um erro léxico
     * @param mensagem linha onde o lexema está localizado
     * @param nLinha tipo do erro léxico
     */
    public ErroSintatico(String nTerminal, String mensagem, int nLinha) {
        this.nTerminal = nTerminal;
        this.mensagem = mensagem;
        this.nLinha = nLinha;

    }

    /**
     * Método get do atributo Lexema.
     * @return o lexema do erro.
     */
    public String getnTerminal() {
        return this.nTerminal;
    }

    /**
     * Método get do atributo nLinha.
     * @return a linha onde está localizado o lexema.
     */
    public int getnLinha() {
        return this.nLinha;
    }

    /**
     * Método get do atributo Tipo.
     * @return o tipo do erro léxico (representação numérica).
     */
    public String getMensagem() {
        return this.mensagem;
    }

    /**
     * Método toString da classe Erro.
     * @return String no padrão de saída da lista de erros usado pelo Analisador Léxico.
     */
    public String toString(){
        return nLinha + " " + nTerminal+ " " +mensagem+";\n";
    }

}
