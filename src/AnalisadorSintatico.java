import java.io.*;
import java.util.*;

/**
 * Classe responsável por realizar a análise sintática.
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */
public class AnalisadorSintatico {

    private List<Token> tokens; // Lista de tokens
    private List<ErroSintatico> errosSintaticos; // Lista de erros sintáticos
    private Token token; // Token atual
    private static int FLAGERRO = 0; // Flag para identificar se foi encontrado EOF no meio da análise
    private Conjunto conjunto_P_S; // Conjuntos primeiro e seguinte de todos os não-terminais
    private static  int linhaErroEOF; // Linha onde foi encontrado EOF

     /**
     * Construtor da classe do AnalisadorSintatico.
     * @param tokens recebe uma lista com todos os tokens gerados pela análise léxica
     */
    public AnalisadorSintatico(List<Token> tokens) {

        this.tokens = tokens;
        this.errosSintaticos = new ArrayList<>();
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
     * Acrescenta um erro sintático na lista de erros sintáticos.
     * @param erro sintático.
     */
    private void addErroSintatico(ErroSintatico erro){
        System.out.println(erro.toString());
        if (erro.getMensagem().split(" ")[0].equals("EOF")){
             if (FLAGERRO !=1 ){
                FLAGERRO = 1;
                this.errosSintaticos.add(erro);
            }
        }
        else
            this.errosSintaticos.add(erro);


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
            if (conjunto.contains("Boolean"))
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

    public void F(){

        if (token != null && token.getLexema().equals("(")){
            ExpressaoAritmetica();
        }
        else if (token != null &&  pertence(0,"F")){
            nextToken();
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("F", token.getLexema()+" não esperado", token.getnLinha()));
            sincronizar(null, "F", null);

        }

        if (token == null){
            addErroSintatico(new ErroSintatico("F","EOF inesperado", linhaErroEOF));
        }




    }

    public void T(){

        if (token != null && conjunto_P_S.primeiro("F").contains(token.getLexema()) || pertence(0,"F")){
            F();
            T2();
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("T", token.getLexema()+" não esperado", token.getnLinha()));
            sincronizar("T2", "T", null);
            if (token != null) {
                if (conjunto_P_S.primeiro("T2").contains(token.getLexema())) {
                    T2();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("T","EOF inesperado", linhaErroEOF));
        }



    }

    //<T2> ::= '*' <ExpressaoAritmetica> | '/' <ExpressaoAritmetica> | <>
    public void T2(){

        if (token != null) {
            if (token.getLexema().equals("*") || token.getLexema().equals("/")) {
                nextToken();
                ExpressaoAritmetica();
            }
        }
        else addErroSintatico(new ErroSintatico("T2","EOF inesperado", linhaErroEOF));

    }

    public void E2(){
        if (token != null) {
            if (token.getLexema().equals("+") || token.getLexema().equals("-")) {
                nextToken();
                ExpressaoAritmetica();
            }
        }
        else addErroSintatico(new ErroSintatico("T2","EOF inesperado", linhaErroEOF));
    }

    //<IdentificadorAritmetico> ::= <Escopo> Id <Identificador2> <ExpressaoAritmetica2>
    //    | Id <IdentificadorAritmetico3>
    public void IdentificadorAritmetico(){
        if (token != null && conjunto_P_S.primeiro("Escopo").contains(token.getLexema())){
            Escopo();
            if (token != null && token.getTipo() == 3){
                nextToken();
                System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu "+token.getLexema());
                Identificador2();
                System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu "+token.getLexema());
                ExpressaoAritmetica2();
                System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu "+token.getLexema());

            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("IdentificadorAritmetico", "Esperava um Identificador, mas encontrou "+token.getLexema(), token.getnLinha()));
                sincronizar("Identificador2#ExpressaoAritmetica2", "IdentificadorAritmetico", null);
                if (token != null) {
                    if (conjunto_P_S.primeiro("Identificador2").contains(token.getLexema())) {
                        Identificador2();
                    }
                    else  if (conjunto_P_S.primeiro("ExpressaoAritmetica2").contains(token.getLexema())) {
                        ExpressaoAritmetica2();
                    }
                }
            }

        }
        else if (token != null && token.getTipo() == 3) {
            nextToken();
            IdentificadorAritmetico3();

        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("IdentificadorAritmetico", token.getLexema()+" não esperado", token.getnLinha()));
            sincronizar(null,"IdentificadorAritmetico", null);
         }

