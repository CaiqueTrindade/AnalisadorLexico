import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AnalisadorLexico {

    private FileInputStream fileInputStream;
    private InputStreamReader inputStreamReader;
    private PushbackReader pushbackReader;
    private File file;

    List<Token> tokens = new ArrayList<>();
    List<Erro> erros = new ArrayList<>();


    public AnalisadorLexico(File file) throws FileNotFoundException{

        this.file = file;
        this.fileInputStream = new FileInputStream(this.file);
        this.inputStreamReader = new InputStreamReader(this.fileInputStream);
        this.pushbackReader = new PushbackReader(this.inputStreamReader);


    }


    public void inserirToken(Token token){

        this.tokens.add(token);
    }

    public void inserirErro(Erro erro){

        this.erros.add(erro);
    }

    public Token recuperaToken(int indice){

        return this.tokens.get(indice);
    }

    public List<Token> getTokens(){

        return this.tokens;

    }

    public List<Erro> getErros(){

        return this.erros;
    }

    public boolean tokensIsVazio(){

        return this.tokens.isEmpty();

    }

    public boolean errosIsVazio(){

        return this.erros.isEmpty();

    }

    public Character obterCaractere() throws  IOException{

        Character c = null;
        int i = this.pushbackReader.read();

        if(i != -1){
            c = (char) i;
        }

        return c;
    }

    public void devolverCaractere(Character c) throws IOException{

        this.pushbackReader.unread(c);
    }



    public void executarAnalise() throws IOException{

        Character caractere = null;
        String estado_atual = "inicio";
        int linha_atual = 1;
        String lexema = "";

        while(caractere != null){

            caractere = this.obterCaractere();

            if ((int)caractere == 10)
                linha_atual++;

            switch (estado_atual) {

                case "inicio":
                    lexema = "";

                    if ((int)caractere == 47){
                        lexema = lexema + caractere;
                        caractere = this.obterCaractere();

                        if ((int) caractere == 47)
                            estado_atual = "comentario_linha";

                        else if ((int) caractere == 42)
                            estado_atual = "comentario_bloco";
                        else {
                            estado_atual = "operador_aritmetico";
                            lexema = "";
                            this.devolverCaractere(caractere);
                        }
                        this.devolverCaractere(caractere);

                    } else if ((int) caractere == 43) {

                        lexema = lexema + caractere;
                        caractere = this.obterCaractere();

                        if ((int) caractere == 43) {
                            estado_atual = "operador_incremento";
                        } else {
                            estado_atual = "operador_aritmetico";
                            lexema = "";
                            this.devolverCaractere(caractere);

                        }
                        this.devolverCaractere(caractere);


                    } else if ((int) caractere == 45) {
                        lexema = lexema + caractere;
                        caractere = this.obterCaractere();

                        if ((int) caractere == 45) {
                            estado_atual = "operador_incremento";
                        } else {
                            estado_atual = "operador_aritmetico";
                            lexema = "";
                            this.devolverCaractere(caractere);

                        }
                        this.devolverCaractere(caractere);

                    } else if ((int) caractere == 42) {
                                estado_atual = "operador_aritmetico";

                    } else if ((int)caractere == 34){
                                estado_atual = "cadeia_caractere_s1";

                    }


                case "comentario_linha":

                    if ((int) caractere != 10)
                        lexema = lexema + caractere;
                    else {
                        this.inserirToken(new Token(lexema, linha_atual, 10));
                        estado_atual = "inicio";
                    }

                case "comentario_bloco_s1":
                    lexema = lexema + caractere;
                    estado_atual = "comentario_bloco_s2";

                case "comentario_bloco_s2":

                    if (caractere != null) {
                        lexema = lexema + caractere;

                        if ((int) caractere == 42)
                            estado_atual = "comentario_bloco_s3";
                    }
                    else
                        estado_atual = "erro_comentario_bloco"; //Falta criar o erro do comentario



                case "comentario_bloco_s3":

                    if (caractere != null){
                        lexema = lexema + caractere;

                        if((int)caractere != 42)
                            estado_atual = "comentario_bloco_s2";
                        else if ((int)caractere == 47) {
                            estado_atual = "estado_final_comentario_bloco";
                        }
                    }
                    else
                        estado_atual = "erro_comentario_bloco"; //Falta criar o erro do comentario



                case "estado_final_comentario_bloco":
                    this.inserirToken(new Token(lexema,linha_atual,10));
                    estado_atual = "inicio";



                case "operador_aritmetico":
                    lexema = lexema + caractere;
                    this.inserirToken(new Token(lexema, linha_atual, 8));
                    estado_atual = "inicio";


                case "operador_incremento":
                    lexema = lexema + caractere;
                    this.inserirToken(new Token(lexema, linha_atual, 9));
                    estado_atual = "inicio";

                case "cadeia_caractere_s1":

                   if ((int)caractere != 10){

                       if ((int)caractere == 92 )
                           estado_atual = "cadeia_caractere_s2";
                       else if ((int)caractere == 34  )
                                estado_atual = "estado_final_cadeia_caractere";

                   } else
                       estado_atual = "cadeia_caractere_erro";


                case "cadeia_caractere_s2":

                    if ((int)caractere != 10){

                        if (Character.toString(caractere).matches("^([a-z]|[A-Z]|[0-9]|\")$") ||  (int)caractere >= 32 && (int)caractere <= 126) {
                                estado_atual = "cadeia_caractere_s1";
                        }
                        else
                            estado_atual = "cadeia_caractere_erro";
                    }
                    else
                        estado_atual = "cadeia_caractere_erro";


                case "estado_final_cadeia_caractere":

                    this.inserirToken(new Token(lexema,linha_atual,11));
                    estado_atual = "inicio";

                case "cadeia_caractere_erro":















            }

        }
    }


    public void escreverEmArquivo() throws IOException {

        OutputStream arquivo;
        OutputStreamWriter arquivoEscrita;

        arquivo = new FileOutputStream(this.file);
        arquivoEscrita = new OutputStreamWriter(arquivo);
        Iterator it = getTokens().iterator();

        if(! this.tokensIsVazio())
           arquivoEscrita.write("Lista de Tokens\n");

        while(it.hasNext()){
             arquivoEscrita.write(it.next().toString());
        }

        it = getErros().iterator();

        if(! this.errosIsVazio())
            arquivoEscrita.write("Lista de Erros\n");

        while (it.hasNext()){
            arquivoEscrita.write(it.next().toString());

        }

        arquivoEscrita.flush();
        arquivoEscrita.close();
    }



}


