public class ErroSemantico {


    private String significado;
    private String mensagem;
    private int nLinha;



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
