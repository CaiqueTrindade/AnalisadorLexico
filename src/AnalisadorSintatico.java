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
        this.token = tokens.size()>0?tokens.get(0):null;
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
            for (int i = 0; i < aux.length; i++) tokens_sincronizacao.addAll(conjuntos_P_S.primeiro(aux[i]));
        }

        if (conjuntos_seguinte != null) {
            String aux[] = conjuntos_seguinte.split("#");
            for (int i = 0; i < aux.length; i++) tokens_sincronizacao.addAll(conjuntos_P_S.seguinte(aux[i]));
        }

        if(lexemas != null) {
            String aux[] = conjuntos_seguinte.split("#");
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

    public List<ErroSintatico> getListaErrosSintaticos(){
        return this.errosSintaticos;

    }


    public void Const(){

    }

    public void Var(){

        if (token.getLexema().equals("var")){
            nextToken();
            if (token.getLexema().equals("{")){
                nextToken();
                tipoVar();
            }
            else{
                addErroSintatico(new ErroSintatico("Var", "Esperava { mas encontrou "+token.getLexema(),token.getnLinha()));
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
























//
//    public static void var3(){
//
//        if (token.equals("}")){
//            token = proximo_token();
//        }
//        else if (isPrimeiro("TipoVar")){
//            tipoVar();
//        }
//        else{
//            erro();
//        }
//
//    }
//
//    public static  void var4(){
//
//        if (token.equals(",")){
//            token = proximo_token();
//            idVar();
//
//        }
//        else if (token.equals(";")){
//            token = proximo_token();
//            var3();
//
//        }
//        else{
//            erro();
//        }
//
//
//    }
//

//    public static  void valorVetor(){
//
//    }
//    public static void matriz(){
//
//        if (token.equals("[")){
//            token = proximo_token();
//            valorVetor();
//            if (token.equals("]")){
//                token = proximo_token();
//                var4();
//            }
//
//        }
//        else if (isPrimeiro("Var4")) {
//            var4();
//        }
//        else {
//            erro();
//        }
//
//
//    }
//    public static void vetor3(){
//
//        if (token.equals("[")){
//            token = proximo_token();
//            valorVetor();
//            if (token.equals("]")){
//                token = proximo_token();
//                matriz();
//            }
//
//        }
//        else{
//            erro();
//        }
//    }
//    public static  void var2(){
//
//        if (isPrimeiro("Var2")){
//            if (token.equals(",")){
//                token = proximo_token();
//                idVar();
//            }
//            else if (token.equals(";")){
//                token = proximo_token();
//                var3();
//
//
//            }
//            else if (token.equals("=")){
//                token = proximo_token();
//                valor();
//                var4();
//            }
//            else {
//                erro();
//            }
//        }
//        else if (isPrimeiro("Vetor")){
//            vetor3();
//            var4();
//
//        } else {
//            erro();
//        }
//
//
//    }
//    public static  void idVar(){
//
//        if (token == "identificador"){
//            token = proximo_token();
//            var2();
//
//        }
//        else{
//            erro();
//        }
//
//    }
//
//    public static void tipoVar(){
//
//        if (isPrimeiro("Tipo")){
//            token = proximo_token();
//            tipo();
//            idVar();
//
//        }
//
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
//    public static void escopo(){
//        if (token.equals("local") || token.equals("global")){
//            //token = proximo_token();
//            if (token.equals(".")){
//                //token = proximo_token()
//            }
//            else{
//                //Erro
//            }
//        }
//        else{
//            //Erro
//        }
//
//    }
//
//    public static  void identificador2(){
////
////        if (token.equals(".")){
////            //token = proximo_token();
////            if (token é um identificador){
////                token = proximo_token();
////                vetor();
////            }
////            else{
////                Erro;
////            }
////        }else if (token pertence ao conjunto primeiro de vetor){
////                vetor();
////        }
////        else{
////            Erro
////        }
//    }
//
//    public static  void listaParametros2(){
//
//        if (token == "identificador"){
//            token = proximo_token();
//        }
//        else if (token == "numero"){
//            token = proximo_token();
//        } else if (token == "String") {
//            token = proximo_token();
//        }
//        else{
//            erro();
//        }
//
//    }
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
//    public static void contListaParametros(){
//
//        if (token.equals(",")){
//            token = proximo_token();
//            listaParametros();
//        }else if (!isSeguinte("ListaParametros")){
//            erro();
//        }
//
//
//    }
//
//    public static  void listaParametros(){
//        if (isPrimeiro("ListaParametros2")){
//            listaParametros2();
//            contListaParametros();
//
//        }
//
//
//    }
//
//    public static  void identificador3(){
//
//        if (isPrimeiro("Identificador2")){
//            identificador2();
//        }
//        else if (token.equals("(")){
//            token = proximo_token();
//            listaParametros();
//            if (token.equals(")")){
//                token = proximo_token();
//            }
//            else
//                erro();
//
//        }
//        else
//            erro();
//
//
//
//    }
//    public static void identificador(){
//
//        if (isPrimeiro("escopo")){
//            escopo();
//
//            if (token == "expressão regular para identificador"){
//                token = proximo_token();
//                identificador2();
//
//            }
//            else{
//                //conjunto primeiro de Identificador2() $
//                erro();
//            }
//        }
//        else if (token == "expressão regular para identificador"){
//            token = proximo_token();
//            identificador3();
//
//        }
//        else{
//            //seguinte de escopo ou primeiro de de Identificador3 e $
//            erro();
//
//        }
//
//
//
//    }


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
