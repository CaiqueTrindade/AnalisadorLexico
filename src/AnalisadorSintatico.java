import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * Classe responsável por realizar a análise sintática.
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */
public class AnalisadorSintatico {

    private List<Token> tokens; // Lista de tokens
    private List<ErroSintatico> errosSintaticos; // Lista de erros sintáticos
    private  List<ErroSemantico> errosSemanticos; // Lista de erros semânticos
    private Token token; // Token atual
    private static int FLAGERRO = 0; // Flag para identificar se foi encontrado EOF no meio da análise
    private Conjunto conjunto_P_S; // Conjuntos primeiro e seguinte de todos os não-terminais
    private static  int linhaErroEOF; // Linha onde foi encontrado EOF
    private TabelaSimbolos constVar = new TabelaSimbolos(); //Tabela para constantes e variáveis
    private TabelaSimbolos struct = new TabelaSimbolos(); //Tabela para Structs
    private  TabelaSimbolos functionProcedure = new TabelaSimbolos(); //Tabela para funçõres e procedimentos
    private int ordem = 1;
    private Simbolo escopo_atual;
    private ArrayList<Simbolo> parametros = new ArrayList();
    private ArrayList<Simbolo> campos = new ArrayList();


    /**
     * Construtor da classe do AnalisadorSintatico.
     * @param tokens recebe uma lista com todos os tokens gerados pela análise léxica
     */
    public AnalisadorSintatico(List<Token> tokens) {

        this.tokens = tokens;
        this.errosSintaticos = new ArrayList<>();
        this.errosSemanticos = new ArrayList<>();
        this.conjunto_P_S = new Conjunto("conjunto");
    }

    /**
     * Consome o token atual e atualiza para o próximo token da lista.
     */
    private void nextToken() {
        if (tokens.size() > 0) {
            token = tokens.get(0);
            tokens.remove(0);
            linhaErroEOF = token.getnLinha();
        }
        else token = null;

        // ignora blocos de comentários
        if (token != null && token.getTipo() == 10) nextToken();
    }

    /**
     * Verifica se alista de erros está vazia
     * @return true se estiver vazia ou false caso contrário
     */
    public boolean errosIsVazio(){

        return this.errosSintaticos.isEmpty();

    }

    /**
     * Acrescenta um erro sintático na lista de erros sintáticos.
     * @param erro sintático.
     */
    private void addErroSintatico(ErroSintatico erro){

        String erroMensagem [] = erro.getMensagem().split(" ");
        if (erroMensagem[0].equals("EOF")){
            if (FLAGERRO != 1){
                FLAGERRO = 1;
                this.errosSintaticos.add(erro);
            }
        }
        else
            this.errosSintaticos.add(erro);


    }
    /**
     * Acrescenta um erro semântico na lista de erros semânticos.
     * @param erro semântico.
     */
    private void addErroSemantico(ErroSemantico erro){

        if (!errosSemanticos.isEmpty()){
            if (errosSemanticos.get(errosSemanticos.size()-1).getnLinha() != erro.getnLinha())
                errosSemanticos.add(erro);
        }
        else
            errosSemanticos.add(erro);
    }

    /**
     * Busca por tokens de sincronização conforme os parâmetros passados.
     *
     * Todos os conjuntos e lexemas passados devem ser separados pelo caracter #
     * Exemplo de chamada: sincronizar("Var#Start", "Identificador#Struct", "Id#IntPos#++#-");
     *
     * @param conjuntos_primeiro string contendo os conjuntos primeiros a serem inclusos.
     * @param conjuntos_seguinte string contendo os conjuntos seguintes a serem inclusos.
     * @param lexemas string contendo os lexemas a serem inclusos.
     */
    private void sincronizar(String conjuntos_primeiro, String conjuntos_seguinte, String lexemas) {

        ArrayList<String> tokens_sincronizacao = new ArrayList<>();

        if (conjuntos_primeiro != null) {
            String aux[] = conjuntos_primeiro.split("#");
            for (int i = 0; i < aux.length; i++) tokens_sincronizacao.addAll(conjunto_P_S.primeiro(aux[i]));
        }

        if (conjuntos_seguinte != null) {
            String aux[] = conjuntos_seguinte.split("#");
            for (int i = 0; i < aux.length; i++) tokens_sincronizacao.addAll(conjunto_P_S.seguinte(aux[i]));
        }

        if(lexemas != null) {
            String aux[] = lexemas.split("#");
            for (int i = 0; i < aux.length; i++) if (!tokens_sincronizacao.contains(aux[i])) tokens_sincronizacao.add(aux[i]);
        }

        if (tokens_sincronizacao.size() > 0) {

            boolean encontrou = false;
            boolean eof = (token == null);

            while (!encontrou && !eof) {
                if (token.getTipo() == 3) {
                    if (tokens_sincronizacao.contains("Id"))
                        encontrou = true;
                } else if (token.getTipo() == 2 && Integer.parseInt(token.getLexema()) >= 0) {
                    if (tokens_sincronizacao.contains("IntPos"))
                        encontrou = true;
                    else if (tokens_sincronizacao.contains("Numero"))
                        encontrou = true;
                } else if (token.getTipo() == 0 && token.getLexema().matches("^(true|false)$")) {
                    if (tokens_sincronizacao.contains("Boolean"))
                        encontrou = true;
                } else if (token.getTipo() == 11) {
                    if (tokens_sincronizacao.contains("String"))
                        encontrou = true;
                } else if (token.getTipo() == 1 || token.getTipo() == 2) {
                    if (tokens_sincronizacao.contains("Numero"))
                        encontrou = true;
                } else if (tokens_sincronizacao.contains(token.getLexema())) {
                    encontrou = true;
                }

                if (!encontrou) {
                    nextToken();
                    if (token == null) eof = true;
                }
            }
        }
    }

    /**
     * Verifica se o token atual pertence a um conjunto (primeiro ou seguinte).
     *
     * Todos os conjuntos e lexemas passados devem ser separados pelo caracter #
     * Exemplo de chamada para verificar se o token atual pertence ao conjunto primeiro de Var: pertence(0, "Var");
     *
     * @param tipo_conjunto tipo do conjunto (0 para conjunto primeiro, 1 para conjunto seguinte).
     * @param nterminal string com o nome do não terminal.
     * @return true se pertencer ou false caso não pertença.
     */
    private boolean pertence(int tipo_conjunto, String nterminal) {

        ArrayList<String> conjunto = (tipo_conjunto == 0)?conjunto_P_S.primeiro(nterminal):conjunto_P_S.seguinte(nterminal);

        if (token.getTipo() == 3) {
            if (conjunto.contains("Id"))
                return true;
        } else if (token.getTipo() == 2 && Integer.parseInt(token.getLexema()) >= 0) {
            if (conjunto.contains("IntPos"))
                return true;
            else if (conjunto.contains("Numero"))
                return true;
        } else if (token.getTipo() == 0 && token.getLexema().matches("^(true|false)$")) {
            if (conjunto.contains("Boolean") || conjunto.contains("true") || conjunto.contains("false"))
                return true;
        } else if (token.getTipo() == 11) {
            if (conjunto.contains("String"))
                return true;
        } else if (token.getTipo() == 1 || token.getTipo() == 2) {
            if (conjunto.contains("Numero"))
                return true;
        } else if (conjunto.contains(token.getLexema())) {
            return true;
        }

        return false;
    }

    public List<ErroSintatico> getListaErrosSintaticos(){
        return this.errosSintaticos;
    }
    public List<ErroSemantico> getListaErrosSemanticos(){
        return this.errosSemanticos;
    }

