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


    public void Const(){

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
                //VetorDeclaracao();
            }
            else if (token != null) {
                addErroSintatico(new ErroSintatico("Identificador2", "Esperava um Identificador mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar("VetorDeclaracao", "Identificador2", null);
                if (token != null){
                    if (conjunto_P_S.primeiro("VetorDeclaracao").contains(token.getLexema())){
                        //VetorDeclaracao();
                    }
                }

            }
        }else if (token != null && conjunto_P_S.primeiro("VetorDeclaracao").contains(token.getLexema())){
            //VetorDeclaracao();
        }
        else if (token != null && !conjunto_P_S.seguinte("Identificador2").contains(token.getLexema()) && !pertence(1,"Identificador2")){
            addErroSintatico(new ErroSintatico("Identificador3", "Esperava ) mas encontrou "+token.getLexema(),token.getnLinha()));
            sincronizar("VetorDeclaracao", "Identificador2", null);
            if (token != null){
                if (conjunto_P_S.primeiro("VetorDeclaracao").contains(token.getLexema())){
                    //VetorDeclaracao();
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

        if (token.getLexema().equals("[")){
            nextToken();
            //ValorVetor();
            if (token.getLexema().equals("]")){
                nextToken();
                Var4();
            }
            else{
                addErroSintatico(new ErroSintatico("Matriz", "Esperava ] mas encontrou "+token.getLexema(),token.getnLinha()));
                sincronizar("ValorVetor#Var4#GeraFuncaoeProcedure#Start", null, null);

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
        else if (conjunto_P_S.primeiro("Var4").contains(token.getLexema())) {
            Var4();
        }
        else {
            addErroSintatico(new ErroSintatico("Matriz", token.getLexema()+ " não esperado", token.getnLinha()));

            sincronizar("Var2#GeraFuncaoeProcedure#Start", "Vetor3", null);

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

        if (token.getLexema().equals("[")){
            nextToken();
            //ValorVetor();
            if (token.equals("]")){
                nextToken();
                Matriz();
            }
            else{
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
        else{
            addErroSintatico(new ErroSintatico("Vetor3", "Esperava [ mas encontrou "+token.getLexema(),token.getnLinha()));
            sincronizar("ValorVetor#Matriz#GeraFuncaoeProcedure#Start", "Vetor3", null);

            if (token != null){
                if (conjunto_P_S.primeiro("ValorVetor").contains(token.getLexema())){
                    //ValorVetor();
                }
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

        if (token == null){
            addErroSintatico(new ErroSintatico("Vetor3", "EOF inesperado", linhaErroEOF));
        }
    }




    public void Var4(){

        if (token.getLexema().equals(",")){
            nextToken();
            IdVar();
        }
        else if (token.getLexema().equals(";")){
            nextToken();
            Var3();
        }
        else{
           addErroSintatico(new ErroSintatico("Var4", token.getLexema()+ " não esperado", token.getnLinha()));
           sincronizar("IdVar#Var3#Var2#GeraFuncaoeProcedure#Start",null, null);
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

        if (token.getLexema().equals("}")){
            nextToken();
        }
        else if (conjunto_P_S.primeiro("TipoVar").contains(token.getLexema())){
            TipoVar();
        }
        else{
           addErroSintatico(new ErroSintatico("Var3", token.getLexema() + " não esperado", token.getnLinha()));
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
            addErroSintatico(new ErroSintatico("Var3", "EOF inesperado", linhaErroEOF));
        }


    }


    public void Var2(){

        if (conjunto_P_S.primeiro("Var2").contains(token.getLexema())){
            if (token.getLexema().equals(",")){
                nextToken();
                IdVar();
            }
            else if (token.getLexema().equals(";")){
                nextToken();
                Var3();
            }
            else if (token.getLexema().equals("=")){
                nextToken();
                //Valor();
                Var4();
            }
            else if (token.getLexema().equals("[")) {
                Vetor3();
                Var4();
            }
            else {
                addErroSintatico(new ErroSintatico("Var2", token.getLexema() +" não esperado", token.getnLinha() ));
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
        } else {
            addErroSintatico(new ErroSintatico("Var2", token.getLexema() +" não esperado", token.getnLinha()));
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

        if (token.getTipo() == 3){
            nextToken();
            Var2();
        }
        else{
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

        if (this.conjunto_P_S.primeiro("Tipo").contains(token.getLexema())){
            nextToken();
            //Tipo();
            IdVar();

        }
        else {
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

        if (token.getLexema().equals("var")){
            nextToken();
            if (token.getLexema().equals("{")){
                nextToken();
                TipoVar();
            }
            else{
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
//    public static void indiceVetor(){
//
////        if (token é um intPos){
////            token = proximo_token();
////
////        }
////        else if (token pertence ao conjunto primeiro de identificador){
////            identificador();
////        }
////        else{
////            Erro
////        }
//
//    }
//
//    public static  void vetor2(){
//
////        if (token.equals("[")){
////            token = proximo_token();
////            indiceVetor();
////            if (token.equals("]")){
////                token = proximo_token();
////            }else {
////                Erro;
////            }
////
////        }
////        else{
////            Erro
////        }
//
//    }
//
//    public static  void identificador4(){
//
////        if (token.equals(".")){
////            token = proximo_token();
////            if (token é um identificador){
////                token = proximo_token();
////                vetor();
////
////            }
////            else{
////                Erro
////            }
////        }
////        else{
////            Erro
////        }
//
//
//    }
//
//    public static void vetor(){
//
////        if(token.equals("[")){
////            token = proximo_token();
////            indiceVetor();
////            if (token.equals("]")){
////                token = proximo_token();
////                vetor2();
//        identificador4();
////            }
////            else{
////                Erro
////            }
////
////        } else if (token pertence ao conjunto primeiro de identificador4){
////            identificador4();
////        }
////        else{
////            Erro;
////        }
//
//
//    }

//

//

//
//    public static void identificadorSemFuncao(){
//
//        if(isPrimeiro("Escopo")){
//            escopo();
//            if (token == "identificador"){
//                token = proximo_token();
//                identificador2();
//            }
//
//        }
//        else if (token == "identificador"){
//            token = proximo_token();
//            identificador2();
//
//        }
//        else{
//            erro();
//        }
//    }
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
