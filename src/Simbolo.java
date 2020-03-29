import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Classe que representa um “símbolo” (elementos da linguagem que são nomeadas por identificadores) na análise semântica.
 * O símbolo pode se referir a uma constante, variável, struct, campo de uma struct, função, procedimento, entre outros descritos
 * pela categoria definida abaixo.
 *
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */
public class Simbolo {
    ArrayList<Simbolo> simbolo = new ArrayList<>();
    private String identificador;                   // Identificador (nome) do símbolo

    private int categoria;                          // Categoria do símbolo (conferir as constantes abaixo)

    public static final int START           = 0,    // Procedimento start
                            FUNCTION        = 1,    // Funções
                            PROCEDURE       = 2,    // Procedimentos
                            STRUCT          = 3,    // Definição de estruturas
                            CONSTANTE       = 4,    // Constantes unitárias (quando não são vetores, matrizes e estruturas)
                            CONST_VETOR     = 5,    // Vetores constantes
                            CONST_MATRIZ    = 6,    // Matrizes constantes
                            VARIAVEL        = 7,    // Variáveis unitárias (quando não são vetores, matrizes e estruturas)
                            VAR_VETOR       = 8,   // Vetores
                            VAR_MATRIZ      = 9;   // Matrizes


    private String tipo;                            // Tipo, primitivo ou definido por um typedef, para constantes e variáveis

    private String tipo_retorno;                    // Tipo de retorno, primitivo ou definido por um typedef, para funções (void para procedimentos e null para demais casos)
    private String parametros;                      // Lista dos tipos de parâmetros, em uma string, em ordem de posicionamento, para funções e procedimentos (Ex: "int#real#string")

    private Map<String,Simbolo> simbolos_local;     // Hash Map que armazenará símbolos pertencentes a funções ou procedimentos (variáveis) ou a estruturas (campos)

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
     * @param categoria categoria da variável/constante
     * @param tipo tipo (primitivo ou definido) da variável/constante
     */
    public Simbolo(String identificador, int categoria, String tipo) {
        this.identificador = identificador;
        this.categoria = categoria;
        if (categoria >= 3) this.tipo = tipo;
    }

    /**
     * Construtor rápido para funções e procedimentos.
     *
     * @param identificador identificador (nome) do símbolo
     * @param categoria 1 se for função ou 2 se for procedimento
     * @param tipo_retorno tipo do retorno da função/procedimento (primitivo ou definido)
     * @param parametros ArrayList de símbolos dos parâmetros da função/procedimento
     */
    public Simbolo(String identificador, int categoria, String tipo_retorno, ArrayList<Simbolo> parametros) {
        this.parametros = "";
        this.identificador = identificador;
        this.categoria = categoria;
        if (categoria == 1 || categoria == 2) {
            this.tipo_retorno = (categoria == 1)?tipo_retorno:"void";
            if (!parametros.isEmpty()) {
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
        if (simbolos_local != null) {
            if (simbolos_local.containsKey(identificador)) {
                return simbolos_local.get(identificador);
            }
        }
        return null;
    }

    public ArrayList<Simbolo> getSimbolos_local() {

        for (Map.Entry<String, Simbolo> entrada : simbolos_local.entrySet()) {
               simbolo.add(entrada.getValue());
        }
        return simbolo;
    }

    public int tamanhoMap(){
        return simbolos_local.size();
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
                parametros = parametros + "#" + simbolo.getTipo();
                return true;
            }
        }
        return false;
    }
}