        if (token == null){
            addErroSintatico(new ErroSintatico("T2","EOF inesperado", linhaErroEOF));
        }


    }

    //<IdentificadorAritmetico3> ::= <Identificador2> <ExpressaoAritmetica2> | '(' <ListaParametros> ')' <T2> <E2>
    public void IdentificadorAritmetico3(){
        System.out.println("Tokennnnnnnnnnnnnnnnnnnnnnnnnnnn-------------"+token.getLexema());
        if (token != null && pertence(0, "Identificador2")){
            Identificador2();
            ExpressaoAritmetica2();
        }
        else if (token != null && token.getLexema().equals("(")){
            nextToken();
            ListaParametros();
            if (token != null && token.getLexema().equals(")")){
                nextToken();
                T2();
                E2();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("IdentificadorAritmetico3", "Esperava (, mas encontrou "+token.getLexema(), token.getnLinha()));
                sincronizar("T2", "IdentificadorAritmetico3", null);
                if (token != null) {
                    if (conjunto_P_S.primeiro("T2").contains(token.getLexema())) {
                        Identificador2();
                    }
                    else  if (conjunto_P_S.primeiro("T2").contains(token.getLexema())) {
                        T2();
                        E2();
                    }
                }
            }
        }


        if (token == null){
            addErroSintatico(new ErroSintatico("F","EOF inesperado", linhaErroEOF));
        }



    }

    public void ExpressaoAritmetica (){
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+token.getLexema());
        if (token != null && conjunto_P_S.primeiro("T").contains(token.getLexema()) || pertence(0, "T")){
            T();
            E2();

        }
        else if (token != null && conjunto_P_S.primeiro("IdentificadorAritmetico").contains(token.getLexema()) || token.getTipo() == 3){
            IdentificadorAritmetico();

        }
        else if (token != null && token.getTipo() == 9){
            IdentificadorSemFuncao();
            T2();
            E2();
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("ExpressaoAritmetica", token.getLexema()+" não esperado", token.getnLinha()));
            sincronizar("E2", "ExpressaoAritmetica", null);
            if (token != null) {
                if (conjunto_P_S.primeiro("E2").contains(token.getLexema())) {
                    E2();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("ExpressaoAritmetica","EOF inesperado", linhaErroEOF));
        }


    }

    //<ExpressaoAritmetica2> ::= '++' <T2> <E2> | '--' <T2> <E2> | <T2> <E2>
    private void ExpressaoAritmetica2() {
        if(token != null) {
            if(token.getTipo() == 9){
                nextToken();
                T2();
                E2();
            }
            else if (conjunto_P_S.primeiro("T2").contains(token.getLexema())){
                T2();
                E2();

            }


        }
        else addErroSintatico(new ErroSintatico("ExpressaoAritmetica2","EOF inesperado", linhaErroEOF));
    }



    public void IdentificadorSemFuncao(){

        if(token != null && conjunto_P_S.primeiro("Escopo").contains(token.getLexema())){
            Escopo();
            if (token != null && token.getTipo() == 3){
                nextToken();
                Identificador2();
            }
        }
        else  if (token != null && token.getTipo() == 3){
            nextToken();
            Identificador2();
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("IdentificadorSemFuncao", token.getLexema()+" não esperado", token.getnLinha()));
            sincronizar("Identificador2", "IdentificadorSemFuncao", null);
            if (token != null) {
                if (conjunto_P_S.primeiro("Identificador2").contains(token.getLexema())) {
                    Identificador2();
                }
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("IdentificadorSemFuncao","EOF inesperado", linhaErroEOF));
        }

    }





    public void IndiceVetor(){

        if (token != null && token.getTipo() == 2 && Integer.parseInt(token.getLexema()) >= 0) {
            nextToken();
        }
        else if (token != null && conjunto_P_S.primeiro("Identificador").contains(token.getLexema())){
            Identificador();
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("IndiceVetor", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "IndiceVetor", null);

        }

        if (token == null){
            addErroSintatico(new ErroSintatico("IndiceVetor","EOF inesperado", linhaErroEOF));
        }

    }
    //<Struct> ::= 'typedef' 'struct' Id <Extends> <TipoStruct> <Struct> | <>
    public void struct(){
        if(token != null && token.getLexema().equals("typedef")){
            nextToken();
            if ( token != null && token.getLexema().equals("struct")){
                nextToken();
                if (token != null && token.getTipo() == 3){ //Id
                    nextToken();
                    Extend();
                    TipoStruct();
                    struct();
                } else if (token != null){
                    addErroSintatico(new ErroSintatico("Struct", token.getLexema() +" não esperado", token.getnLinha()));
                    sincronizar("Extends", "Struct", ";");
                    if(token != null && pertence(0, "Extends")){
                        Extend();
                        TipoStruct();
                        struct();
                    }else if( token != null && token.getLexema().equals(";")){
                        nextToken();
                        Struct3();
                    }
                }
            }else if (token != null){
                addErroSintatico(new ErroSintatico("Struct", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar("Extends", "Struct", ";");
                if(token != null && pertence(0, "Extends")){
                    Extend();
                    TipoStruct();
                    struct();
                }else if(token != null && token.getLexema().equals(";")){
                    nextToken();
                    Struct3();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Struct","EOF inesperado", linhaErroEOF));
        }

    }
    //<Extends> ::= 'extends' Id '{' | '{'
    public void Extend(){
        if(token != null && token.getLexema().equals("extends")){
            nextToken();
            if (token != null && token.getTipo() == 3){
                nextToken();
                if (token != null && token.getLexema().equals("{")){
                    nextToken();
                } else if(token != null){
                    addErroSintatico(new ErroSintatico("Extends", token.getLexema() +" não esperado", token.getnLinha()));
                    sincronizar(null, "Extends", ";");

                    if(token != null && token.getLexema().equals(";")){
                        nextToken();
                        Struct3();
                    }
                }
            }else if(token != null){
                addErroSintatico(new ErroSintatico("Extends", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar(null, "Extends", ";");
                if(token != null && token.getLexema().equals(";")){
                    nextToken();
                    Struct3();
                }
            }
        } else if(token != null && token.getLexema().equals("{")){
            nextToken();
        }else if(token != null){
            addErroSintatico(new ErroSintatico("Extends", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "Extends", ";");
            if(token != null && token.getLexema().equals(";")){
                nextToken();
                Struct3();
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Extends","EOF inesperado", linhaErroEOF));
        }


    }
    //<TipoStruct> ::= <Tipo> <IdStruct>
    public void TipoStruct(){
        Tipo();
        IdStruct();
    }
    //<IdStruct> ::= Id <Struct2>
    public void IdStruct(){
        if(token != null && token.getTipo() == 3){
            nextToken();
            Struct2();
        } else if(token != null){
            addErroSintatico(new ErroSintatico("IdStruct", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar("Struct2", "IdStruct", ";");
            if(token != null && token.getLexema().equals(";")){
                nextToken();
                Struct3();
            }else if(token != null && pertence(0, "Struct2")){
                Struct2();
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("IdStruct","EOF inesperado", linhaErroEOF));
        }

    }

    //<Struct2> ::= ',' <IdStruct> | ';' <Struct3>
    public void Struct2(){
        if(token != null && token.getLexema().equals(",")){
            nextToken();
            IdStruct();
        } else if(token != null && token.getLexema().equals(";")){
            nextToken();
            Struct3();
        } else if(token != null){
            addErroSintatico(new ErroSintatico("Struct2", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar("Struct3", "Struct2", ";");
            if(token != null && pertence(0, "Struct3")){
                Struct3();
            } else if(token != null && token.getLexema().equals(";")){
                nextToken();
                Struct3();
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Struct2","EOF inesperado", linhaErroEOF));
        }
    }

    //<Struct3> ::= '}' | <TipoStruct>
    public void Struct3(){
        if(token != null && token.getLexema().equals("}")){
            nextToken();
        }else if (token != null && pertence(0, "TipoStruct")){
            TipoStruct();
        }else if(token != null){
            addErroSintatico(new ErroSintatico("Struct3", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "Struct3", null);

        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Struct3","EOF inesperado", linhaErroEOF));
        }
    }

    public void Valor(){
        if(token != null && pertence(0, "Valor")){
            nextToken();
            System.out.println("Retorno 4"+ token.getLexema());
        } else if (token != null){
            addErroSintatico(new ErroSintatico("Valor", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "Valor", null);
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Valor","EOF inesperado", linhaErroEOF));
        }
    }
//<ValorVetor> ::= IntPos | Id
    public void ValorVetor(){
        if(token != null && pertence(0, "ValorVetor")){
            nextToken();
        } else if (token != null) {
            addErroSintatico(new ErroSintatico("ValorVetor", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "ValorVetor", null);
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("ValorVetor","EOF inesperado", linhaErroEOF));
        }
    }

    public void Tipo(){
        if(token != null && token.getLexema().equals("int")){
            nextToken();
        } else if(token != null && token.getLexema().equals("boolean")){
            nextToken();
        } else if (token != null && token.getLexema().equals("string")){
            nextToken();
        } else if (token != null && token.getLexema().equals("real")){
            nextToken();
        } else if(token != null && token.getTipo() == 3){
            nextToken();
        } else if (token != null){
            addErroSintatico(new ErroSintatico("Tipo", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "Tipo", null);
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Tipo","EOF inesperado", linhaErroEOF));
        }
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
                        } else if (token != null) {
                            addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                            sincronizar(null, "Laco", null);
                        }
                    } else if (token != null) {
                        addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                        sincronizar("Corpo2", "Laco", null);
                        if (token != null && conjunto_P_S.primeiro("Corpo2").contains(token.getLexema())) {
                            Corpo2();
                            if (token != null && token.getLexema().equals("}")) {
                                nextToken();
                            } else if (token != null) {
                                addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                                sincronizar(null, "Laco", null);
                            }
                        }
                    }
                } else if (token != null) {
                    addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                    sincronizar("Corpo", "Laco", "{");
                    if (token != null && token.getLexema().equals("{")) {
                        nextToken();
                        Corpo2();
                        if (token != null && token.getLexema().equals("}")) {
                            nextToken();
                        } else {
                            addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                            sincronizar("Corpo", "Laco", null);
                        }
                    } else if (token != null && pertence(0, "Corpo2")) {
                        Corpo2();
                        if (token != null && token.getLexema().equals("}")) {
                            nextToken();
                        } else if (token != null) {
                            addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                            sincronizar(null, "Laco", null);
                        }
                    }
                }
            } else if (token != null) {
                addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                sincronizar("expressaoLogicaRelacional", "Laco", null);
                if (token != null && pertence(0, "expressaoLogicaRelacional")) {
                    ExpressaoLogicaRelacional();
                    if (token != null && token.getLexema().equals(")")) {
                        nextToken();
                        if (token != null && token.getLexema().equals("{")) {
                            nextToken();
                            Corpo2();
                            if (token != null && token.getLexema().equals("}")) {
                                nextToken();
                            } else if (token != null) {
                                addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                                sincronizar(null, "Laco", null);
                            }
                        } else if (token != null) {
                            addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                            sincronizar("Corpo2", "Laco", null);
                            if (token != null && pertence(0, "Corpo2")) {
                                Corpo2();
                                if (token != null && token.getLexema().equals("}")) {
                                    nextToken();
                                } else if (token != null) {
                                    addErroSintatico(new ErroSintatico("Laco", token.getLexema() + " não esperado", token.getnLinha()));
                                    sincronizar(null, "Laco", null);
                                }
                            }
                        }
                    }
                }
            } else if (token != null) {
                addErroSintatico(new ErroSintatico("Laco", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar(null, "Laco", "while");
                if (token != null && token.getLexema().equals("while")) {
                    Laco();
                }
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Laco","EOF inesperado", linhaErroEOF));
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
    if (token == null){
        addErroSintatico(new ErroSintatico("GeraFuncaoeProcedure","EOF inesperado", linhaErroEOF));
    }
}

// <Funcao> ::= 'function' <Tipo> Id '(' <Parametro>
public void Funcao(){
    if(token != null && token.getLexema().equals("function")){
        nextToken();
        Tipo();
        if(token != null && token.getTipo() == 3){
            nextToken();
            if(token != null && token.getLexema().equals("(")){
                nextToken();
                Parametro();
            }else if(token != null){
                addErroSintatico(new ErroSintatico("Funcao", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar("Parametro", "Funcao", null);
                if (token != null && pertence(0, "Parametro")){
                    Parametro();
                }
            }
        }else if (token != null){
            addErroSintatico(new ErroSintatico("Funcao", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar("Parametro", "Funcao", null);
            if (token != null && pertence(0, "Parametro")){
                Parametro();
            }
        }
    }else if (token != null){
        addErroSintatico(new ErroSintatico("Funcao", token.getLexema() +" não esperado", token.getnLinha()));
        sincronizar(null, "Funcao", "function");
        if (token != null && token.getLexema().equals("function")){
            Funcao();
        }
    }
    if (token == null){
        addErroSintatico(new ErroSintatico("Funcao","EOF inesperado", linhaErroEOF));
    }
}

//<Procedimento> ::= 'procedure' Id '(' <Parametro>
public void Procedimento(){
    if(token != null && token.getLexema().equals("procedure")){
        nextToken();
        if(token != null && token.getTipo() == 3){
            nextToken();
            if(token != null && token.getLexema().equals("(")){
                nextToken();
                Parametro();
            }else if(token != null){
                addErroSintatico(new ErroSintatico("Procedimento", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar("Parametro", "Procedimento", null);
                if (token != null && pertence(0, "Parametro")){
                    Parametro();
                }
            }
        }else if (token != null){
            addErroSintatico(new ErroSintatico("Procedimento", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar("Parametro", "Procedimento", null);
            if (token != null && pertence(0, "Parametro")){
                Parametro();
            }
        }
    }else if (token != null){
        addErroSintatico(new ErroSintatico("Procedimento", token.getLexema() +" não esperado", token.getnLinha()));
        sincronizar(null, "Procedimento", "procedure");
        if (token != null && token.getLexema().equals("procedure")){
            Procedimento();
        }
    }
    if (token == null){
        addErroSintatico(new ErroSintatico("Procedimento","EOF inesperado", linhaErroEOF));
    }
}
//<Parametro> ::=  <Tipo> Id <Para2> <Para1>
    public void Parametro(){
        Tipo();
        if(token != null && token.getTipo() == 3){
            nextToken();
            Para2();
            Para1();
        } else if (token != null){
            addErroSintatico(new ErroSintatico("Parametro", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar("Para1", "Parametro", null);
            if(token != null && pertence(0, "Para1")){
                Para1();
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Parametro","EOF inesperado", linhaErroEOF));
        }
    }


    public void Para1(){
        if(token != null && token.getLexema().equals(",")){
            nextToken();
            Parametro();
        }else if(token != null && token.getLexema().equals(")")){
            nextToken();
            F2();
        }else if (token != null){
            addErroSintatico(new ErroSintatico("Para1", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "Para1", null);
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Para1","EOF inesperado", linhaErroEOF));
        }
    }

    public void Para2(){
        if(token != null && token.getLexema().equals("[")){
            nextToken();
            if(token != null && token.getLexema().equals("]")){
                nextToken();
                Para3();
            }else if(token != null){
                addErroSintatico(new ErroSintatico("Para2", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar("Para3", "Para2", null);
                if(token != null && pertence(0, "Para3")){
                    Para3();
                }
            }

        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Para2","EOF inesperado", linhaErroEOF));
        }
    }

    public void Para3(){
        if(token != null && token.getLexema().equals("[")){
            nextToken();
            if(token != null && token.getLexema().equals("]")){
                nextToken();
            }else{
                addErroSintatico(new ErroSintatico("Para3", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar(null, "Para3", null);
            }

        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Para3","EOF inesperado", linhaErroEOF));
        }
    }

    public void F2(){
        if(token != null && token.getLexema().equals("{")){
            nextToken();
            Corpo();
        }else if(token != null){
            addErroSintatico(new ErroSintatico("F2", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "F2", null);
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("F2","EOF inesperado", linhaErroEOF));
        }
    }

    public void Print(){
        if(token != null && token.getLexema().equals("print")){
            nextToken();
            if(token != null && token.getLexema().equals("(")){
                nextToken();
                Print1();
            }else if (token != null){
                addErroSintatico(new ErroSintatico("Print", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar("Print1", "Print", null);
                if (token != null && pertence(0, "Print1")){
                    Print1();
                }
            }
        }else if (token != null){
            addErroSintatico(new ErroSintatico("Print", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar("Print1", "Print", "print");
            if (token != null && pertence(0, "Print1")){
                Print1();
            } else if (token.getLexema().equals("print")){
                Print();
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Print","EOF inesperado", linhaErroEOF));
        }
    }

    public void Print1(){
        if(token != null && pertence(0, "Print1")){
            nextToken();
            AuxPrint();
        }else if(token != null && pertence(0, "Identificador")){
            Identificador();
            AuxPrint();
        }else if (token != null){
            addErroSintatico(new ErroSintatico("Print1", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar("AuxPrint", "Print1", null);
            if (token != null && pertence(0, "AuxPrint")){
                AuxPrint();
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Print1","EOF inesperado", linhaErroEOF));
        }
    }

    public void AuxPrint(){
        if(token != null && token.getLexema().equals(",")){
            nextToken();
            Print1();
        }else if(token != null && pertence(0, "PrintFim")){
            PrintFim();
        }else if (token != null){
            addErroSintatico(new ErroSintatico("AuxPrint", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar("Print1", "AuxPrint", null);
            if (token != null && pertence(0, "Print1")){
                Print1();
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("AuxPrint","EOF inesperado", linhaErroEOF));
        }
    }

    public void PrintFim(){
        if(token != null && token.getLexema().equals(")")){
            nextToken();
            if(token != null && token.getLexema().equals(";")){
                nextToken();
            }else if (token != null){
                addErroSintatico(new ErroSintatico("PrintFim", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar(null, "PrintFim", null);

            }
        }else if (token != null){
            addErroSintatico(new ErroSintatico("PrintFim", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "PrintFim", null);

        }
        if (token == null){
            addErroSintatico(new ErroSintatico("PrintFim","EOF inesperado", linhaErroEOF));
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
                }else if(token != null){
                    addErroSintatico(new ErroSintatico("CondEnd", token.getLexema() +" não esperado", token.getnLinha()));
                    sincronizar(null, "CondEnd", null);
                }
            }else if(token != null){
                addErroSintatico(new ErroSintatico("CondEnd", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar("Corpo2", "CondEnd", null);
                if(token != null && pertence(0, "Corpo2")){
                    Corpo2();
                    if(token != null && token.getLexema().equals("}")){
                        nextToken();
                    }else if(token != null){
                        addErroSintatico(new ErroSintatico("CondEnd", token.getLexema() +" não esperado", token.getnLinha()));
                        sincronizar(null, "CondEnd", null);
                    }
                }
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("CondEnd","EOF inesperado", linhaErroEOF));
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
                            }else if (token != null){
                                addErroSintatico(new ErroSintatico("Condicional", token.getLexema() +" não esperado", token.getnLinha()));
                                sincronizar("CondEnd", "Condicional", null);
                                if(token != null && pertence(0, "CondEnd")){
                                    CondEnd();
                                }

                            }
                        }else if (token != null){
                            addErroSintatico(new ErroSintatico("Condicional", token.getLexema() +" não esperado", token.getnLinha()));
                            sincronizar("Corpo2", "Condicional", null);
                            if(token != null && pertence(0, "Corpo2")){
                                Corpo2();
                                if(token != null && token.getLexema().equals("}")){
                                    nextToken();
                                    CondEnd();
                                }else if (token != null){
                                    addErroSintatico(new ErroSintatico("Condicional", token.getLexema() +" não esperado", token.getnLinha()));
                                    sincronizar("CondEnd", "Condicional", null);
                                    if(token != null && pertence(0, "CondEnd")){
                                        CondEnd();
                                    }

                                }
                            }
                        }
                    }else if(token != null){
                        addErroSintatico(new ErroSintatico("Condicional", token.getLexema() +" não esperado", token.getnLinha()));
                        sincronizar("Corpo2", "Condicional", null);
                        if(token != null && pertence(0, "Corpo2")){
                            Corpo2();
                            if(token != null && token.getLexema().equals("}")){
                                nextToken();
                                CondEnd();
                            }else if (token != null){
                                addErroSintatico(new ErroSintatico("Condicional", token.getLexema() +" não esperado", token.getnLinha()));
                                sincronizar("CondEnd", "Condicional", null);
                                if(token != null && pertence(0, "CondEnd")){
                                    CondEnd();
                                }

                            }
                        }
                    }
                }else if(token != null){
                    addErroSintatico(new ErroSintatico("Condicional", token.getLexema() +" não esperado", token.getnLinha()));
                    sincronizar("Corpo2", "Condicional", null);
                    if(token != null && pertence(0, "Corpo2")){
                        Corpo2();
                        if(token != null && token.getLexema().equals("}")){
                            nextToken();
                            CondEnd();
                        }else if (token != null){
                            addErroSintatico(new ErroSintatico("Condicional", token.getLexema() +" não esperado", token.getnLinha()));
                            sincronizar("CondEnd", "Condicional", null);
                            if(token != null && pertence(0, "CondEnd")){
                                CondEnd();
                            }

                        }
                    }
                }
            }else if(token != null){
                addErroSintatico(new ErroSintatico("Condicional", token.getLexema() +" não esperado", token.getnLinha()));
                sincronizar("ExpressaoLogicaRelacional", "Condicional", null);
                if(token != null && pertence(0, "ExpressaoLogicaRelacional")){
                    ExpressaoLogicaRelacional();
                    if(token != null && token.getLexema().equals(")")) {
                        nextToken();
                        if (token != null && token.getLexema().equals("then")) {
                            nextToken();
                            if (token != null && token.getLexema().equals("{")) {
                                nextToken();
                                Corpo2();
                                if (token != null && token.getLexema().equals("}")) {
                                    nextToken();
                                    CondEnd();
                                } else if (token != null) {
                                    addErroSintatico(new ErroSintatico("Condicional", token.getLexema() + " não esperado", token.getnLinha()));
                                    sincronizar("CondEnd", "Condicional", null);
                                    if (token != null && pertence(0, "CondEnd")) {
                                        CondEnd();
                                    }

                                }
                            } else if (token != null) {
                                addErroSintatico(new ErroSintatico("Condicional", token.getLexema() + " não esperado", token.getnLinha()));
                                sincronizar("Corpo2", "Condicional", null);
                                if (token != null && pertence(0, "Corpo2")) {
                                    Corpo2();
                                    if (token != null && token.getLexema().equals("}")) {
                                        nextToken();
                                        CondEnd();
                                    } else if (token != null) {
                                        addErroSintatico(new ErroSintatico("Condicional", token.getLexema() + " não esperado", token.getnLinha()));
                                        sincronizar("CondEnd", "Condicional", null);
                                        if (token != null && pertence(0, "CondEnd")) {
                                            CondEnd();
                                        }

                                    }
                                }
                            }
                        } else if (token != null) {
                            addErroSintatico(new ErroSintatico("Condicional", token.getLexema() + " não esperado", token.getnLinha()));
                            sincronizar("Corpo2", "Condicional", null);
                            if (token != null && pertence(0, "Corpo2")) {
                                Corpo2();
                                if (token != null && token.getLexema().equals("}")) {
                                    nextToken();
                                    CondEnd();
                                } else if (token != null) {
                                    addErroSintatico(new ErroSintatico("Condicional", token.getLexema() + " não esperado", token.getnLinha()));
                                    sincronizar("CondEnd", "Condicional", null);
                                    if (token != null && pertence(0, "CondEnd")) {
                                        CondEnd();
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }else if (token != null){
            addErroSintatico(new ErroSintatico("Condicional", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar(null, "Condicional", "if");
            if(token != null && token.getLexema().equals("if")){
                Condicional();
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Condicional","EOF inesperado", linhaErroEOF));
        }
    }



public void Vetor2(){

        if (token != null && token.getLexema().equals("[")){
            nextToken();
            IndiceVetor();
            if (token != null && token.getLexema().equals("]")){
                nextToken();
            }else if (token != null) {
                addErroSintatico(new ErroSintatico("Vetor2", "Esperava ] mas encontrou " + token.getLexema(), token.getnLinha()));
                sincronizar(null, "Vetor2", null);
             }
        }
//        else if (token != null && !conjunto_P_S.seguinte("Vetor2").contains(token.getLexema()) && !pertence(1,"Vetor2")){
//            addErroSintatico(new ErroSintatico("Vetor", token.getLexema()+" não esperado" + token.getLexema(), token.getnLinha()));
//            sincronizar("Vetor", "Vetor2", null);
//        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Vetor2","EOF inesperado", linhaErroEOF));
        }

    }

    //<Vetor> ::= '[' <IndiceVetor> ']' <Vetor2> <Identificador4>
    //    | <Identificador4>
    public void Vetor(){

        if(token != null && token.equals("[")){
            nextToken();
            IndiceVetor();
            if (token != null && token.equals("]")){
                nextToken();
                Vetor2();
                Identificador4();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Vetor", "Esperava ] mas encontrou " + token.getLexema(), token.getnLinha()));
                sincronizar("Vetor2#Identificador4", "Vetor", null);
                if (token != null) {
                    if (conjunto_P_S.primeiro("Vetor2").contains(token.getLexema())) {
                        Vetor2();
                    }
                    else if (conjunto_P_S.primeiro("Identificador4").contains(token.getLexema())) {
                        Identificador4();
                    }
                }

            }

        } else if ( token != null && conjunto_P_S.primeiro("Identificador4").contains(token.getLexema())){
            Identificador4();
        }
//        else if (token != null && !conjunto_P_S.seguinte("Vetor").contains(token.getLexema()) && !pertence(1,"Vetor")){
//            addErroSintatico(new ErroSintatico("Vetor", token.getLexema()+" não esperado" + token.getLexema(), token.getnLinha()));
//            sincronizar("Vetor", "Vetor", null);
//            if (token != null) {
//                if (conjunto_P_S.primeiro("Vetor").contains(token.getLexema())) {
//                    Vetor();
//                }
//
//            }
//        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Vetor","EOF inesperado", linhaErroEOF));
        }


    }

    public void Identificador4() {

        if (token != null && token.getLexema().equals(".")) {
            nextToken();
            if (token != null && token.getTipo() == 3) {
                nextToken();
                Vetor();

            } else if (token != null) {
                addErroSintatico(new ErroSintatico("Identificador4", "Esperava um Identificador mas encontrou " + token.getLexema(), token.getnLinha()));
                sincronizar("Vetor", "Identificador4", null);
                if (token != null) {
                    if (conjunto_P_S.primeiro("Vetor").contains(token.getLexema())) {
                        Vetor();
                    }
                }

            }
        }
//        else if (token != null && !conjunto_P_S.seguinte("Identificador4").contains(token.getLexema()) && !pertence(1,"Identificador4")){
//            addErroSintatico(new ErroSintatico("Identificador4", token.getLexema()+"  não esperado", token.getnLinha()));
//            sincronizar("Vetor", "Identificador2", null);
//            if (token != null) {
//                if (conjunto_P_S.primeiro("Vetor").contains(token.getLexema())) {
//                    Vetor();
//                }
//            }
//
//        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Identificador4","EOF inesperado", linhaErroEOF));
        }
    }

    public void Escopo(){
        if (token != null && (token.getLexema().equals("local") || token.getLexema().equals("global"))){
            nextToken();
            if (token != null && token.getLexema().equals(".")){
                nextToken();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Escopo", "Esperava . mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar(null, "Escopo", null);

            }
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("Escopo", "Esperava local ou global mas encontrou "+token.getLexema(),token.getnLinha()));
            sincronizar(null, "Escopo", ".");
            if (token != null && token.equals(".")){
                nextToken();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Escopo", "Esperava . mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar(null, "Escopo", null);

            }

        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Escopo","EOF inesperado", linhaErroEOF));
        }

    }
  //<Identificador2> ::= '.' Id <VetorDeclaracao> | <VetorDeclaracao> | <>
    public void Identificador2(){

        if (token != null && token.getLexema().equals(".")){
            nextToken();
            if (token!= null && token.getTipo() == 3){
                nextToken();
                Vetor();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Identificador2", "Esperava um Identificador mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar("Vetor", "Identificador2", null);
                if (token != null){
                    if (conjunto_P_S.primeiro("Vetor").contains(token.getLexema())){
                        Vetor();
                    }
                }

            }
        }else if (token != null && conjunto_P_S.primeiro("Vetor").contains(token.getLexema())){
            Vetor();
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Identificador2","EOF inesperado", linhaErroEOF));
        }
    }

    public void Identificador3(){

        if (token != null && pertence(0, "Identificador2")){
            Identificador2();
        }
        else if (token != null && token.getLexema().equals("(")){
            nextToken();
            ListaParametros();
            if (token != null && token.getLexema().equals(")")){
                nextToken();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Identificador3", "Esperava ) mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar("Identificador", "Identificador3", null);
                if (token != null){
                    if (conjunto_P_S.primeiro("Identificador").contains(token.getLexema())){
                        Identificador();
                    }
                }

            }
        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("Identificador3", "Esperava ( mas encontrou "+token.getLexema(),token.getnLinha()));
            sincronizar("ListaParametros", "Identificador3", null);
            if (token != null){
                if (conjunto_P_S.primeiro("ListaParametros").contains(token.getLexema())){
                    ListaParametros();
                    if (token != null && token.getLexema().equals(")")){
                        nextToken();
                    }
                    else if (token != null) {
                        addErroSintatico(new ErroSintatico("Identificador3", "Esperava ) mas encontrou "+token.getLexema(),token.getnLinha()));
                        sincronizar("Identificador", "Identificador3", null);
                        if (token != null){
                            if (conjunto_P_S.primeiro("Identificador").contains(token.getLexema())){
                                Identificador();
                            }
                        }

                    }
                }
            }

        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Identificador3","EOF inesperado", linhaErroEOF));
        }




    }


    public void Identificador(){

        if (token != null && conjunto_P_S.primeiro("Escopo").contains(token.getLexema())){
            Escopo();
            if (token != null  && token.getTipo() == 3){
                nextToken();
                Identificador2();
            }
            else if (token != null){
                addErroSintatico(new ErroSintatico("Identificador", token.getLexema()+" não esperado",token.getnLinha()));
                sincronizar("Identificador2", "Identificador", null);

                if (token != null){
                    if (conjunto_P_S.primeiro("Identificador2").contains(token.getLexema())){
                        Identificador2();
                    }
                }


            }
        }
        else if (token != null && token.getTipo() == 3){
            nextToken();
            Identificador3();
        }
        else if (token != null){
            //seguinte de escopo ou primeiro de de Identificador3 e $
            addErroSintatico(new ErroSintatico("Identificador", token.idToToken(token.getTipo())+" não esperado",token.getnLinha()));
            sincronizar("Identificador3", "Identificador", null);

            if (token != null){
                if (conjunto_P_S.primeiro("Identificador3").contains(token.getLexema())){
                    Identificador3();
                }
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Identificador","EOF inesperado", linhaErroEOF));
        }

    }





    public void ContListaParametros(){

        if (token != null && token.getLexema().equals(",")){
            nextToken();
            ListaParametros();
        }
//        else if (token != null && !conjunto_P_S.seguinte("ListaParametros").contains(token.getLexema()) && !pertence(1,"ListaParametros")){
//            addErroSintatico(new ErroSintatico("ContListaParametros", token.idToToken(token.getTipo())+" não esperado",token.getnLinha()));
//            sincronizar("ContListaParametros", "ListaParametros", null);
//
//            if (token != null){
//                if (conjunto_P_S.primeiro("ContListaParametros").contains(token.getLexema())){
//                    ContListaParametros();
//                }
//            }
//        }
        if (token == null){
            addErroSintatico(new ErroSintatico("ContListaParametros","EOF inesperado", linhaErroEOF));
        }


    }

    public void ListaParametros2(){

        if (token != null && pertence(0, "ListaParametros2")){
            nextToken();
        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("ListaParametros2", token.idToToken(token.getTipo())+" não esperado",token.getnLinha()));
            sincronizar("ContListaParametros", null, null);

            if (token != null){
                if (conjunto_P_S.primeiro("ContListaParametros").contains(token.getLexema())){
                    ContListaParametros();
                }

            }

        }

        if (token == null){
            addErroSintatico(new ErroSintatico("ListaParametros2","EOF inesperado", linhaErroEOF));
        }

    }


    public void ListaParametros(){
        if (token!= null && conjunto_P_S.primeiro("ListaParametros2").contains(token.getLexema())){
            ListaParametros2();
            ContListaParametros();
        }
        else if (token!= null){
            addErroSintatico(new ErroSintatico("ListaParametros", token.getLexema()+" não esperado",token.getnLinha()));
            sincronizar("ContListaParametros#Start", null, null);

            if (token != null){
                if (conjunto_P_S.primeiro("ContListaParametros").contains(token.getLexema())){
                    ContListaParametros();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start(0);
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("ListaParametros","EOF inesperado", linhaErroEOF));
        }

    }

    public void Matriz(){

        if (token != null && token.getLexema().equals("[")){
            nextToken();
            ValorVetor();
            if (token != null && token.getLexema().equals("]")){
                nextToken();
                Var4();
            }
            else if (token != null){
                addErroSintatico(new ErroSintatico("Matriz", "Esperava ] mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar("ValorVetor#Var4#GeraFuncaoeProcedure#Start", "Matriz", null);

                if (token != null){
                    if (conjunto_P_S.primeiro("ValorVetor").contains(token.getLexema())){
                        ValorVetor();
                    }
                    else if (conjunto_P_S.seguinte("Var4").contains(token.getLexema())){
                        Var4();
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
        else if (token != null && conjunto_P_S.primeiro("Var4").contains(token.getLexema())) {
            Var4();
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("Matriz", token.getLexema()+ " não esperado", token.getnLinha()));

            sincronizar("Var2#GeraFuncaoeProcedure#Start", "Matriz", null);

            if (token != null){
                if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                    Var2();
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
            addErroSintatico(new ErroSintatico("Matriz","EOF inesperado", linhaErroEOF));
        }


    }


    //<Vetor3> ::= '[' <ValorVetor> ']' <Matriz>
    public void Vetor3(){

        if (token != null && token.getLexema().equals("[")){
            nextToken();
            ValorVetor();
            if (token != null && token.getLexema().equals("]")){
                nextToken();
                Matriz();
            }
            else if (token != null){
                addErroSintatico(new ErroSintatico("Vetor3", "Esperava ] mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar("Matriz#GeraFuncaoeProcedure#Start", "Vetor3", null);

                if (token != null){
                    if (conjunto_P_S.primeiro("Matriz").contains(token.getLexema())){
                        Matriz();
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
        else if (token != null){
            addErroSintatico(new ErroSintatico("Vetor3", "Esperava [ mas encontrou "+token.getLexema(),token.getnLinha()));
            sincronizar("ValorVetor#Matriz#GeraFuncaoeProcedure#Start", "Vetor3", null);

            if (token != null){
                if (conjunto_P_S.primeiro("ValorVetor").contains(token.getLexema())){
                    ValorVetor();
                }
                if (conjunto_P_S.primeiro("Matriz").contains(token.getLexema())){
                    Matriz();
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
            addErroSintatico(new ErroSintatico("Vetor3", "EOF inesperado", linhaErroEOF));
        }
    }



   //<Var4> ::= ',' <IdVar> | ';' <Var3>
    public void Var4(){
        System.out.println("Souuuuuuuuuuuuuuuuuuuuuuuuuuuuuu "+token.getLexema());
        if (token != null && token.getLexema().equals(",")){
            nextToken();
            IdVar();
            System.out.println("Retorno 1"+ token.getLexema());
        }
        else if (token != null && token.getLexema().equals(";")){
            nextToken();
            Var3();
            System.out.println("Retorno 5"+ token.getLexema());
        }
        else if (token != null){
           addErroSintatico(new ErroSintatico("Var4", "Esperava , ou ; mas encontrou um " + token.getLexema()+ " não esperado", token.getnLinha()));
           sincronizar("IdVar#Var3#Var2#GeraFuncaoeProcedure#Start","Var4", null);
           if (token != null){
               if (conjunto_P_S.primeiro("IdVar").contains(token.getLexema())){
                   IdVar();
               }
               else if (conjunto_P_S.primeiro("Var3").contains(token.getLexema())){
                   Var3();
               }
                else if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                    Var2();
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
            addErroSintatico(new ErroSintatico("Var4", "EOF inesperado", linhaErroEOF));
        }


    }
    //<Var3> ::= '}' | <TipoVar>
    public void Var3(){

        if (token != null && token.getLexema().equals("}")){
            nextToken();
            System.out.println("---------------------------------------------------------------"+token.getLexema());
        }
        else if (token != null && pertence(0, "TipoVar")){
            TipoVar();
        }
        else if (token != null){
           addErroSintatico(new ErroSintatico("Var3", token.getLexema() + " não esperado", token.getnLinha()));
           sincronizar("Var2#GeraFuncaoeProcedure#Start","Var3", null);
            if (token != null){
                if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                    Var2();
                }
                else if (pertence(0, "GeraFuncaoeProcedure")){
                    GeraFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start(0);
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Var3", "EOF inesperado", linhaErroEOF));
        }


    }

    //<Var2> ::= ',' <IdVar> | ';' <Var3> | '=' <Valor> <Var4> | <Vetor3> <Var4>
    public void Var2(){

        if (token != null && conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
            if (token != null && token.getLexema().equals(",")){
                nextToken();
                IdVar();
            }
            else if (token != null &&  token.getLexema().equals(";")){
                nextToken();
                Var3();
            }
            else if (token != null && token.getLexema().equals("=")){
                nextToken();
                Valor();
                System.out.println("Retorno 3"+ token.getLexema());
                Var4();
            }
            else if (token != null && token.getLexema().equals("[")) {
                Vetor3();
                //Var4();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Var2", token.getLexema() +" não esperado", token.getnLinha() ));
                sincronizar("Var2#GeraFuncaoeProcedure#Start","Var2", null);
                if (token != null){
                    if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                        Var2();
                    }
                    else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                        GeraFuncaoeProcedure();
                    }
                    else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                        Start(0);
                    }
                }
            }
        } else if (token != null) {
            addErroSintatico(new ErroSintatico("Var2", token.getLexema() +" não esperado", token.getnLinha()));
            sincronizar("Var2#GeraFuncaoeProcedure#Start","Var2", null);
            if (token != null){
                if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                    Var2();
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
            addErroSintatico(new ErroSintatico("Var2", "EOF inesperado", linhaErroEOF));
        }



    }
    //<IdVar> ::= Id <Var2>
    public void IdVar(){

        if (token != null && token.getTipo() == 3){
            nextToken();
            Var2();
        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("IdVar", "Esperava um identificador mas encontrou "+token.getLexema(),  token.getnLinha()));
            sincronizar("Var2#GeraFuncaoeProcedure#Start","IdVar", null);
            if (token != null){
                if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                    Var2();
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
            Tipo();
            IdVar();
    }

    //<Var> ::= 'var' '{' <TipoVar> | <>
    public void Var(){

        if (token != null && token.getLexema().equals("var")){
            nextToken();
            if (token != null && token.getLexema().equals("{")){
                nextToken();
                TipoVar();

            }
            else if (token != null){
                addErroSintatico(new ErroSintatico("Var", "Esperava { mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar("TipoVar#GeraFuncaoeProcedure#Start",null, null);

                if (token != null){
                    if (conjunto_P_S.primeiro("TipoVar").contains(token.getLexema())){
                        TipoVar();
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
        if (token == null){
            addErroSintatico(new ErroSintatico("Var", "EOF inesperado", linhaErroEOF));
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
            }
        }
		else if (token != null) {
            addErroSintatico(new ErroSintatico("Const", "Esperava { mas encontrou "+token.getLexema(),token.getnLinha()));
            sincronizar("TipoConst#IdConst#Const2#Const3", "Const", null);
            if (token != null) {
                if (pertence(0, "TipoConst"))
                    TipoConst();
                else if (pertence(0, "IdConst"))
                    IdConst();
                else if (pertence(0, "Const2"))
                    Const2();
                else if (pertence(0, "Const3"))
                    Const3();
                else return;
            }
            else addErroSintatico(new ErroSintatico("Const", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("Const", "EOF inesperado", linhaErroEOF));
    }

    // <TipoConst> ::= <Tipo> <IdConst>
    private void TipoConst() {
        Tipo();
        IdConst();
    }

    // <IdConst> ::= Id <Valor> <Const2>
    private void IdConst() {
        if (token != null && token.getTipo() == 3) {
            nextToken();
            Valor();
            Const2();
        }
        else if (token != null) {
            sincronizar("IdConst#Valor#Const2", "IdConst", null);
            addErroSintatico(new ErroSintatico("IdConst", "Esperava Id mas encontrou "+token.getLexema(),token.getnLinha()));

            if (token != null) {
                if (pertence(0, "IdConst"))
                    IdConst();
                else if (pertence(0, "Valor"))
                    Valor();
                else if (pertence(0, "Const2"))
                    Const2();
                else return;
            }
            else addErroSintatico(new ErroSintatico("IdConst", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("IdConst", "EOF inesperado", linhaErroEOF));
    }

    // <Const2> ::= ' , ' <IdConst> | ' ; ' <Const3>
    private void Const2() {
        if (token != null && token.getLexema().equals(",")) {
            nextToken();
            IdConst();
        }
        else if (token != null && token.getLexema().equals(";")) {
            nextToken();
            Const3();
        }
        else if (token != null) {
            sincronizar("Const2#IdConst#Const3", "Const2", null);
            addErroSintatico(new ErroSintatico("Const2", "Esperava , ou ; mas encontrou "+token.getLexema(),token.getnLinha()));

            if (token != null) {
                if (pertence(0, "Const2"))
                    Const2();
                else if (pertence(0, "IdConst"))
                    IdConst();
                else if (pertence(0, "Const3"))
                    Const3();
                else return;
            }
            else addErroSintatico(new ErroSintatico("Const2", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("Const2", "EOF inesperado", linhaErroEOF));
    }

    // <Const3> ::= ' } ' | <TipoConst>
    private void Const3() {
        if (token != null && token.getLexema().equals("}"))
            nextToken();
	    else if (token != null && pertence(0, "TipoConst"))
            TipoConst();
        else if (token != null) {
            sincronizar("Const3", "Const3", null);
            addErroSintatico(new ErroSintatico("Const3", "Esperava } ou nova declaração de const mas encontrou " + token.getLexema(), token.getnLinha()));

            if (token != null) {
                if (pertence(0, "Const3"))
                    Const3();
                else return;
            } else addErroSintatico(new ErroSintatico("Const3", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("Const3", "EOF inesperado", linhaErroEOF));
    }


    // <ExpressaoLogicaRelacional> ::= <ExpressaoLR> | '(' <ExpressaoLR> ')' <ExpressaoLR3>
    private void ExpressaoLogicaRelacional() {

        if (token != null && pertence(0, "ExpressaoLR"))
            ExpressaoLR();
        else if (token != null && token.getLexema().equals("(")) {
            nextToken();
            ExpressaoLR();

            if (token != null && token.getLexema().equals(")")) {
                nextToken();
                ExpressaoLR3();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("ExpressaoLogicaRelacional", "Esperava ) mas encontrou " + token.getLexema(), token.getnLinha()));
                sincronizar("ExpressaoLR3", "ExpressaoLogicaRelacional", ")");

                if (token != null) {
                    if (pertence(0, "ExpressaoLR3"))
                        ExpressaoLR3();
                    else if (token.getLexema().equals(")")) {
                        nextToken();
                        ExpressaoLR3();
                    }
                    else return;
                }
                else addErroSintatico(new ErroSintatico("ExpressaoLogicaRelacional", "EOF inesperado", linhaErroEOF));
            }
            else addErroSintatico(new ErroSintatico("ExpressaoLogicaRelacional", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("ExpressaoLogicaRelacional", "EOF inesperado", linhaErroEOF));
    }

    // <ExpressaoLR> ::= <ArgumentoLR2> <ExpressaoLR2> | <ArgumentoLR3> <OperadorRelacional> <ArgumentoLR> <ExpressaoLR3>
    private void ExpressaoLR() {

        if (token != null && pertence(0, "ArgumentoLR2")) {
            ArgumentoLR2();
            ExpressaoLR2();
        }
        else if (token != null && pertence(0, "ArgumentoLR3")) {
            ArgumentoLR3();
            OperadorRelacional();
            ArgumentoLR();
            ExpressaoLR3();
        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("ExpressaoLR", "" + token.getLexema() +" inesperado", token.getnLinha()));
            sincronizar("ExpressaoLR#ExpressaoLR2#OperadorRelacional#ArgumentoLR#ExpressaoLR3", "ExpressaoLR", "");

            if (token != null) {
                if (pertence(0, "ExpressaoLR"))
                    ExpressaoLR();
                else if (pertence(0, "ExpressaoLR2"))
                    ExpressaoLR2();
                else if (pertence(0, "OperadorRelacional")) {
                    OperadorRelacional();
                    ArgumentoLR();
                    ExpressaoLR3();
                }
                else if (pertence(0, "ArgumentoLR")) {
                    ArgumentoLR();
                    ExpressaoLR3();
                }
                else if (pertence(0, "ExpressaoLR3"))
                    ExpressaoLR3();
                else return;
            }
            else addErroSintatico(new ErroSintatico("ExpressaoLogicaRelacional", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("ExpressaoLR", "EOF inesperado", linhaErroEOF));
    }

    // <ExpressaoLR2> ::= <OperadorRelacional> <ArgumentoLR> <ExpressaoLR3> | <ExpressaoLR3>
    private void ExpressaoLR2() {

        if (token != null && pertence(0, "OperadorRelacional")) {
            OperadorRelacional();
            ArgumentoLR();
            ExpressaoLR3();
        }
        else if (token != null && pertence(0, "ExpressaoLR3")) {
            ExpressaoLR3();
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("ExpressaoLR2", "" + token.getLexema() +" inesperado", token.getnLinha()));
            sincronizar("ExpressaoLR2#ArgumentoLR", "ExpressaoLR2", "");

            if (token != null) {
                if (pertence(0, "ExpressaoLR2"))
                    ExpressaoLR2();
                else if (pertence(0, "ArgumentoLR")) {
                    ArgumentoLR();
                    ExpressaoLR3();
                }
                else return;
            }
            else addErroSintatico(new ErroSintatico("ExpressaoLR2", "EOF inesperado", linhaErroEOF));
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
    private void ArgumentoLR() {
        if (token != null && pertence(0, "ArgumentoLR2")) {
            ArgumentoLR2();
        }
        else if (token != null && pertence(0, "ArgumentoLR3")) {
            ArgumentoLR3();
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("ArgumentoLR", "" + token.getLexema() +" inesperado", token.getnLinha()));
            sincronizar("ArgumentoLR", "ArgumentoLR", "");

            if (token != null) {
                if (pertence(0, "ArgumentoLR"))
                    ArgumentoLR();
                else return;
            }
            else addErroSintatico(new ErroSintatico("ExpressaoLR2", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("ArgumentoLR", "EOF inesperado", linhaErroEOF));
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
        else if (token != null) {
            addErroSintatico(new ErroSintatico("ArgumentoLR2", "Esperava !, true ou false mas encontrou " + token.getLexema(), token.getnLinha()));
            sincronizar("ArgumentoLR2#ArgumentoLR2_1", "ArgumentoLR2", "");

            if (token != null) {
                if (pertence(0, "ArgumentoLR2"))
                    ArgumentoLR2();
                else if (pertence(0, "ArgumentoLR2_1"))
                    ArgumentoLR2_1();
                else return;
            }
            else addErroSintatico(new ErroSintatico("ArgumentoLR2", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("ArgumentoLR2", "EOF inesperado", linhaErroEOF));
    }

    // <ArgumentoLR2_1> ::= Id | true | false
    private void ArgumentoLR2_1() {

        if (token != null) {
            if (token.getTipo() == 3 || token.getLexema().matches("^(true|false)$"))
                nextToken();
            else {
                addErroSintatico(new ErroSintatico("ArgumentoLR2_1", "Esperava identificador, true ou false mas encontrou " + token.getLexema(), token.getnLinha()));
                sincronizar("ArgumentoLR2_1", "ArgumentoLR2_1", "");

                if (token != null) {
                    if (pertence(0, "ArgumentoLR2_1"))
                        ArgumentoLR2_1();
                    else return;
                }
                else addErroSintatico(new ErroSintatico("ArgumentoLR2_1", "EOF inesperado", linhaErroEOF));
            }
        }
        else addErroSintatico(new ErroSintatico("ArgumentoLR2_1", "EOF inesperado", linhaErroEOF));
    }

    // <ArgumentoLR3> ::= String | <ExpressaoAritmetica>
    private void ArgumentoLR3() {

        if (token != null && token.getTipo() == 11)
            nextToken();
        else if (token != null && pertence(0, "ExpressaoAritmetica"))
            ExpressaoAritmetica();
        else if (token != null) {
            addErroSintatico(new ErroSintatico("ArgumentoLR3", "" + token.getLexema() +" inesperado", token.getnLinha()));
            sincronizar("ArgumentoLR3", "ArgumentoLR3", "");

            if (token != null) {
                if (pertence(0, "ArgumentoLR3"))
                    ArgumentoLR3();
                else return;
            }
            else addErroSintatico(new ErroSintatico("ArgumentoLR3", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("ArgumentoLR3", "EOF inesperado", linhaErroEOF));
    }

    // <OperadorRelacional> ::= '!=' | '==' | '<' | '>' | '<=' | '>='
    private void OperadorRelacional() {

        if (token != null && token.getTipo() == 6)
            nextToken();
        else if (token != null) {
            addErroSintatico(new ErroSintatico("OperadorRelacional", "Esperava um operador relacional mas encontrou " + token.getLexema(), token.getnLinha()));
            sincronizar("OperadorRelacional", "OperadorRelacional", "");

            if (token != null) {
                if (pertence(0, "OperadorRelacional"))
                    OperadorRelacional();
                else return;
            }
            else addErroSintatico(new ErroSintatico("OperadorRelacional", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("OperadorRelacional", "EOF inesperado", linhaErroEOF));
    }

    // <OperadorLogico> ::= '&&' | '||'
    private void OperadorLogico() {
        if (token != null && token.getTipo() == 4)
            nextToken();
        else if (token != null) {
            addErroSintatico(new ErroSintatico("OperadorLogico", "Esperava um operador relacional mas encontrou " + token.getLexema(), token.getnLinha()));
            sincronizar("OperadorLogico", "OperadorLogico", "");

            if (token != null) {
                if (pertence(0, "OperadorLogico"))
                    OperadorLogico();
                else return;
            }
            else addErroSintatico(new ErroSintatico("OperadorLogico", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("OperadorLogico", "EOF inesperado", linhaErroEOF));
    }

    // <Read> ::= 'read' '(' <Read1>
    private void Read() {
        if (token != null && token.getLexema().equals("read")) {
            nextToken();
            if (token != null && token.getLexema().equals("(")) {
                nextToken();
                Read1();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Read", "Esperava um ( mas encontrou " + token.getLexema(), token.getnLinha()));
                sincronizar("Read1", "Read", "(");

                if (token != null) {
                    if (pertence(0, "Read1"))
                        Read1();
                    else if (token.getLexema().equals("(")) {
                        nextToken();
                        Read1();
                    }
                    else return;
                }
                else addErroSintatico(new ErroSintatico("Read", "EOF inesperado", linhaErroEOF));
            }
            else addErroSintatico(new ErroSintatico("Read", "EOF inesperado", linhaErroEOF));
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
        else if (token != null) {
            addErroSintatico(new ErroSintatico("AuxRead", "" + token.getLexema() +" inesperado", token.getnLinha()));
            sincronizar("AuxRead#Read1", "AuxRead", null);

            if (token != null) {
                if (pertence(0, "AuxRead"))
                    AuxRead();
                else if (pertence(0, "Read1"))
                    Read1();
                else return;
            }
            else addErroSintatico(new ErroSintatico("AuxRead", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("AuxRead", "EOF inesperado", linhaErroEOF));
    }

    // <ReadFim> ::= ')' ';'
    private void ReadFim() {

        if (token != null && token.getLexema().equals(")")) {
            nextToken();
            if (token != null && token.getLexema().equals(";"))
                nextToken();
            else {
                addErroSintatico(new ErroSintatico("ReadFim", "Esperava um ; mas encontrou " + token.getLexema(), token.getnLinha()));
                sincronizar(null, "ReadFim", ";");

                if (token != null) {
                    if (token.getLexema().equals(";"))
                        nextToken();
                    else return;
                }
                else addErroSintatico(new ErroSintatico("ReadFim", "EOF inesperado", linhaErroEOF));
            }
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("ReadFim", "Esperava um ; mas encontrou " + token.getLexema(), token.getnLinha()));
            sincronizar("ReadFim", "ReadFim", ";");

            if (token != null) {
                if (token.getLexema().equals(";"))
                    nextToken();
                else if (pertence(0, "ReadFim"))
                    ReadFim();
                else return;
            }
            else addErroSintatico(new ErroSintatico("ReadFim", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("ReadFim", "EOF inesperado", linhaErroEOF));
    }

    // <Corpo>::= <Var> <Corpo2> '}' | '}'
    private void Corpo() {
        Var();
        Corpo2();
        if (token != null && token.getLexema().equals("}")) {
            nextToken();
        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("Corpo", "Esperava um } mas encontrou " + token.getLexema(), token.getnLinha()));
            sincronizar(null, "Corpo", "}");

            if (token != null) {
                if (token.getLexema().equals("}"))
                    nextToken();
                else return;
            }
            else addErroSintatico(new ErroSintatico("Corpo", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("Corpo", "EOF inesperado", linhaErroEOF));
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
        else if (token != null) {
            addErroSintatico(new ErroSintatico("IdentificadorComandos", "Esperava um ; mas encontrou " + token.getLexema(), token.getnLinha()));
            sincronizar("", "IdentificadorComandos", ";");

            if (token != null) {
                if (token.getLexema().equals(";"))
                    nextToken();
                else return;
            }
            else addErroSintatico(new ErroSintatico("IdentificadorComandos", "EOF inesperado", linhaErroEOF));
        }
        else addErroSintatico(new ErroSintatico("IdentificadorComandos", "EOF inesperado", linhaErroEOF));
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
                ListaParametros();
                if (token != null && token.getLexema().equals(")"))
                    nextToken();
                else {
                    addErroSintatico(new ErroSintatico("IdentificadorComandos2", "Esperava um ) mas encontrou " + token.getLexema(), token.getnLinha()));
                    sincronizar(null, "IdentificadorComandos2", ")");

                    if (token != null) {
                        if (token.getLexema().equals(")"))
                            nextToken();
                        else return;
                    }
                    else addErroSintatico(new ErroSintatico("IdentificadorComandos2", "EOF inesperado", linhaErroEOF));
                }
            }
            else {
                addErroSintatico(new ErroSintatico("IdentificadorComandos2", "" + token.getLexema() +" inesperado", token.getnLinha()));
                sincronizar("IdentificadorComandos2#IdentificadorComandos2_1#ListaParametros", "IdentificadorComandos2", null);

                if (token != null) {
                    if (pertence(0,"IdentificadorComandos2"))
                        IdentificadorComandos2();
                    else if (pertence(0, "IdentificadorComandos2_1"))
                        IdentificadorComandos2_1();
                    else if (pertence(0, "ListaParametros")) {
                        ListaParametros();

                        if (token != null && token.getLexema().equals(")"))
                            nextToken();
                        else {
                            addErroSintatico(new ErroSintatico("IdentificadorComandos2", "Esperava um ) mas encontrou " + token.getLexema(), token.getnLinha()));
                            sincronizar(null, "IdentificadorComandos2", ")");

                            if (token != null) {
                                if (token.getLexema().equals(")"))
                                    nextToken();
                                else return;
                            }
                            else addErroSintatico(new ErroSintatico("IdentificadorComandos2", "EOF inesperado", linhaErroEOF));
                        }
                    }
                    else return;
                }
                else addErroSintatico(new ErroSintatico("IdentificadorComandos2", "EOF inesperado", linhaErroEOF));
            }
        }
        else addErroSintatico(new ErroSintatico("IdentificadorComandos2", "EOF inesperado", linhaErroEOF));
    }

    // <IdentificadorComandos2_1> ::= <ExpressaoAritmetica> | String | Boolean
    private void IdentificadorComandos2_1() {
        if (token != null) {
            if (pertence(0, "ExpressaoAritmetica"))
                ExpressaoAritmetica();
            else if (token.getTipo() == 11 || token.getLexema().matches("^(true|false)$"))
                nextToken();
            else {
                addErroSintatico(new ErroSintatico("IdentificadorComandos2_1", "" + token.getLexema() +" inesperado", token.getnLinha()));
                sincronizar("IdentificadorComandos2_1", "IdentificadorComandos2_1", null);

                if (token != null) {
                    if (pertence(0,"IdentificadorComandos2_1"))
                        IdentificadorComandos2_1();
                    else return;
                }
                else addErroSintatico(new ErroSintatico("IdentificadorComandos2_1", "EOF inesperado", linhaErroEOF));
            }
        }
        else addErroSintatico(new ErroSintatico("IdentificadorComandos2_1", "EOF inesperado", linhaErroEOF));
    }

    // <ComandosReturn> ::= 'return' <CodigosRetornos>
    private void ComandosReturn() {
        if (token != null) {
            if (token.getLexema().equals("return")) {
                nextToken();
                CodigosRetornos();
            }
            else {
                addErroSintatico(new ErroSintatico("ComandosReturn", "Esperava return mas encontrou " + token.getLexema(), token.getnLinha()));
                sincronizar("ComandosReturn#CodigosRetornos", "ComandosReturn", null);

                if (token != null) {
                    if (pertence(0,"ComandosReturn"))
                        ComandosReturn();
                    else if (pertence(0, "CodigosRetornos"))
                        CodigosRetornos();
                    else return;
                }
                else addErroSintatico(new ErroSintatico("ComandosReturn", "EOF inesperado", linhaErroEOF));
            }
        }
        else addErroSintatico(new ErroSintatico("ComandosReturn", "EOF inesperado", linhaErroEOF));
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
                else {
                    addErroSintatico(new ErroSintatico("CodigosRetornos", "Esperava ; mas encontrou " + token.getLexema(), token.getnLinha()));
                    sincronizar(null, "CodigosRetornos", ";");

                    if (token != null) {
                        if (token.getLexema().equals(";"))
                            nextToken();
                        else return;
                    }
                    else addErroSintatico(new ErroSintatico("CodigosRetornos", "EOF inesperado", linhaErroEOF));
                }
            }
            else {
                addErroSintatico(new ErroSintatico("CodigosRetornos", "" + token.getLexema() +" inesperado", token.getnLinha()));
                sincronizar("CodigosRetornos", "CodigosRetornos", null);

                if (token != null) {
                    if (pertence(0, "CodigosRetornos"))
                        CodigosRetornos();
                    else return;
                }
                else addErroSintatico(new ErroSintatico("CodigosRetornos", "EOF inesperado", linhaErroEOF));
            }
        }
        else addErroSintatico(new ErroSintatico("CodigosRetornos", "EOF inesperado", linhaErroEOF));
    }

    // <Start> ::= 'start' '(' ')' '{' <Corpo> '}'
    private void Start(int estagio) {

        int estagio_atual = estagio;

        if (token!= null) {
            switch (estagio) {
                case 0:
                    if (token != null && token.getLexema().equals("start")) {
                        nextToken();
                        estagio_atual ++;
                    }
                case 1:
                    if (token != null && token.getLexema().equals("(") && estagio_atual == 1) {
                        nextToken();
                        estagio_atual ++;
                    }
                case 2:
                    if (token != null && token.getLexema().equals(")") && estagio_atual == 2) {
                        nextToken();
                        estagio_atual ++;
                    }
                case 3:
                    if (token != null && token.getLexema().equals("{") && estagio_atual == 3) {
                        nextToken();
                        estagio_atual ++;
                    }
                case 4:
                    if (token != null && estagio_atual == 4) {
                        Corpo();
                        estagio_atual ++;
                    }
                case 5:
                    if (token != null && token.getLexema().equals("}") && estagio_atual == 5) {
                        nextToken();
                        estagio_atual ++;
                    }
            }

            if (token != null) addErroSintatico(new ErroSintatico("Start", "EOF inesperado", linhaErroEOF));
            else if (estagio_atual < 6) {
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
                    case 5:
                        lexemas = (lexemas != null)?lexemas + "#}":"}";
                }

                addErroSintatico(new ErroSintatico("Start", "Esperava " + lexemas.split("#")[0] + " mas encontrou " + token.getLexema(), token.getnLinha()));
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
                            case "}":
                                Start(5);
                                break;
                        }
                    }
                }
                else addErroSintatico(new ErroSintatico("Start", "EOF inesperado", linhaErroEOF));
            }
        }
        else addErroSintatico(new ErroSintatico("Start", "EOF inesperado", linhaErroEOF));
    }

    /**
     * Método responsável por  os erros no arquivo de saída
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
        if(!this.getListaErrosSintaticos().isEmpty()){
            //Varre a lista de tokens
            while(it.hasNext()){
                //Escreve no arquivo de saída correpondente
                arquivoEscrita.write(it.next().toString());
            }
        }
        else{
            arquivoEscrita.write("Análise Sintática realizada com Sucesso!");
        }

        //Obriga que os dados que estão no buffer sejam escritos imediatamente
        arquivoEscrita.flush();
        arquivoEscrita.close();
    }

}