/* Exemplo inicial do autômato de identificadores e palavras reservadas
try {
InputStream entrada = new FileInputStream("input\\entrada1.txt");

int c, estado_atual, linha_atual;
String lista_tokens = "", caractere_atual;

estado_atual = 0;
linha_atual = 0;

// Lê o próximo caractere em ASCII
c = entrada.read();

boolean repete = true;


while (repete) {

    // Verdadeiro quando o caractere atual é diferente de um caractere de retorno ao início da linha \r
    if (c != 13) {

        // Transforma o ASCII em seu respectivo caractere e o armazena em uma String
        caractere_atual = Character.toString((char) c);

        // Simulação do autômato
        switch (estado_atual) {
            case 0:
                if (caractere_atual.matches("^([a-z]|[A-Z])$")) {
                    estado_atual = 1;
                    lista_tokens += "" + linha_atual + " " + caractere_atual;
                }
                else if (c == 10) {
                    linha_atual ++;
                }
                // Caso de erro (quando o caractere não faz parte da gramática)
                else if (c != 9 && c != 32 && c != -1) {
                    estado_atual = -1;
                    lista_tokens += "" + linha_atual + " " + caractere_atual;
                }

                if (c != -1)
                    c = entrada.read();
                else
                    repete = false;
                break;
            case 1:
                if (caractere_atual.matches("^([a-z]|[A-Z]|[0-9]|_)$")) {
                    lista_tokens += caractere_atual;
                    c = entrada.read();
                }
                // Verdadeiro quando o caractere atual é algum delimitador (incompleto e provavelmente sofrerá mudanças significativas)
                else  if (c == 9 || c == 32 || c == 10 || c == -1) {
                    String tabela_aux[] = lista_tokens.split("\n");
                    String ultima_linha = tabela_aux[tabela_aux.length - 1];

                    if (ultima_linha.split(" ")[1].matches("^(var|const|typedef|struct|extends|procedure|function|start|return|if|else|then|while|read|print|int|real|boolean|string|true|false|global|local)$"))
                        lista_tokens += " " + ultima_linha.split(" ")[1] + " (palavra reservada)\n";
                    else
                        lista_tokens += " identificador\n";

                    estado_atual = 0;

                    if (c == 9 || c == 32)
                        c = entrada.read();
                }
                // Caso de erro
                else {
                    lista_tokens += caractere_atual;
                    estado_atual = -1;
                    c = entrada.read();
                }
                break;
            case -1:
                if (c == 9 || c == 32 || c == 10 || c == -1){
                    lista_tokens += " ???\n";
                    estado_atual = 0;

                    if (c == 9 || c == 32)
                        c = entrada.read();
                }
                else {
                    lista_tokens += caractere_atual;
                    c = entrada.read();
                }
        }
    }
    else {
        c = entrada.read();
    }
}

System.out.print(lista_tokens);
entrada.close();
} catch (FileNotFoundException e) {
e.printStackTrace();
} catch (IOException e) {
e.printStackTrace();
}
*/