    //<F> ::= '(' <ExpressaoAritmetica> ')' | Numero
    public String F(){

        String tipo = null;

        if (token != null && token.getLexema().equals("(")){
            tipo = ExpressaoAritmetica();
        }
        else if (token != null &&  pertence(0,"F")){
            if (token.getTipo() == 1) tipo = "real";
            else if (token.getTipo() == 2) tipo = "int";
            nextToken();
        }
        return tipo;
    }

    //<T> ::= <F> <T2>
    public String T(){
        String tipo = null;
        String tipo2 = null;

        if (token != null && conjunto_P_S.primeiro("F").contains(token.getLexema()) || pertence(0,"F")){
            tipo = F();
            tipo2 = T2();
            if (tipo != null && tipo2 != null && !tipo.equals(tipo2)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo2 +").", token.getnLinha()));
        }
        return (tipo != null)?tipo:tipo2;
    }

    //<T2> ::= '*' <ExpressaoAritmetica> | '/' <ExpressaoAritmetica> | <>
    public String T2(){
        String tipo = null;

        if (token != null) {
            if (token.getLexema().equals("*") || token.getLexema().equals("/")) {
                nextToken();
                tipo = ExpressaoAritmetica();
            }
        }
       return tipo;
    }

    //<E2> ::= '+' <ExpressaoAritmetica>
    //| '-' <ExpressaoAritmetica>
    //|  <>
    public String E2(){
        String tipo = null;

        if (token != null) {
            if (token.getLexema().equals("+") || token.getLexema().equals("-")) {
                nextToken();
                tipo = ExpressaoAritmetica();
            }
        }
        return tipo;
    }

    //<IdentificadorAritmetico> ::= <Escopo> Id <Identificador2> <ExpressaoAritmetica2> | Id <IdentificadorAritmetico3>
    public String IdentificadorAritmetico(){
        String tipo = null, tipo2 = null;

        if (token != null && pertence(0, "Escopo") || conjunto_P_S.primeiro("IdentificadorAritmetico").contains(token.getLexema())){
            String escopo = Escopo();
            if (token != null && token.getTipo() == 3){
                String id = token.getLexema();
                nextToken();

                Simbolo simbolo = null;
                if (escopo == null || escopo.equals("local")) simbolo = escopo_atual.getSimbolo(id);
                if (simbolo == null || escopo.equals("global")) simbolo = constVar.getIdentificadorGeneral(id);

                if (simbolo == null) addErroSemantico(new ErroSemantico("Identificador não declarado", id + " não declarado", token.getnLinha()));

                tipo = Identificador2(simbolo).getTipo();
                tipo2 = ExpressaoAritmetica2();
            }
        }
        else if (token != null && token.getTipo() == 3) {
            String id = token.getLexema();
            int linha = token.getnLinha();
            nextToken();
            tipo = IdentificadorAritmetico3(id);
        }

        if (tipo != null && tipo2 != null && !tipo.equals(tipo2)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo2 +").", token.getnLinha()));

        return tipo;
    }

    //<IdentificadorAritmetico3> ::= <Identificador2> <ExpressaoAritmetica2> | '(' <ListaParametros> ')' <T2> <E2>
    public String IdentificadorAritmetico3(String id){
        String tipo = null, tipo2 = null, tipo3 = null;

        if (token != null && token.getLexema().equals("(")) {
            nextToken();
            ArrayList<Simbolo> lParametros = IdentificadorExtra();

            if (token != null && token.getLexema().equals(")")){
                nextToken();
                tipo2 = T2();
                tipo3 = E2();
            }

        }
        else if (token != null) {

            Simbolo simbolo = (escopo_atual.getSimbolo(id) != null)?escopo_atual.getSimbolo(id):constVar.getIdentificadorGeneral(id);
            if (simbolo == null) addErroSemantico(new ErroSemantico("Identificador não declarado", id + " não declarado", token.getnLinha()));

            tipo = Identificador2(simbolo).getTipo(); // Permite vazio
            tipo2 = ExpressaoAritmetica2();
        }
        else addErroSintatico(new ErroSintatico("IdentificadorAritmetico3","EOF inesperado", linhaErroEOF));

        if (tipo != null && tipo2 != null && tipo3 != null && !tipo.equals(tipo2) || !tipo.equals(tipo3)) {
            String tipo_diferente = (!tipo.equals(tipo2))?tipo2:tipo3;
            addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo_diferente +").", token.getnLinha()));
        }
        else {
            if (tipo != null && tipo2 != null && !tipo.equals(tipo2)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo2 +").", token.getnLinha()));
            else if (tipo != null && tipo3 != null && !tipo.equals(tipo3)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo3 +").", token.getnLinha()));
            else if (tipo2 != null && tipo3 != null && !tipo2.equals(tipo3)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo2 + " mas encontrou do tipo "+ tipo3 +").", token.getnLinha()));
            else if (tipo2 != null && tipo == null && tipo3 == null) tipo = tipo2;
            else if (tipo3 != null && tipo == null && tipo2 == null) tipo = tipo3;
        }

        return tipo;
    }

    // <ExpressaoAritmetica> ::= <T> <E2> | <IdentificadorAritmetico> | '++' <IdentificadorSemFuncao> <T2> <E2> | '--' <IdentificadorSemFuncao> <T2> <E2>
    public String ExpressaoAritmetica (){
        String tipo = null;
        String tipo2 = null;
        String tipo3 = null;

        if (token != null && pertence(0, "T") || conjunto_P_S.primeiro("T").contains(token.getLexema())){
            tipo = T();
            tipo2 = E2();
        }
        else if (token != null && pertence(0, "IdentificadorAritmetico") || conjunto_P_S.primeiro("Escopo").contains(token.getLexema())){
            tipo = IdentificadorAritmetico();
        }
        else if (token != null && token.getTipo() == 9){
            nextToken();
            tipo = IdentificadorSemFuncao();
            tipo2 = T2();
            tipo3 = E2();
        }

        if (tipo != null && tipo2 != null && tipo3 != null && !tipo.equals(tipo2) || !tipo.equals(tipo3)) {
            String tipo_diferente = (!tipo.equals(tipo2))?tipo2:tipo3;
            addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo_diferente +").", token.getnLinha()));
        }
        else {
            if (tipo != null && tipo2 != null && !tipo.equals(tipo2)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo2 +").", token.getnLinha()));
            else if (tipo != null && tipo3 != null && !tipo.equals(tipo3)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo3 +").", token.getnLinha()));
            else if (tipo2 != null && tipo3 != null && !tipo2.equals(tipo3)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo2 + " mas encontrou do tipo "+ tipo3 +").", token.getnLinha()));
            else if (tipo2 != null && tipo == null && tipo3 == null) tipo = tipo2;
            else if (tipo3 != null && tipo == null && tipo2 == null) tipo = tipo3;
        }

        return tipo;
    }

    //<ExpressaoAritmetica2> ::= '++' <T2> <E2> | '--' <T2> <E2> | <T2> <E2>
    private String ExpressaoAritmetica2() {
        String tipo = null, tipo2 = null;

        if(token != null) {
            if(token.getTipo() == 9) {
                nextToken();
            }
            tipo = T2();
            tipo = E2();
        }


        if (tipo != null && tipo2 != null && !tipo.equals(tipo2)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo2 +").", token.getnLinha()));

        return tipo;
    }


    //<IdentificadorSemFuncao> ::= <Escopo> Id <Identificador2> | Id <Identificador2>
    public String IdentificadorSemFuncao(){
        String tipo = null;

        if(token != null && pertence(0, "Escopo") || conjunto_P_S.primeiro("Escopo").contains(token.getLexema())){
            String escopo = Escopo();
            if (token != null && token.getTipo() == 3){
                String id = token.getLexema();
                nextToken();

                Simbolo simbolo = null;
                if (escopo == null || escopo.equals("local")) simbolo = escopo_atual.getSimbolo(id);
                if (simbolo == null || escopo.equals("global")) simbolo = constVar.getIdentificadorGeneral(id);

                if (simbolo == null) addErroSemantico(new ErroSemantico("Identificador não declarado", id + " não declarado", token.getnLinha()));

                tipo = Identificador2(simbolo).getTipo();
            }
        }
        else  if (token != null && token.getTipo() == 3){
            String id = token.getLexema();
            nextToken();

            Simbolo simbolo = (escopo_atual.getSimbolo(id) != null)?escopo_atual.getSimbolo(id):constVar.getIdentificadorGeneral(id);
            if (simbolo == null) addErroSemantico(new ErroSemantico("Identificador não declarado", id + " não declarado", token.getnLinha()));

            tipo = Identificador2(simbolo).getTipo();
        }

        return tipo;
    }




    // <IndiceVetor> ::= IntPos | <Identificador>
    public void IndiceVetor(){

        if (token != null && token.getTipo() == 2 && Integer.parseInt(token.getLexema()) >= 0) {
            nextToken();
        }
        else if (token != null && pertence(0, "Identificador") || conjunto_P_S.primeiro("Escopo").contains(token.getLexema())){
            Simbolo sb = Identificador();
            if (!sb.getTipo().equals("int")){
                addErroSemantico(new ErroSemantico("Parâmetro incompatível: tipo", " parametro incompatível: "+ sb.getTipo(), token.getnLinha()));
            }

        }


    }
    //<Struct> ::= 'typedef' 'struct' Id <Extends> <TipoStruct> <Struct> | <>
    public void struct(){

        Simbolo simbolo = null;
        if(token != null && token.getLexema().equals("typedef")){
            nextToken();
            if ( token != null && token.getLexema().equals("struct")){
                nextToken();
                if (token != null && token.getTipo() == 3){ //Id
                    String identificador = token.getLexema();

                    if(!struct.buscarGeneral(identificador)){
                        simbolo = new Simbolo(identificador, Simbolo.STRUCT, identificador);
                        struct.inserirGeneral(simbolo);
                    }
                    else  addErroSemantico(new ErroSemantico("Identificador de Struct já declarado", token.getLexema()+ " já foi declarado", token.getnLinha()));
                    nextToken();
                    Extend(identificador);
                    TipoStruct();
                    Iterator it = campos.iterator();
                    while (it.hasNext()) {
                        if (simbolo != null)
                            simbolo.addSimbolo((Simbolo) it.next());
                        else
                            System.out.println("Erro");
                    }

                }
            }
        }

  }
    //<Extends> ::= 'extends' Id '{' | '{'
    public void Extend(String identificador){
        if(token != null && token.getLexema().equals("extends")){
            nextToken();
            if (token != null && token.getTipo() == 3){
                String identificador_aux = token.getLexema();
                if(identificador.equals(identificador_aux)){
                    addErroSemantico(new ErroSemantico("Extends não permitido", token.getLexema()+ " tem o mesmo nome da estrutura", token.getnLinha()));

                }
                else if(!struct.buscarGeneral(identificador))
                    addErroSemantico(new ErroSemantico("Identificador de Struct não declarado", token.getLexema()+ " não pode estender", token.getnLinha()));
                nextToken();
                if (token != null && token.getLexema().equals("{")){
                    nextToken();
                }
            }
        } else if(token != null && token.getLexema().equals("{")){
            nextToken();
        }


    }
    //<TipoStruct> ::= <Tipo> <IdStruct>
    public void TipoStruct(){
        campos.clear();
        String tipo = Tipo();
        IdStruct(tipo);


    }
    //<IdStruct> ::= Id <Struct2>
    public void IdStruct(String tipo){
        String identificador = "";
        if(token != null && token.getTipo() == 3){
           identificador = token.getLexema();

            if(constVar.buscarGeneral(identificador) && constVar.getIdentificadorGeneral(identificador).getCategoria() == Simbolo.CONSTANTE){
                if (!campos.isEmpty()) {
                    Simbolo simbolo_aux = new Simbolo(identificador, Simbolo.CONSTANTE, tipo);
                    if (!campos.contains(simbolo_aux)){
                        campos.add(simbolo_aux);
                    }
                    else addErroSemantico(new ErroSemantico("Constante já utilizada", token.getLexema()+ " já foi declarado", token.getnLinha()));

                }
            }
            else{
                if (!campos.isEmpty()) {
                    Simbolo simbolo_aux = new Simbolo(identificador, Simbolo.VARIAVEL, constVar.getIdentificadorGeneral(identificador).getTipo());
                    if (!campos.contains(simbolo_aux)){
                        campos.add(simbolo_aux);
                    }
                    else addErroSemantico(new ErroSemantico("Identificador já declarado", token.getLexema()+ " já foi declarado", token.getnLinha()));

                }

            }
            nextToken();
            Struct2(tipo);
        }


    }

    //<Struct2> ::= ',' <IdStruct> | ';' <Struct3>
    public void Struct2(String tipo){
        if(token != null && token.getLexema().equals(",")){
            nextToken();
            IdStruct(tipo);
        } else if(token != null && token.getLexema().equals(";")){
            nextToken();
            Struct3();
        }
    }

    //<Struct3> ::= '}' | <TipoStruct>
    public void Struct3(){
        if(token != null && token.getLexema().equals("}")){
            nextToken();
        }else if (token != null && pertence(0, "TipoStruct")){
            TipoStruct();
        }

    }

    private String Valor(){
        String tipo_valor = null;
        if(token != null && pertence(0, "Valor")){
            if (token.getLexema().matches("^(true|false)$")) tipo_valor = "boolean";
            else if (token.getTipo() == 1) tipo_valor = "real";
            else if (token.getTipo() == 2) tipo_valor = "int";
            else if (token.getTipo() == 11) tipo_valor = "string";
            else if (token.getTipo() == 3) {
                if (escopo_atual != null && escopo_atual.getSimbolo(token.getLexema()) == null) addErroSemantico(new ErroSemantico("Identificador não declarado", token.getLexema() + " não foi declarado", token.getnLinha()));
                else if (escopo_atual != null) tipo_valor = escopo_atual.getSimbolo(token.getLexema()).getTipo();
                else if (constVar.getIdentificadorGeneral(token.getLexema()) == null) addErroSemantico(new ErroSemantico("Identificador não declarado", token.getLexema() + " não foi declarado", token.getnLinha()));
                else tipo_valor = constVar.getIdentificadorGeneral(token.getLexema()).getTipo();
            }
            nextToken();
        }
        return tipo_valor;
    }
    //<ValorVetor> ::= IntPos | Id
    public void ValorVetor(){
        if(token != null && pertence(0, "ValorVetor")){
            nextToken();
        }
    }

    private String Tipo(){
        String tipo = null;
        if(token != null && token.getLexema().equals("int") || token.getLexema().equals("boolean") || token.getLexema().equals("string") || token.getLexema().equals("real") || token.getTipo() == 3){
            tipo = token.getLexema();
            nextToken();
            return tipo;
        }
        return tipo;
    }

    public void Laco() {
        if (token != null && token.getLexema().equals("while")) {
            nextToken();
            if (token != null && token.getLexema().equals("(")) {
                nextToken();
                ExpressaoLogicaRelacional();
                if (token != null && token.getLexema().equals(")")) {
                    nextToken();
                    if (token != null && token.getLexema().equals("{")) {
                        nextToken();
                        Corpo2();
                        if (token != null && token.getLexema().equals("}")) {
                            nextToken();
                        }
                    }
                }
            }
        }
    }


    // <GeraFuncaoeProcedure> ::= <Funcao> <GeraFuncaoeProcedure> | <Procedimento>  <GeraFuncaoeProcedure> | <>
    public void GeraFuncaoeProcedure(){
        if(token != null && pertence(0, "Funcao")){
            Funcao();
            GeraFuncaoeProcedure();
        }else if(token != null && pertence(0, "Procedimento")){
            Procedimento();

            GeraFuncaoeProcedure();
        }

    }

    // <Funcao> ::= 'function' <Tipo> Id '(' <Parametro>
    public void Funcao(){
        int erro = 0;
        parametros.clear();
        if(token != null && token.getLexema().equals("function")){
            nextToken();
            String tipo = Tipo();
            String id = null;
            int cat = Simbolo.FUNCTION;
            String tipo_retorno = null;

            if(token != null && token.getTipo() == 3){
                if(functionProcedure.buscarGeneral(token.getLexema())){
                    addErroSemantico(new ErroSemantico("Identificador já foi declarado", token.getLexema()+ " já foi declarado", token.getnLinha()));
                    erro = 1;
                }else{
                    id = token.getLexema();
                    cat = 1;

                    if(tipo != null) {
                        tipo_retorno = tipo;
                    }else{
                        erro = 1;
                    }
                }
                nextToken();
                if(token != null && token.getLexema().equals("(")){
                    nextToken();
                    Parametro();
                }
                if(erro == 0 && parametros != null){
                    Simbolo sb = new Simbolo(id, cat, tipo_retorno, parametros);
                    if (!(functionProcedure.buscarFunctioneProcedure(sb) && functionProcedure.getFunctionProcedure(sb).getCategoria() == Simbolo.FUNCTION)){
                        functionProcedure.inserirFunctionProcedure(sb);
                        escopo_atual = sb;
                    }
                    else  addErroSemantico(new ErroSemantico("Identificador de procedimento já declarado", id + " já foi declarado", token.getnLinha()));
                }



            }
        }
    }

