import java.util.HashMap;

/***
 * Essa classe representa a tabela de simbolos que será utilizada para armazenar os identificadores
 * do código fonte utilizado na análise semântica
 */
public class TabelaSimbolos {

    private HashMap<String, Simbolo> tabela;

    public TabelaSimbolos(){
        tabela = new HashMap<String, Simbolo>();
    }

    /***
     * Verifica a existência de um identificador na tabela, este caso para variáveis e constantes
     * @param identificador a chave de busca
     * @return true se o objeto estiver presente na tabela ou false caso contrário
     */
    public boolean buscar(String identificador){
        return tabela.containsKey(identificador);
    }

    /***
     * Verifica a existência de um identificador na tabela, este caso para funções e procedimentos
     * @param simbolo a chave de busca
     * @return true se o objeto estiver presente na tabela ou false caso contrário
     */
    public boolean buscarFunctioneProcedure(Simbolo simbolo){


        return tabela.containsKey(simbolo.getIdentificador());
    }

    /***
     * Insere um um entrada na tabela de simbolos, por meio de um mapeamento utilizando o identificador e o simbolo
     * @param simbolo objeto a ser inserido na tabela de simbolos
     */
    public void inserir(Simbolo simbolo){
        this.tabela.put(simbolo.getIdentificador(),simbolo);

    }

    /***
     * Busca um simbolo na tabela e o retorna para ser comparado de acordo com o desejado. Por exemplo, verificar o tipo.
     * @param identificador Chave de busca na tabela.
     * @return
     */
    public Simbolo getIdentificador(String identificador){
        return  this.tabela.get(identificador);
    }


}
