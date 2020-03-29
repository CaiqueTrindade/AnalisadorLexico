
/**
 * Classe que representa um erro proveniente da análise semântica realizada pela classe AnalisadorLexico.
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */

public class ErroSemantico {


    private String significado; //Significado princial do erro
    private String mensagem; //Mensagem principal do erro, contendo o lexema do erro
    private int nLinha; //Indica a linha onde ocorreu o erro



    /**
     * Construtor da classe Erro
     * @param significado possui o significado do erro
     * @param mensagem linha onde o lexema está localizado
     * @param nLinha tipo do erro léxico
     */
    public ErroSemantico(String significado, String mensagem, int nLinha) {
        this.significado = significado;
        this.mensagem = mensagem;
        this.nLinha = nLinha;

    }

    /**
     * Método get do atributo Lexema.
     * @return o lexema do erro.
     */
    public String getnTerminal() {
        return this.significado;
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
        return nLinha + " " +significado + " : " +mensagem+";\n";
    }

}
