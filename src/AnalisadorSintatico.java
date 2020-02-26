import java.io.*;
import java.util.*;

public class AnalisadorSintatico {

    private List<Token> tokens; // Lista de tokens
    private List<ErroSintatico> errosSintaticos; // Lista de erros sintáticos
    private Token token; // Token atual
    private static int FLAGERRO = 0;
    private Conjunto conjunto_P_S;
    private static  int linhaErroEOF;


    public AnalisadorSintatico(List<Token> tokens) {

        this.tokens = tokens;
        this.errosSintaticos = new ArrayList<>();
        this.conjunto_P_S = new Conjunto();
    }

    private void nextToken() {
        if (tokens.size() > 0) {
            token = tokens.get(0);
            tokens.remove(0);
            linhaErroEOF = token.getnLinha();
        }
        else token = null;
    }

    private void addErroSintatico(ErroSintatico erro){
        if (erro.getMensagem().split(" ")[0].equals("EOF")){
             if (FLAGERRO !=1 ){
                FLAGERRO = 1;
                this.errosSintaticos.add(erro);
            }
        }
        else
            this.errosSintaticos.add(erro);


    }

    // Todos os conjuntos e lexemas devem ser separados por #
    // EXEMPLO: "start#if#else", "Inicio#Var#Identificador"
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