//<Procedimento> ::= 'procedure' Id '(' <Parametro>
//
//<Parametro> ::=  <Tipo> Id <Para2> <Para1>
//<Para1> ::= ',' <Parametro> | ')' <F2>
//<Para2>  ::= '[' ']'<Para3> |<>
//<Para3>  ::= '[' ']' | <>
//<F2> ::= '{' <Corpo>

    //<Procedimento> ::= 'procedure' Id '(' <Parametro>
 public void Procedimento() {

         parametros.clear();
         if (token != null && token.getLexema().equals("procedure")) {
            nextToken();
            if (token != null && token.getTipo() == 3) {
                String identificador = token.getLexema();
                nextToken();
                if (token != null && token.getLexema().equals("(")) {
                    nextToken();
                    Parametro();
                    escopo_atual = new Simbolo(identificador, Simbolo.PROCEDURE, "", parametros);
                    if (!(functionProcedure.buscarFunctioneProcedure(escopo_atual) && functionProcedure.getFunctionProcedure(escopo_atual).getCategoria() == Simbolo.PROCEDURE)){
                        functionProcedure.inserirFunctionProcedure(escopo_atual);
                    }
                    else  addErroSemantico(new ErroSemantico("Identificador de procedimento já declarado", token.getLexema()+ " já foi declarado", token.getnLinha()));
                }
            }
        }
    }

    //<Parametro> ::=  <Tipo> Id <Para2> <Para1>
    public void Parametro(){
        String tipo = Tipo();

        if(token != null && token.getTipo() == 3){
            nextToken();

            if(constVar.buscarGeneral(token.getLexema()) && constVar.getIdentificadorGeneral(token.getLexema()).getCategoria() == Simbolo.CONSTANTE) {
                int retorno = Para2();
                if (retorno == 0) parametros.add(new Simbolo(token.getLexema(),Simbolo.CONST_VETOR,tipo));
                else if (retorno == 1) parametros.add(new Simbolo(token.getLexema(),Simbolo.CONST_MATRIZ,tipo));
                else if (retorno == 2) parametros.add(new Simbolo(token.getLexema(),Simbolo.CONSTANTE,tipo));
            }
            else{
                int retorno = Para2();
                if (retorno == 0) parametros.add(new Simbolo(token.getLexema(),Simbolo.VAR_VETOR,tipo));
                else if (retorno == 1) parametros.add(new Simbolo(token.getLexema(),Simbolo.VAR_MATRIZ,tipo));
                else if (retorno == 2) parametros.add(new Simbolo(token.getLexema(),Simbolo.VARIAVEL,tipo));
            }
            Para1();

        }

    }


    public void Para1(){
        if(token != null && token.getLexema().equals(",")){
            nextToken();
            Parametro();
        }else if(token != null && token.getLexema().equals(")")){
            nextToken();
            F2();
        }
    }

    // 1 indica que é matriz 0 indica que é vetor e 2 indica que não é nem vetor nem matriz
    public int Para2(){
        if(token != null && token.getLexema().equals("[")){
            nextToken();
            if(token != null && token.getLexema().equals("]")){
                nextToken();
                if (Para3()) return 1;
            }
            return 0;

        }
        return 2;

    }

    public boolean Para3(){
        if(token != null && token.getLexema().equals("[")){
            nextToken();
            if(token != null && token.getLexema().equals("]")){
                nextToken();
            }
            return true;
        }
        return false;
    }

    public void F2(){
        if(token != null && token.getLexema().equals("{")){
            nextToken();
            Corpo();
        }
    }

    // <Print> ::= 'print' '(' <Print1>
    public void Print(){
        if(token != null && token.getLexema().equals("print")){
            nextToken();
            if(token != null && token.getLexema().equals("(")){
                nextToken();
                Print1();
            }
        }
    }

    // <Print1> ::= String <AuxPrint> | <Identificador> <AuxPrint> | Numero <AuxPrint>
    public void Print1(){
        if(token != null && token.getTipo() == 11 || token.getTipo() == 1 || token.getTipo() == 2){
            nextToken();
            AuxPrint();
        }else if(token != null && pertence(0, "Identificador")){
            Simbolo sb = Identificador();
            if(sb != null) {
                if (!(functionProcedure.buscarFunctioneProcedure(sb) ||
                        functionProcedure.getFunctionProcedure(escopo_atual).getIdentificador() == sb.getIdentificador() ||
                        constVar.buscarGeneral(sb.getIdentificador()))) {
                    addErroSemantico(new ErroSemantico("Identificador de procedimento já declarado", sb.getIdentificador() + " já foi declarado", token.getnLinha()));

                }
                addErroSemantico(new ErroSemantico("Identificador de procedimento já declarado", sb.getIdentificador() + " já foi declarado", token.getnLinha()));

            }
            AuxPrint();
        }
    }

    //
    public void AuxPrint(){
        if(token != null && token.getLexema().equals(",")){
            nextToken();
            Print1();
        }else if(token != null && pertence(0, "PrintFim")){
            PrintFim();
        }
    }

    public void PrintFim() {
        if (token != null && token.getLexema().equals(")")) {
            nextToken();
            if (token != null && token.getLexema().equals(";")) {
                nextToken();
            }
        }
    }

    //<CondEnd> ::= 'else' '{' <Corpo> '}'| <>
    public void CondEnd(){
        if(token != null && token.getLexema().equals("else")){
            nextToken();
            if(token != null && token.getLexema().equals("{")){
                nextToken();
                Corpo2();
                if(token != null && token.getLexema().equals("}")){
                    nextToken();
                }
            }
        }

    }

    public void Condicional(){
        if(token != null && token.getLexema().equals("if")){
            nextToken();
            if(token != null && token.getLexema().equals("(")){
                nextToken();
                ExpressaoLogicaRelacional();
                if(token != null && token.getLexema().equals(")")){
                    nextToken();
                    if(token != null && token.getLexema().equals("then")){
                        nextToken();
                        if(token != null && token.getLexema().equals("{")){
                            nextToken();
                            Corpo2();
                            if(token != null && token.getLexema().equals("}")){
                                nextToken();
                                CondEnd();
                            }
                        }
                    }
                }
            }
        }
    }


    //<Vetor2> ::= '[' <IndiceVetor> ']'
