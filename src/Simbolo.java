import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Classe que representa um “símbolo” (elementos da linguagem que são nomeadas por identificadores) na análise semântica.
 *
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */
public class Simbolo {

    private String identificador;                   // Identificador da função/procedimento/estrutura

    private int categoria;                          // Categoria do símbolo (conferir as constantes abaixo)

    public static final int START           = 0,    // Procedimento start
                            FUNCTION        = 1,    // Funções
                            PROCEDURE       = 2,    // Procedimentos
                            STRUCT          = 3,    // Definição de estruturas
                            CONSTANTE       = 4,    // Constantes
                            CONST_VETOR     = 6,    // Vetores constantes
                            CONST_MATRIZ    = 7,    // Matrizes constantes
                            CONST_STRUCT    = 8,    // Estruturas declaradas como constantes
                            VARIAVEL        = 9,    // Variáveis unitárias (quando não são vetores, matrizes e estruturas)
                            VAR_VETOR       = 10,   // Vetores
                            VAR_MATRIZ      = 11,   // Matrizes
                            VAR_STRUCT      = 12;   // Estruturas declaradas como variáveis


    private String tipo;                            // Tipo (primitivo ou definido por um typedef) de constante ou variável

    private String tipo_retorno;                    // Tipo de retorno (primitivo ou definido) de função (void para procedimentos e null para demais casos)
    private String parametros;                      // Lista de parâmetros de função ou procedimento (Ex: "int#real#string)

    private Map<String,Simbolo> simbolos_local;     // Hash Map que armazenará símbolos pertencentes a funções e procedimentos (variáveis) ou a estruturas (campos)

    /**
     * Construtor simples da classe Simbolo.
     *
     * @param identificador identificador (nome) do símbolo
     * @param categoria categoria do símbolo, conforme definido pelas constantes da classe
     */
    public Simbolo(String identificador, int categoria) {
        this.identificador = identificador;
        this.categoria = categoria;
    }

    /**
     * Construtor rápido para variáveis e constantes.
     *
     * @param identificador identificador (nome) do símbolo
     * @param categoria 4 se for constante ou 9 se for variável
     * @param tipo tipo (primitivo ou definido) da variável/constante
     */
    public Simbolo(String identificador, int categoria, String tipo) {
        this.identificador = identificador;
        this.categoria = categoria;
        if (categoria == 4 || categoria == 9) this.tipo = tipo;
    }

    /**
     * Construtor rápido para funções e procedimentos.
     *
     * Exemplo de criação de um símbolo função: new Simbolo("funcao_a", 1, "int", "int:para_1;real:para_2")
     * Exemplo de criação de um símbolo procedimento: new Simbolo("procedimento_b", 2, "void", null)
     *
     * @param identificador identificador (nome) do símbolo
     * @param categoria 1 se for função ou 2 se for procedimento
     * @param tipo_retorno tipo do retorno da função/procedimento (primitivo ou definido)
     * @param parametros ArrayList de símbolos dos parâmetros da função/procedimento
     */
    public Simbolo(String identificador, int categoria, String tipo_retorno, ArrayList<Simbolo> parametros) {
        this.identificador = identificador;
        this.categoria = categoria;
        if (categoria == 1 || categoria == 2) {
            this.tipo_retorno = (categoria == 1)?tipo_retorno:"void";
            if (parametros != null) {
                Iterator it = parametros.iterator();
                while (it.hasNext()) addParametro((Simbolo) it.next());
            }
        }
    }

    public String getIdentificador() { return identificador; }

    public int getCategoria() { return categoria; }

    public void setTipo(String tipo) { if (categoria > 3)this.tipo = tipo; }

    public String getTipo() { return tipo; }

    public void setTipo_retorno(String tipo_retorno) { if (categoria == 1 || categoria == 2) this.tipo_retorno = tipo_retorno; }

    public String getTipo_retorno() { return tipo_retorno; }

    public void setParametros(String parametros) { if (categoria == 1 || categoria == 2) this.parametros = parametros; }

    public String getParametros() { return parametros; }

    /**
     * Adiciona um símbolo (variável ou campo) pertencente a função/procedimento/estrutura.
     *
     * @param simbolo Simbolo da variável/constante/campo
     * @return true se inserido com sucesso, false se já houver alguma variável/campo com mesmo nome (identificador)
     */
    public boolean addSimbolo(Simbolo simbolo) {
        if (categoria < 4) {
            if (simbolos_local == null) simbolos_local = new HashMap<String,Simbolo>();

            if (!simbolos_local.containsKey(simbolo.getIdentificador())) {
                simbolos_local.put(simbolo.getIdentificador(), simbolo);
                return true;
            }
        }
        return false;
    }

    /**
     * Retorna uma string com os atributos de uma variável/campo da função/procedimento/estrutura.
     *
     * @param identificador identificador (nome) do campo ou variável
     * @return Simbolo referente ao identificador, null caso não exista
     */
    public Simbolo getSimbolo(String identificador) {
        return (simbolos_local != null)?simbolos_local.get(identificador):null;
    }

    /**
     * Adiciona um parâmetro na lista de parâmetros da função/procedimento (ordenada por inserção)
     *
     * @param simbolo Simbolo do parâmetro
     * @return true se inserido com sucesso, false se já houver algum parâmetro com mesmo nome (identificador)
     */
    public boolean addParametro(Simbolo simbolo) {
        if (categoria == 1 || categoria == 2) {
            if (addSimbolo(simbolo)) {
                parametros = (parametros == null) ? simbolo.getTipo() : parametros + "#" + simbolo.getTipo();
                return true;
            }
        }
        return false;
    }
}