    // tipo_conjunto = 0 (conjunto primeiro) ou 1 (conjunto seguinte) (na vdd qualquer coisa diferente de 0 é considerado como conjunto seguinte)
    private boolean pertence(int tipo_conjunto, String nterminal) {

        LinkedHashSet<String> conjunto = (tipo_conjunto == 0)?conjunto_P_S.primeiro(nterminal):conjunto_P_S.seguinte(nterminal);

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

    public void T2(){
        if (token != null && token.getLexema().equals("*")){
            ExpressaoAritmetica();
        }
        else if (token != null && token.getLexema().equals("/")) {
            ExpressaoAritmetica();

        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("T2", token.getLexema()+" não esperado", token.getnLinha()));
            sincronizar("ExpressaoAritmetica", "T2", null);
            if (token != null) {
                if (conjunto_P_S.primeiro("ExpressaoAritmetica").contains(token.getLexema())) {
                    ExpressaoAritmetica();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("T2","EOF inesperado", linhaErroEOF));
        }

    }

    public void E2(){
        if (token != null && token.getLexema().equals("+")){
            ExpressaoAritmetica();
        }
        else if (token != null && token.getLexema().equals("-")) {
            ExpressaoAritmetica();

        }
        else if (token != null) {
            addErroSintatico(new ErroSintatico("E2", token.getLexema()+" não esperado", token.getnLinha()));
            sincronizar("ExpressaoAritmetica", "E2", null);
            if (token != null) {
                if (conjunto_P_S.primeiro("ExpressaoAritmetica").contains(token.getLexema())) {
                    ExpressaoAritmetica();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("T2","EOF inesperado", linhaErroEOF));
        }


    }

    public void IdentificadorAritmetico(){

    }

    public void IdentificadorAritmetico3(){

    }

    public void ExpressaoAritmetica (){

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
        else if (token != null && !conjunto_P_S.seguinte("Vetor2").contains(token.getLexema()) && !pertence(1,"Vetor2")){
            addErroSintatico(new ErroSintatico("Vetor", token.getLexema()+" não esperado" + token.getLexema(), token.getnLinha()));
            sincronizar("Vetor", "Vetor2", null);
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Vetor2","EOF inesperado", linhaErroEOF));
        }

    }


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
        else if (token != null && !conjunto_P_S.seguinte("Vetor").contains(token.getLexema()) && !pertence(1,"Vetor")){
            addErroSintatico(new ErroSintatico("Vetor", token.getLexema()+" não esperado" + token.getLexema(), token.getnLinha()));
            sincronizar("Vetor", "Vetor", null);
            if (token != null) {
                if (conjunto_P_S.primeiro("Vetor").contains(token.getLexema())) {
                    Vetor();
                }

            }
        }

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
        }else if (token != null && !conjunto_P_S.seguinte("Identificador4").contains(token.getLexema()) && !pertence(1,"Identificador4")){
            addErroSintatico(new ErroSintatico("Identificador4", token.getLexema()+"  não esperado", token.getnLinha()));
            sincronizar("Vetor", "Identificador2", null);
            if (token != null) {
                if (conjunto_P_S.primeiro("Vetor").contains(token.getLexema())) {
                    Vetor();
                }
            }

        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Identificador4","EOF inesperado", linhaErroEOF));
        }
    }
    public void Escopo(){
        if (token != null && (token.getLexema().equals("local") || token.getLexema().equals("global"))){
            nextToken();
            if (token != null && token.equals(".")){
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
        else if (token != null && !conjunto_P_S.seguinte("Identificador2").contains(token.getLexema()) && !pertence(1,"Identificador2")){
            addErroSintatico(new ErroSintatico("Identificador3", "Esperava ) mas encontrou "+token.getLexema(),token.getnLinha()));
            sincronizar("Vetor", "Identificador2", null);
            if (token != null){
                if (conjunto_P_S.primeiro("Vetor").contains(token.getLexema())){
                    Vetor();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Identificador2","EOF inesperado", linhaErroEOF));
        }
    }

    public void Identificador3(){

        if (token != null && conjunto_P_S.primeiro("Identificador2").contains(token.getLexema())){
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
        else if (token != null && !conjunto_P_S.seguinte("ListaParametros").contains(token.getLexema()) && !pertence(1,"ListaParametros")){
            addErroSintatico(new ErroSintatico("ContListaParametros", token.idToToken(token.getTipo())+" não esperado",token.getnLinha()));
            sincronizar("ContListaParametros", "ListaParametros", null);

            if (token != null){
                if (conjunto_P_S.primeiro("ContListaParametros").contains(token.getLexema())){
                    ContListaParametros();
                }
            }
        }
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
                    Start();
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
            //ValorVetor();
            if (token != null && token.getLexema().equals("]")){
                nextToken();
                Var4();
            }
            else if (token != null){
                addErroSintatico(new ErroSintatico("Matriz", "Esperava ] mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar("ValorVetor#Var4#GeraFuncaoeProcedure#Start", "Matriz", null);

                if (token != null){
                    if (conjunto_P_S.primeiro("ValorVetor").contains(token.getLexema())){
                        //ValorVetor();
                    }
                    else if (conjunto_P_S.seguinte("Var4").contains(token.getLexema())){
                        Var4();
                    }
                    else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                        GerarFuncaoeProcedure();
                    }
                    else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                        Start();
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
                    GerarFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Matriz","EOF inesperado", linhaErroEOF));
        }


    }



    public void Vetor3(){

        if (token != null && token.getLexema().equals("[")){
            nextToken();
            //ValorVetor();
            if (token != null && token.equals("]")){
                nextToken();
                Matriz();
            }
            else if (token != null){
                addErroSintatico(new ErroSintatico("Var", "Esperava ] mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar("Matriz#GeraFuncaoeProcedure#Start", "Vetor3", null);

                if (token != null){
                    if (conjunto_P_S.primeiro("Matriz").contains(token.getLexema())){
                        Matriz();
                    }
                    else if (conjunto_P_S.seguinte("Vetor3").contains(token.getLexema())){
                        Var4();
                    }
                    else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                        GerarFuncaoeProcedure();
                    }
                    else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                        Start();
                    }
                }
            }

        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("Vetor3", "Esperava [ mas encontrou "+token.getLexema(),token.getnLinha()));
            sincronizar("ValorVetor#Matriz#GeraFuncaoeProcedure#Start", "Vetor3", null);

            if (token != null){
                if (conjunto_P_S.primeiro("ValorVetor").contains(token.getLexema())){
                    //ValorVetor();
                }
                if (conjunto_P_S.primeiro("Matriz").contains(token.getLexema())){
                    Matriz();
                }
                else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                    GerarFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start();
                }
            }

        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Vetor3", "EOF inesperado", linhaErroEOF));
        }
    }




    public void Var4(){

        if (token != null && token.getLexema().equals(",")){
            nextToken();
            IdVar();
        }
        else if (token != null && token.getLexema().equals(";")){
            nextToken();
            Var3();
        }
        else if (token != null){
           addErroSintatico(new ErroSintatico("Var4", token.getLexema()+ " não esperado", token.getnLinha()));
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
                    GerarFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start();
                }
           }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Var3", "EOF inesperado", linhaErroEOF));
        }


    }

    public void Var3(){

        if (token != null && token.getLexema().equals("}")){
            nextToken();
        }
        else if (token != null && conjunto_P_S.primeiro("TipoVar").contains(token.getLexema())){
            TipoVar();
        }
        else if (token != null){
           addErroSintatico(new ErroSintatico("Var3", token.getLexema() + " não esperado", token.getnLinha()));
           sincronizar("Var2#GeraFuncaoeProcedure#Start","Var3", null);
            if (token != null){
                if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                    Var2();
                }
                else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                    GerarFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("Var3", "EOF inesperado", linhaErroEOF));
        }


    }


    public void Var2(){

        if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
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
                //Valor();
                Var4();
            }
            else if (token != null && token.getLexema().equals("[")) {
                Vetor3();
                Var4();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Var2", token.getLexema() +" não esperado", token.getnLinha() ));
                sincronizar("Var2#GeraFuncaoeProcedure#Start","Var2", null);
                if (token != null){
                    if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                        Var2();
                    }
                    else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                        GerarFuncaoeProcedure();
                    }
                    else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                        Start();
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
                    GerarFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("TipoVar", "EOF inesperado", linhaErroEOF));
        }



    }
    public void IdVar(){

        if (token != null && token.getTipo() == 3){
            nextToken();
            Var2();
        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("IdVar", "Esperava um identificador mas encontrou "+token.getLexema(),  token.getnLinha()));
            sincronizar("Var2#GeraFuncaoeProcedure#Start",null, null);
            if (token != null){
                if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
                    Var2();
                }
                else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                    GerarFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start();
                }
            }
        }

        if (token == null){
            addErroSintatico(new ErroSintatico("TipoVar", "EOF inesperado", linhaErroEOF));
        }


    }


    public void TipoVar(){

        if (token != null && conjunto_P_S.primeiro("Tipo").contains(token.getLexema())){
            nextToken();
            //Tipo();
            IdVar();

        }
        else if (token != null){
            addErroSintatico(new ErroSintatico("TipoVar", token.getLexema()+" não esperado", token.getnLinha()));
            sincronizar("IdVar#GeraFuncaoeProcedure#Start",null, null);
            if (token != null){
                if (conjunto_P_S.primeiro("IdVar").contains(token.getLexema())){
                    IdVar();
                }
                else if (conjunto_P_S.primeiro("GeraFuncaoeProcedure").contains(token.getLexema())){
                    GerarFuncaoeProcedure();
                }
                else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                    Start();
                }
            }

        }

        if (token == null){
            addErroSintatico(new ErroSintatico("TipoVar", "EOF inesperado", linhaErroEOF));
        }


    }


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
                        GerarFuncaoeProcedure();
                    }
                    else if (conjunto_P_S.primeiro("Start").contains(token.getLexema())){
                        Start();
                    }
                }
            }
        }
        if (token == null){
            addErroSintatico(new ErroSintatico("Var", "EOF inesperado", linhaErroEOF));
        }

    }
    public void Struct(){

    }
    public void GerarFuncaoeProcedure(){

    }

    public void Start(){

    }
