//    | <>
    public int Vetor2(){

        if (token != null && token.getLexema().equals("[")){
            nextToken();
            IndiceVetor();
            if (token != null && token.getLexema().equals("]")) {
                nextToken();
            }
            return 9;
        }
        return 8;

    }

    //<Vetor> ::= '[' <IndiceVetor> ']' <Vetor2> <Identificador4> | <Identificador4>
    public Simbolo Vetor(Simbolo s){

        if(token != null && token.getLexema().equals("[")){
            nextToken();
            IndiceVetor();
            if (token != null && token.getLexema().equals("]")) {
                nextToken();
                int matriz = Vetor2();
                if(s != null) {
                    if (s.getCategoria() == matriz) {
                        Simbolo n = Identificador4(s);
                        if (n != null) {
                            return n;
                        }
                    } else {
                        addErroSemantico(new ErroSemantico("Identificador não declarado", s.getIdentificador() + " não declarado", token.getnLinha()));
                        return null;
                    }
                }else{
                    Identificador4(s);
                    return null;
                }


            }


        } else if ( token != null && conjunto_P_S.primeiro("Identificador4").contains(token.getLexema())){
            Simbolo n = Identificador4(s);
            if (n != null){
                return n;
            }else{
                return null;
            }
        }
        return null;

    }
    //<Identificador4> ::= '.' Id <VetorDeclaracao>
    //    | <>
    public Simbolo Identificador4(Simbolo s) {

        if (token != null && token.getLexema().equals(".")) {
            nextToken();
            if (token != null && token.getTipo() == 3) {
                if (s != null) {
                    Simbolo var = s.getSimbolo(token.getLexema());
                    if (var != null) {
                        nextToken();
                        s = Vetor(var);

                    } else {
                        addErroSemantico(new ErroSemantico("Identificador não declarado", var.getIdentificador() + " não declarado", token.getnLinha()));

                        return null;
                    }
                }else{
                    nextToken();
                    s = Vetor(s);
                }
            }
        }
        return s;
    }

    //<Escopo> ::= 'local' '.' | 'global' '.'
    public String Escopo(){
        if (token != null && (token.getLexema().equals("local") || token.getLexema().equals("global"))){
            String escopo = token.getLexema();
            nextToken();
            if (token != null && token.getLexema().equals(".")){
                nextToken();
            }

            return escopo;

        }

        return null;

    }
    //<Identificador2> ::= '.' Id <Vetor> | <Vetor> | <>
    public Simbolo Identificador2(Simbolo entrada){

        if (token != null && token.getLexema().equals(".")){
            nextToken();
            if (token!= null && token.getTipo() == 3){
                if(entrada.getCategoria() == 3) {
                    String id = token.getLexema();
                    nextToken();
                    Simbolo var = struct.getIdentificadorGeneral(entrada.getIdentificador());
                    var = var.getSimbolo(id);
                    if (var == null) {

                        addErroSemantico(new ErroSemantico("Identificador não declarado", var.getIdentificador() + " não declarado", token.getnLinha()));

                    }
                    var = Vetor(var);
                    if (var != null) {
                        return var;
                    } else {
                        return null;
                    }


                }


            }


        } else if (token != null && conjunto_P_S.primeiro("Vetor").contains(token.getLexema())){

            Simbolo var = Vetor(entrada);
            if (var != null) {
                return var;
            } else {
                return null;
            }


        }

        return entrada;

    }

    //<ContListaParametros> ::= ',' <ListaParametros> | <>
    public ArrayList<Simbolo> ContListaParametros(ArrayList<Simbolo> simbolos){
        if (token != null && token.getLexema().equals(",")){
            nextToken();
            simbolos = ListaParametros(simbolos);
        }
       return simbolos;
    }

    // <ListaParametros2> ::= <Identificador> | Numero | String
    public Simbolo ListaParametros2(){
        Simbolo s = null;
        if (token != null && pertence(0, "Identificador") || conjunto_P_S.primeiro("Escopo").contains(token.getLexema()))
            s = Identificador();
        else if (token != null && token.getTipo() == 11 || token.getTipo() == 1 || token.getTipo() == 2) {
            if (token.getTipo() == 11) {
                s = new Simbolo(token.getLexema(), 7, "string");
            } else if (token.getTipo() == 1) {
                s = new Simbolo(token.getLexema(), 7, "real");
            } else if (token.getTipo() == 2) {
                s = new Simbolo(token.getLexema(), 7, "int");
            }

            nextToken();
        }
        return s;
    }

    // <ListaParametros> ::= <ListaParametros2> <ContListaParametros>
    public ArrayList<Simbolo> ListaParametros(ArrayList<Simbolo> simbolos){
        Simbolo s = ListaParametros2();
        if (s != null) {
            simbolos.add(s);
        }
        simbolos = ContListaParametros(simbolos);
        return simbolos;
    }

    private ArrayList<Simbolo> IdentificadorExtra() {
        ArrayList<Simbolo> lParametros = null;
        if (token!= null && pertence(0, "ListaParametros")) {
            lParametros = new ArrayList<Simbolo>();
            lParametros = ListaParametros(lParametros);
        }
        return lParametros;
    }

    // <Identificador3> ::= <Identificador2> | '(' <IdentificadorExtra> ')'
    public Simbolo Identificador3(Simbolo entrada){
        if (token != null) { //Permite vazio
            entrada = Identificador2(entrada);
        }
        return entrada;
    }

    // <Identificador3> ::= <Identificador2> | '(' <IdentificadorExtra> ')'
    public Simbolo Identificador3(String id_entrada){
        int erro = 0;
        if (token != null && token.getLexema().equals("(")) {
            nextToken();
            ArrayList<Simbolo> lParametros = IdentificadorExtra();
            String id = id_entrada;
            for (Simbolo s: lParametros) {
                id = id + "#";
                id = id + s.getTipo();
            }
            if (functionProcedure.buscarGeneral(id)) {
                Simbolo entrada = functionProcedure.getIdentificadorGeneral(id);

                if (entrada.getCategoria() == 1 || entrada.getCategoria() == 2) {

                    String[] tipos = entrada.getParametros().split("#");
                    if (lParametros.size() != tipos.length) {
                        addErroSemantico(new ErroSemantico("Parâmetros incompatíveis", "Parâmetros incompatíveis: quantidade", token.getnLinha()));
                        erro = erro + 1;
                    }
                    int i = 0;

                    for (Simbolo s : lParametros) {
                        if (s.getTipo() != tipos[i]) {
                            addErroSemantico(new ErroSemantico("Parâmetros incompatíveis", "Parâmetros incompatíveis: tipo", token.getnLinha()));
                            erro = erro + 1;
                        }
                        i = i + 1;
                    }

                    // checar se os parametros estao corretos com a entrada
                    if (token != null && token.getLexema().equals(")")) {
                        nextToken();
                        return entrada;
                    }
                    if (erro > 0) {
                        return null;
                    }

                } else {
                    addErroSemantico(new ErroSemantico("Identificador não declarado", id_entrada + " não declarado", token.getnLinha()));
                    return null;
                }
            }else{
                addErroSemantico(new ErroSemantico("Identificador não declarado", id_entrada + " não declarado", token.getnLinha()));
                return null;
            }

        }

        return null;
    }


    // <Identificador> ::= <Escopo> Id <Identificador2> | Id <Identificador3>
    public Simbolo Identificador(){
        if (token != null && pertence(0, "Escopo")){
            String escopo = Escopo();
            if (token != null  && token.getTipo() == 3){
                String id = token.getLexema();
                nextToken();
                if (escopo.equals("local") || escopo == null){
                    Simbolo s = escopo_atual;
                    Simbolo str = s.getSimbolo(id);
                    if (str != null){
                        s = Identificador2(str);
                        return s;
                    }else{
                        addErroSemantico(new ErroSemantico("Identificador não declarado", id + " não declarado", token.getnLinha()));
                        return null;
                    }
                }if(escopo.equals("global") || escopo == null) {
                    if (constVar.buscarGeneral(id)) {
                        Simbolo s = constVar.getIdentificadorGeneral(id);
                        s = Identificador2(s);
                        return s;

                    }else{
                        addErroSemantico(new ErroSemantico("Identificador não declarado", id + " não declarado", token.getnLinha()));

                        return null;
                    }
                }


            }

        }
        else if (token != null && token.getTipo() == 3){
            String id = token.getLexema();
            nextToken();
            Simbolo f = escopo_atual;
            if (constVar.buscarGeneral(id)) {
                Simbolo s = constVar.getIdentificadorGeneral(id);
                s = Identificador3(s);
                return s;

            } else if(f.getSimbolo(id) != null) {
                Simbolo str = f.getSimbolo(id);
                Simbolo s = Identificador3(str);
                return s;

            }else{
                Simbolo s = Identificador3(id);
                if(s != null){
                    return s;
                }else {
                    addErroSemantico(new ErroSemantico("Identificador não declarado", id + " não declarado", token.getnLinha()));
                    return null;
                }
            }


        }


        return null;
    }



    //<Matriz> ::= '[' <ValorVetor> ']' <Var4> | <Var4>
    public void Matriz(String tipo, String id, String linha){

        if (token != null && token.getLexema().equals("[")){
            nextToken();
            ValorVetor();
            if (token != null && token.getLexema().equals("]")){
                nextToken();

                if (escopo_atual != null && escopo_atual.getSimbolo(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
                else if (escopo_atual != null) escopo_atual.addSimbolo(new Simbolo(id, Simbolo.VAR_MATRIZ, tipo));
                else if (constVar.getIdentificadorGeneral(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
                else constVar.inserirGeneral(new Simbolo(id, Simbolo.VAR_MATRIZ, tipo));

                Var4(tipo);
            }
        }
        else if (token != null && conjunto_P_S.primeiro("Var4").contains(token.getLexema())) {

            if (escopo_atual != null && escopo_atual.getSimbolo(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", token.getnLinha()));
            else if (escopo_atual != null) escopo_atual.addSimbolo(new Simbolo(id, Simbolo.VAR_VETOR, tipo));
            else if (constVar.getIdentificadorGeneral(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", token.getnLinha()));
            else constVar.inserirGeneral(new Simbolo(id, Simbolo.VAR_VETOR, tipo));
            Var4(tipo);
        }
    }


    //<Vetor3> ::= '[' <ValorVetor> ']' <Matriz>
    public void Vetor3(String tipo, String id, String linha){

        if (token != null && token.getLexema().equals("[")){
            nextToken();
            ValorVetor();
            if (token != null && token.getLexema().equals("]")){
                nextToken();
                Matriz(tipo, id, linha);
            }
        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("Vetor3", "Esperava [ mas encontrou "+token.getLexema(),token.getnLinha()));
            sincronizar("ValorVetor#Matriz#GeraFuncaoeProcedure#Start", "Vetor3", null);

            if (token != null){
                if (conjunto_P_S.primeiro("ValorVetor").contains(token.getLexema())){
                    ValorVetor();
                }
                if (conjunto_P_S.primeiro("Matriz").contains(token.getLexema())){
                    Matriz(tipo);
                }
                else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                    GeraFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start(0);
                }
            }

        }


    }



    //<Var4> ::= ',' <IdVar> | ';' <Var3>
    public void Var4(String tipo){

        if (token != null && token.getLexema().equals(",")){
            nextToken();
            IdVar(tipo);

        }
        else if (token != null && token.getLexema().equals(";")){
            nextToken();
            Var3();

        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("Var4", "Esperava , ou ; mas encontrou um " + token.getLexema()+ " não esperado", token.getnLinha()));
            sincronizar("IdVar#Var3#Var2#GeraFuncaoeProcedure#Start","Var4", null);
            if (token != null){
                if (conjunto_P_S.primeiro("IdVar").contains(token.getLexema())){
                    IdVar(tipo);
                }
                else if (conjunto_P_S.primeiro("Var3").contains(token.getLexema())){
                    Var3();
                }
                else if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                    Var2(tipo);
                }
                else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                    GeraFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start(0);
                }
            }
        }



    }
    //<Var3> ::= '}' | <TipoVar>
    public void Var3(){

        if (token != null && token.getLexema().equals("}")){
            nextToken();

        }
        else if (token != null && pertence(0, "TipoVar")){
            TipoVar();
        }
    }

    //<Var2> ::= ',' <IdVar> | ';' <Var3> | '=' <Valor> <Var4> | <Vetor3>
    public void Var2(String tipo, String id, int linha){
        if (token != null && conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
            if (token != null && token.getLexema().equals(",")){
                nextToken();

                if (escopo_atual != null && escopo_atual.getSimbolo(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
                else if (escopo_atual != null) escopo_atual.addSimbolo(new Simbolo(id, Simbolo.VARIAVEL, tipo));
                else if (constVar.getIdentificadorGeneral(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
                else constVar.inserirGeneral(new Simbolo(id, Simbolo.VARIAVEL, tipo));

                IdVar(tipo);
            }
            else if (token != null &&  token.getLexema().equals(";")){
                nextToken();

                if (escopo_atual != null && escopo_atual.getSimbolo(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
                else if (escopo_atual != null) escopo_atual.addSimbolo(new Simbolo(id, Simbolo.VARIAVEL, tipo));
                else if (constVar.getIdentificadorGeneral(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
                else constVar.inserirGeneral(new Simbolo(id, Simbolo.VARIAVEL, tipo));

                Var3();
            }
            else if (token != null && token.getLexema().equals("=")){
                nextToken();

                if (escopo_atual != null && escopo_atual.getSimbolo(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
                else if (escopo_atual != null) escopo_atual.addSimbolo(new Simbolo(id, Simbolo.VARIAVEL, tipo));
                else if (constVar.getIdentificadorGeneral(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
                else constVar.inserirGeneral(new Simbolo(id, Simbolo.VARIAVEL, tipo));

                String valor_tipo = Valor();

                if (tipo != null && valor_tipo != null && !tipo.equals(valor_tipo)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis de atribuição (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ valor_tipo+").", token.getnLinha()));

                Var4(tipo);
            }
            else if (token != null && token.getLexema().equals("[")) {
                Vetor3(tipo, id, linha);
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Var2", token.getLexema() +" não esperado", token.getnLinha() ));
                sincronizar("Var2#GeraFuncaoeProcedure#Start","Var2", null);
                if (token != null){
                    if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                        Var2(tipo);
                    }
                    else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                        GeraFuncaoeProcedure();
                    }
                    else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                        Start(0);
                    }
                }
            }
        }
    }

    //<IdVar> ::= Id <Var2>
    public void IdVar(String tipo){
        if (token != null && token.getTipo() == 3){
            String id = token.getLexema();
            int linha = token.getnLinha();
            nextToken();
            Var2(tipo, id, linha);
        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("IdVar", "Esperava um identificador mas encontrou "+token.getLexema(),  token.getnLinha()));
            sincronizar("Var2#GeraFuncaoeProcedure#Start","IdVar", null);
            if (token != null){
                if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                    Var2(tipo);
                }
                else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                    GeraFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start(0);
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("IdVar", "EOF inesperado", linhaErroEOF));
        }
    }

    //<TipoVar> ::= <Tipo> <IdVar>
    public void TipoVar(){
        String tipo = Tipo();
        IdVar(tipo);
    }

    //<Var> ::= 'var' '{' <TipoVar> | <>
    public void Var(){

        if (token != null && token.getLexema().equals("var")){
            nextToken();
            if (token != null && token.getLexema().equals("{")){
                nextToken();
                TipoVar();
            }
        }

    }


    public void executarAnalise() {
        nextToken();
        if (this.token != null){
            Const();
            struct();
            Var();
            GeraFuncaoeProcedure();
            Start(0);
            if (FLAGERRO == 0 && token != null) addErroSintatico(new ErroSintatico("Inicio", "Esperava EOF mas encontrou "+token.getLexema(),token.getnLinha()));
        }
        else
            System.out.println("A lista de tokens está vazia!");
    }

    // <Const> ::= 'const' ' { ' <TipoConst> | <>
    private void Const() {

        if (token != null && token.getLexema().equals("const")) {
            nextToken();
            if (token != null && token.getLexema().equals("{")) {
                nextToken();
                TipoConst();
            } else if (token != null) {
                addErroSintatico(new ErroSintatico("Const", "Esperava { mas encontrou " + token.getLexema(), token.getnLinha()));
                sincronizar("TipoConst#IdConst#Const2#Const3", "Const", null);
                if (token != null) {
                    if (pertence(0, "TipoConst"))
                        TipoConst();
                    else if (pertence(0, "IdConst"))
                        IdConst(null);
                    else if (pertence(0, "Const2"))
                        Const2(null);
                    else if (pertence(0, "Const3"))
                        Const3();
                    else return;
                }
            }
        }
    }

    // <TipoConst> ::= <Tipo> <IdConst>
    private void TipoConst() {
        String tipo = Tipo();
        IdConst(tipo);
    }

    // <IdConst> ::= Id <Valor> <Const2>
    private void IdConst(String tipo) {
        if (token != null && token.getTipo() == 3) {
            String id = token.getLexema();
            int linha = token.getnLinha();

            nextToken();

            if (escopo_atual != null && escopo_atual.getSimbolo(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
            else if (escopo_atual != null) escopo_atual.addSimbolo(new Simbolo(id, Simbolo.CONSTANTE, tipo));
            else if (constVar.getIdentificadorGeneral(id) != null) addErroSemantico(new ErroSemantico("Identificador já foi declarado", id + " já foi declarado", linha));
            else constVar.inserirGeneral(new Simbolo(id, Simbolo.CONSTANTE, tipo));

            String valor_tipo = Valor();

            if (tipo != null && valor_tipo != null && !tipo.equals(valor_tipo)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis de atribuição (esperava um valor do tipo " + tipo + " mas encontrou "+ valor_tipo+").", token.getnLinha()));

            Const2(tipo);
        }

    }

    // <Const2> ::= ' , ' <IdConst> | ' ; ' <Const3>
    private void Const2(String tipo) {
        if (token != null && token.getLexema().equals(",")) {
            nextToken();
            IdConst(tipo);
        }
        else if (token != null && token.getLexema().equals(";")) {
            nextToken();
            Const3();
        }

    }

    // <Const3> ::= ' } ' | <TipoConst>
    private void Const3() {
        if (token != null && token.getLexema().equals("}"))
            nextToken();
        else if (token != null && pertence(0, "TipoConst"))
            TipoConst();

    }


    // <ExpressaoLogicaRelacional> ::= <ExpressaoLR> | '(' <ExpressaoLR> ')' <ExpressaoLR3>
    private void ExpressaoLogicaRelacional() {

        if (token != null && pertence(0, "ExpressaoLR")) {
            ExpressaoLR();
        }
        else if (token != null && token.getLexema().equals("(")) {
            nextToken();
            ExpressaoLR();

            if (token != null && token.getLexema().equals(")")) {
                nextToken();
                ExpressaoLR3();
            }
        }
  }



    // <ExpressaoLR> ::= <ArgumentoLR2> <ExpressaoLR2> | <ArgumentoLR3> <OperadorRelacional> <ArgumentoLR> <ExpressaoLR3>
    private void ExpressaoLR() {

        if (token != null && pertence(0, "ArgumentoLR2")) {
            ArgumentoLR2();
            ExpressaoLR2("boolean");
        }
        else if (token != null && pertence(0, "ArgumentoLR3")) {
            String tipo = ArgumentoLR3();
            OperadorRelacional();
            String tipo2 = ArgumentoLR();

            if (tipo != null && tipo2 != null && !tipo.equals(tipo2)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão lógica e relacional (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo2 +").", token.getnLinha()));

            ExpressaoLR3();
        }

    }

    // <ExpressaoLR2> ::= <OperadorRelacional> <ArgumentoLR> <ExpressaoLR3> | <ExpressaoLR3>
    private void ExpressaoLR2(String tipo) {

        if (token != null && pertence(0, "OperadorRelacional")) {
            OperadorRelacional();
            String tipo2 = ArgumentoLR();

            if (tipo != null && tipo2 != null && !tipo.equals(tipo2)) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão lógica e relacional (esperava um valor do tipo " + tipo + " mas encontrou do tipo "+ tipo2 +").", token.getnLinha()));

            ExpressaoLR3();
        }
        else if (token != null) {
            ExpressaoLR3();
        }
        else addErroSintatico(new ErroSintatico("ExpressaoLR2", "EOF inesperado", linhaErroEOF));
    }

    // <ExpressaoLR3> ::= <OperadorLogico> <ExpressaoLogicaRelacional> | <>
    private void ExpressaoLR3() {

        if (token != null && pertence(0, "OperadorLogico")) {
            OperadorLogico();
            ExpressaoLogicaRelacional();
        }
        else if (token == null) addErroSintatico(new ErroSintatico("ExpressaoLR3", "EOF inesperado", linhaErroEOF));
    }

    // <ArgumentoLR> ::= <ArgumentoLR2> | <ArgumentoLR3>
    private String ArgumentoLR() {
        String tipo = "boolean";

        if (token != null && pertence(0, "ArgumentoLR2")) {
            ArgumentoLR2();
        }
        else if (token != null && pertence(0, "ArgumentoLR3")) {
            tipo = ArgumentoLR3();
        }

        return tipo;
    }

    // <ArgumentoLR2> ::= '!' <ArgumentoLR2_1> | true | false
    private void ArgumentoLR2() {

        if (token != null && token.getLexema().equals("!")) {
            nextToken();
            ArgumentoLR2_1();
        }
        else if (token != null && token.getLexema().equals("true"))
            nextToken();
        else if (token != null && token.getLexema().equals("false"))
            nextToken();

    }

    // <ArgumentoLR2_1> ::= Id | true | false
    private void ArgumentoLR2_1() {

        if (token != null) {
            if (token.getTipo() == 3) {
                Simbolo simbolo = Identificador();
                if (simbolo != null && !simbolo.getTipo().equals("boolean")) addErroSemantico(new ErroSemantico("Tipos incompatíveis", "Tipos incompatíveis na expressão aritmética (esperava um valor do tipo boolean mas encontrou do tipo "+ simbolo.getTipo() +").", token.getnLinha()));;
            }
            else if (token.getLexema().matches("^(true|false)$"))
                nextToken();

        }

    }

    // <ArgumentoLR3> ::= String | <ExpressaoAritmetica>
    private String ArgumentoLR3() {
        String tipo = null;

        if (token != null && token.getTipo() == 11) {
            tipo = "string";
            nextToken();
        }
        else if (token != null && pertence(0, "ExpressaoAritmetica"))
            tipo = ExpressaoAritmetica();

        return tipo;
    }

    // <OperadorRelacional> ::= '!=' | '==' | '<' | '>' | '<=' | '>='
    private void OperadorRelacional() {

        if (token != null && token.getTipo() == 6)
            nextToken();

    }

    // <OperadorLogico> ::= '&&' | '||'
    private void OperadorLogico() {
        if (token != null && token.getTipo() == 4)
            nextToken();

    }

    // <Read> ::= 'read' '(' <Read1>
    private void Read() {
        if (token != null && token.getLexema().equals("read")) {
            nextToken();
            if (token != null && token.getLexema().equals("(")) {
                nextToken();
                Read1();
            }

        }
    }

    // <Read1> ::= <IdentificadorSemFuncao> <AuxRead>
    private void Read1() {
        IdentificadorSemFuncao();
        AuxRead();
    }

    // <AuxRead> ::= ',' <Read1> | <ReadFim>
    private void AuxRead() {

        if (token != null && token.getLexema().equals(",")) {
            nextToken();
            Read1();
        }
        else if (token != null && pertence(0, "ReadFim"))
            ReadFim();

    }

    // <ReadFim> ::= ')' ';'
    private void ReadFim() {

        if (token != null && token.getLexema().equals(")")) {
            nextToken();
            if (token != null && token.getLexema().equals(";"))
                nextToken();

        }

    }

    // <Corpo>::= <Var> <Corpo2> '}' | '}'
    private void Corpo() {
        Var();
        Corpo2();
        if (token != null && token.getLexema().equals("}")) {
            nextToken();
        }

    }

    // <Corpo2>::= <Comandos> <Corpo2> | <>
    private void Corpo2() {
        if (token != null && pertence(0, "Comandos")) {
            Comandos();
            Corpo2();
        }
    }

    // <Comandos> ::= <Condicional>| <Laco> | <Read> | <Print> | <ComandosReturn> | <IdentificadorComandos>
    private void Comandos() {
        if (pertence(0, "Condicional"))
            Condicional();
        else if (pertence(0, "Laco"))
            Laco();
        else if (pertence(0, "Read"))
            Read();
        else if (pertence(0, "Print"))
            Print();
        else if (pertence(0, "ComandosReturn"))
            ComandosReturn();
        else if (pertence(0, "IdentificadorComandos"))
            IdentificadorComandos();
    }

    // <IdentificadorComandos> ::= <IdentificadorSemFuncao> <IdentificadorComandos2> ';'
    private void IdentificadorComandos() {
        IdentificadorSemFuncao();
        IdentificadorComandos2();
        if (token != null && token.getLexema().equals(";"))
            nextToken();

    }

    // <IdentificadorComandos2>::= '=' <IdentificadorComandos2_1> | '(' <ListaParametros> ')'
    private void IdentificadorComandos2() {
        if (token != null) {
            if (token.getLexema().equals("=")) {
                nextToken();
                IdentificadorComandos2_1();
            }
            else if (token.getLexema().equals("(")) {
                nextToken();
                IdentificadorExtra();
                if (token != null && token.getLexema().equals(")"))
                    nextToken();

            }

        }

    }

    // <IdentificadorComandos2_1> ::= <ExpressaoAritmetica> | String | Boolean
    private void IdentificadorComandos2_1() {
        if (token != null) {
            if (pertence(0, "ExpressaoAritmetica"))
                ExpressaoAritmetica();
            else if (token.getTipo() == 11 || token.getLexema().matches("^(true|false)$"))
                nextToken();

        }

    }

    // <ComandosReturn> ::= 'return' <CodigosRetornos>
    private void ComandosReturn() {
        if (token != null) {
            if (token.getLexema().equals("return")) {
                nextToken();
                CodigosRetornos();
            }

        }

    }

    // <CodigosRetornos>::= ';' | <ExpressaoAritmetica> ';'
    private void CodigosRetornos() {
        if (token != null) {
            if (token.getLexema().equals(";"))
                nextToken();
            else if (pertence(0, "ExpressaoAritmetica")) {
                ExpressaoAritmetica();
                if (token != null && token.getLexema().equals(";"))
                    nextToken();

            }

        }

    }

    // <Start> ::= 'start' '(' ')' '{' <Corpo> '}'
    private void Start(int estagio) {

        int estagio_atual = estagio;
        boolean erro = false;

        if (token!= null && estagio_atual <= 5) {
            switch (estagio) {
                case 0:
                    if (token.getLexema().equals("start")) {
                        nextToken();
                        estagio_atual ++;
                    }
                    else erro = true;
                    break;
                case 1:
                    if (token.getLexema().equals("(")) {
                        nextToken();
                        estagio_atual ++;
                    }
                    else erro = true;
                    break;
                case 2:
                    if (token.getLexema().equals(")")) {
                        nextToken();
                        estagio_atual ++;
                    }
                    else erro = true;
                    break;
                case 3:
                    if (token.getLexema().equals("{")) {
                        nextToken();
                        estagio_atual ++;
                    }
                    else erro = true;
                    break;
                case 4:
                    Corpo();
                    estagio_atual ++;
                    break;
            }

            if (token != null && !erro && estagio_atual <= 4) {
                Start(estagio_atual);
            }
            else if (token != null && erro && estagio_atual <= 4) {
                String lexemas = null;
                String conjuntos_primeiro = null;
                switch (estagio_atual) {
                    case 0:
                        lexemas = "start";
                    case 1:
                        lexemas = (lexemas != null)?lexemas + "#(":"(";
                    case 2:
                        lexemas = (lexemas != null)?lexemas + "#)":")";
                    case 3:
                        lexemas = (lexemas != null)?lexemas + "#{":"{";
                    case 4:
                        conjuntos_primeiro = "Corpo";
                }


                sincronizar(conjuntos_primeiro, null, lexemas);

                if (token != null) {
                    if (pertence(0, "Corpo"))
                        Start(4);
                    else {
                        switch (token.getLexema()) {
                            case "start":
                                Start(0);
                                break;
                            case "(":
                                Start(1);
                                break;
                            case ")":
                                Start(2);
                                break;
                            case "{":
                                Start(3);
                                break;
                        }
                    }
                }

            }
            else if (token != null && estagio_atual > 4) addErroSintatico(new ErroSintatico("Start", "Tokens inesperado após a declaração do Start.", linhaErroEOF));
            else if (token == null && estagio_atual <= 4) addErroSintatico(new ErroSintatico("Start", "EOF inesperado" + estagio_atual, linhaErroEOF));
        }

    }

    /**
     * Método responsável por escrever os erros no arquivo de saída
     * @throws IOException exceção caso ocorra algum erro no processo de escrita do arquivo
     */
    public void escreverEmArquivo(String nameArquivo) throws IOException {
        //Estabelece as variáveis de referências dos output de escrita
        OutputStream arquivo;
        OutputStreamWriter arquivoEscrita;

        //Instancia um objeto file, criando o arquivo de saída correpondente
        File fileSaida = new File("output/"+"saida"+nameArquivo.replaceAll("[^0-9]","")+".txt");
        //Instancia o stream de saída
        arquivo = new FileOutputStream(fileSaida);
        arquivoEscrita = new OutputStreamWriter(arquivo);
        //Recupera o Iterado da lista de tokens
        Iterator it = getListaErrosSintaticos().iterator();

        //Verifica se a lista de tokens não está vazia
        if(!this.getListaErrosSemanticos().isEmpty()){
            //Varre a lista de tokens
            while(it.hasNext()){
                //Escreve no arquivo de saída correpondente
                arquivoEscrita.write(it.next().toString());
            }
        }
        else{
            arquivoEscrita.write("Análise Semântica realizada com Sucesso!");
        }

        //Obriga que os dados que estão no buffer sejam escritos imediatamente
        arquivoEscrita.flush();
        arquivoEscrita.close();
    }

}
