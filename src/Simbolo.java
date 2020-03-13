import java.util.HashMap;
import java.util.Map;

/**
 * Classe que representa um “símbolo” (estruturas, funções, procedimentos) da tabela de símbolos de uma análise semântica.
 * Serve para armazenar informações como: variáveis pertencentes a um escopo de uma função ou procedimento, os seus parâmetros,
 * os campos de uma estrutura, bem como os seus respectivos tipos.
 *
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */
public class Simbolo {

    private String identificador;                   // Identificador da função/procedimento/estrutura
    private int tipo;                               // Tipo (START = 0, FUNCTION = 1, PROCEDURE = 2, STRUCT = 3)
    private String parametros;                      // Lista de parâmetros (Ex: "int:a,real:b,string:c")
    private Map<String, String> var_local;          // Hash Map com as variáveis ou campos pertencentes a função/procedimento/estrutura

    // Constantes referentes ao tipo do “símbolo”
    public static final int START = 0, FUNCTION = 1, PROCEDURE = 2, STRUCT = 3;

    /**
     * Construtor da classe Simbolo
     * @param identificador identificador (lexema) do “símbolo”
     * @param tipo tipo do “símbolo” (START = 0, FUNCTION = 1, PROCEDURE = 2, STRUCT = 3)
     */
    public Simbolo(String identificador, int tipo) {
        this.identificador = identificador;
        this.tipo = tipo;
        this.var_local = new HashMap<String, String>();
    }

    /**
     * Retorna o tipo do “símbolo”.
     *
     * @return (START = 0, FUNCTION = 1, PROCEDURE = 2, STRUCT = 3)
     */
    public int getTipo() {
        return tipo;
    }

    /**
     * Retorna o identificador (nome) do “símbolo”.
     *
     * @return o identificador do “símbolo”
     */
    public String getIdentificador() {
        return identificador;
    }

    /**
     * Método para adicionar uma variável ou campo pertencente a função/procedimento/estrutura
     *
     * @param id_variavel identificador (nome) da variável ou campo
     * @param atributos_variavel atributos pertinentes à variável (tipo, categoria, entre outros)
     * @return true se inserido com sucesso, false se já houver alguma variável/campo com mesmo nome (identificador)
     */
    public boolean addVariavel(String id_variavel, String atributos_variavel) {
        if (!var_local.containsKey(id_variavel)) {
            var_local.put(id_variavel, atributos_variavel);
            return true;
        }
        return false;
    }

    /**
     * Retorna uma string com os atributos de uma variável ou campo da função/procedimento/estrutura.
     *
     * @param identificador identificador (nome) do campo ou variável
     * @return string com os atributos dessa variável ou campo, ou null caso não tenha sido declarada (inserida)
     */
    public String getVariavel(String identificador) {
        return var_local.get(identificador);
    }

    /**
     * Método para adicionar um parâmetro da função ou procedimento
     *
     * @param tipo_parametro tipo (primitivo ou definido por um typedef) do parâmetro
     * @param  id_parametro identificador (nome) do parâmetro
     * @return true se inserido com sucesso, false se já houver algum parâmetro com mesmo nome (identificador)
     */
    public boolean addParametro(String tipo_parametro, String id_parametro) {
        if (!var_local.containsKey(id_parametro)) {
            var_local.put(id_parametro, "tipo:" + tipo_parametro);
            parametros += ";" + tipo_parametro + ":" + id_parametro;
            return true;
        }
        return false;
    }

    /**
     * Retorna uma string com os parâmetros da função ou procedimento.
     *
     * @return string com os parâmetros ou null caso não haja
     */
    public String getParametros() {
        return parametros;
    }

}