//    public static  void valorVetor(){
//
//    }




//

//

//

//

//
//
//    }
//


//

//

//
//
//
//

//

//

    public void executarAnalise() {
        nextToken();
        if (this.token != null){
            Const();
            Struct();
            Var();
            GerarFuncaoeProcedure();
            Start();
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
        if (token != null && pertence(0, "ArgumentoLR3")) {
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

        if (token != null) {
            if (pertence(0, "Var")) {
                Var();
                Corpo2();
                if (token != null && token.getLexema().equals(("}")))
                    nextToken();
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
            else if (token.getLexema().equals("}"))
                nextToken();
            else {
                addErroSintatico(new ErroSintatico("Corpo", "" + token.getLexema() +" inesperado", token.getnLinha()));
                sincronizar("Corpo#Corpo2", "Corpo", null);

                if (token != null) {
                    if (pertence(0,"Corpo"))
                        Corpo();
                    else if (pertence(0, "Corpo2"))
                        Corpo2();
                    else return;
                }
                else addErroSintatico(new ErroSintatico("Corpo", "EOF inesperado", linhaErroEOF));
            }
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
                    if (token.getLexema().equals("start")) {
                        nextToken();
                        estagio_atual ++;
                    }
                case 1:
                    if (token.getLexema().equals("(") && estagio_atual == 1) {
                        nextToken();
                        estagio_atual ++;
                    }
                case 2:
                    if (token.getLexema().equals(")") && estagio_atual == 2) {
                        nextToken();
                        estagio_atual ++;
                    }
                case 3:
                    if (token.getLexema().equals("{") && estagio_atual == 3) {
                        nextToken();
                        estagio_atual ++;
                    }
                case 4:
                    if (estagio_atual == 4) {
                        Corpo();
                        estagio_atual ++;
                    }
                case 5:
                    if (token.getLexema().equals("}") && estagio_atual == 5) {
                        nextToken();
                        estagio_atual ++;
                    }
            }

            if (estagio_atual < 6) {
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
