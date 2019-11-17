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

        Character caractere = ' ';
        String estado_atual = "inicio";
        int linha_atual = 1;
        String lexema = "";
        int ascii = 0;

        while(caractere != null){

            caractere = this.obterCaractere();

            if (caractere != null)
                ascii = (int) caractere;
            else
                ascii = -1;

            if (ascii != 13) {

                switch (estado_atual) {
                    case "inicio":
                        lexema = "";

                        if ((ascii >= 65 && ascii <= 90) || (ascii >= 97 && ascii <= 122)) {
                            lexema = lexema + caractere;
                            estado_atual = "identificador";
                        } else if (ascii >= 48 && ascii <= 57) {
                            lexema = lexema + caractere;
                            estado_atual = "digito_s1";
                        } else if (ascii == 47) {
                            lexema = lexema + caractere;

                            caractere = this.obterCaractere();

                            if (caractere != null) {

                                ascii = (int) caractere;

                                if (ascii == 47)
                                    estado_atual = "comentario_linha";
                                else if (ascii == 42)
                                    estado_atual = "comentario_bloco_s1";
                                else {
                                    this.inserirToken(new Token(lexema, linha_atual, 8));
                                }

                                this.devolverCaractere(caractere);

                            } else
                                this.inserirToken(new Token(lexema, linha_atual, 8));


                        } else if (ascii == 43 || ascii == 45) {

                            lexema = lexema + caractere;
                            caractere = this.obterCaractere();

                            if (caractere != null) {
                                ascii = (int) caractere;

                                if (ascii == 43 || ascii == 45)
                                    estado_atual = "operador_incremento";
                                else {
                                    this.inserirToken(new Token(lexema, linha_atual, 9));
                                }
                                this.devolverCaractere(caractere);

                            } else
                                this.inserirToken(new Token(lexema, linha_atual, 8));

                        } else if (ascii == 45) {
                            lexema = lexema + caractere;
                            caractere = this.obterCaractere();

                            if (ascii == 45) {
                                estado_atual = "operador_incremento";
                            } else if (ascii == 9 || ascii == 32 || (ascii >= 48 && ascii <= 57)) {
                                estado_atual = "operador_ou_digito";
                                this.devolverCaractere(caractere);
                            } else {
                                estado_atual = "operador_aritmetico";
                                lexema = "";
                                this.devolverCaractere(caractere);

                            }

                            this.devolverCaractere(caractere);

                        } else if (ascii == 42) {
                            this.inserirToken(new Token(caractere + "", linha_atual, 8));

                        } else if (ascii == 34) {
                            estado_atual = "cadeia_caractere_s1";


                        } else if (ascii == 59) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 12));
                        } else if (ascii == 44) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 13));
                        } else if (ascii == 40) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 14));
                        } else if (ascii == 41) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 15));
                        } else if (ascii == 91) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 16));
                        } else if (ascii == 93) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 17));
                        } else if (ascii == 123) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 18));
                        } else if (ascii == 126) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 19));
                        } else if (ascii == 46) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 20));
                        } else if (ascii == 61) {
                            lexema = lexema + caractere;
                            estado_atual = "igual";
                        } else if (ascii == 33) {
                            lexema = lexema + caractere;
                            estado_atual = "negacao";
                        } else if (ascii == 38) {
                            lexema = lexema + caractere;
                            estado_atual = "eComercial";
                        } else if (ascii == 60 || ascii == 62) {
                            lexema = lexema + caractere;
                            estado_atual = "lessGreater";
                        } else if (ascii == 124) {
                            lexema = lexema + caractere;
                            estado_atual = "or";
                        }

                        break;
                    case "igual":
                        if (ascii == 61) {
                            lexema = lexema + caractere;
                            this.inserirToken(new Token(lexema, linha_atual, 6));
                        } else {
                            this.inserirToken(new Token(lexema, linha_atual, 7));
                            this.devolverCaractere(caractere);
                        }
                        estado_atual = "inicio";
                        break;
                    case "negacao":
                        if (ascii == 61) {
                            lexema = lexema + caractere;
                            this.inserirToken(new Token(lexema, linha_atual, 6));
                        } else {
                            this.inserirToken(new Token(lexema, linha_atual, 5));
                            this.devolverCaractere(caractere);
                        }
                        estado_atual = "inicio";
                        break;
                    case "lessGreater":
                        if (ascii == 61) {
                            lexema = lexema + caractere;
                            this.inserirToken(new Token(lexema, linha_atual, 6));
                        } else {
                            this.inserirToken(new Token(lexema, linha_atual, 6));
                            this.devolverCaractere(caractere);
                        }
                        estado_atual = "inicio";
                        break;
                    case "eComercial":
                        if (ascii == 38) {
                            lexema = lexema + caractere;
                            this.inserirToken(new Token(lexema, linha_atual, 4));
                        } else {
                            System.out.println("erro de eComercial mal formado, tratar e colocar na classe depois");
                            this.devolverCaractere(caractere);
                        }
                        estado_atual = "inicio";
                        break;
                    case "or":
                        if (ascii == 124) {
                            lexema = lexema + caractere;
                            this.inserirToken(new Token(lexema, linha_atual, 4));
                        } else {
                            System.out.println("erro de mal formado o or, tratar e colocar na classe depois");
                            this.devolverCaractere(caractere);
                        }
                        estado_atual = "inicio";
                        break;
                    case "comentario_linha":

                        if (ascii != 10 && caractere != null)
                            lexema = lexema + caractere;
                        else {
                            this.inserirToken(new Token(lexema, linha_atual, 10));
                            estado_atual = "inicio";
                        }
                        break;

                    case "comentario_bloco_s1":

                        lexema = lexema + caractere;
                        estado_atual = "comentario_bloco_s2";
                        break;
                    case "comentario_bloco_s2":


                        if (caractere != null) {
                            lexema = lexema + caractere;
                            if (ascii == 42)
                                estado_atual = "comentario_bloco_s3";
                        } else {
                            this.inserirErro(new Erro(lexema, linha_atual, 10));
                            estado_atual = "inicio";
                        }


                        break;

                    case "comentario_bloco_s3":

                        if (caractere != null) {
                            lexema = lexema + caractere;
                            ascii = (int) caractere;

                            if (ascii != 42 && ascii != 47) {
                                estado_atual = "comentario_bloco_s2";
                            } else if (ascii == 47) {
                                this.inserirToken(new Token(lexema, linha_atual, 10));
                                estado_atual = "inicio";
                            }

                        } else {
                            this.inserirErro(new Erro(lexema, linha_atual, 10));
                            estado_atual = "inicio";

                        }

                        break;

                    case "operador_aritmetico":
                        if (caractere != null) {
                            this.devolverCaractere(caractere);
                            this.inserirToken(new Token(lexema, linha_atual, 8));
                        } else
                            this.inserirToken(new Token(lexema, linha_atual, 8));
                        estado_atual = "inicio";

                        break;
                    case "operador_incremento":
                        lexema = lexema + caractere;
                        this.inserirToken(new Token(lexema, linha_atual, 9));
                        estado_atual = "inicio";
                        break;

                    case "cadeia_caractere_s1":

                        if (ascii != 10) {
                            lexema = lexema + caractere;

                            if (ascii == 92)
                                estado_atual = "cadeia_caractere_s2";
                            else if (ascii == 34) {
                                this.inserirToken(new Token(lexema, linha_atual, 11));
                                estado_atual = "inicio";
                            } else if (ascii < 32 || ascii > 126) {
                                this.inserirErro(new Erro(lexema, linha_atual, 11));
                                estado_atual = "inicio";
                            }

                        } else {
                            this.inserirErro(new Erro(lexema, linha_atual, 11));
                            estado_atual = "inicio";
                        }
                        break;

                    case "cadeia_caractere_s2":

                        if (ascii != 10) {
                            lexema = lexema + caractere;

                            if (ascii >= 32 || ascii <= 126) {
                                estado_atual = "cadeia_caractere_s1";
                            } else {
                                this.inserirErro(new Erro(lexema, linha_atual, 11));
                                estado_atual = "inicio";
                            }
                        } else {
                            this.inserirErro(new Erro(lexema, linha_atual, 11));
                            estado_atual = "inicio";
                        }
                        break;

                    case "identificador":
                        if ((ascii >= 65 && ascii <= 90) || (ascii >= 97 && ascii <= 122) || (ascii >= 48 && ascii <= 57) || ascii == 95)
                            lexema = lexema + caractere;
                        else {
                            String aux = "" + caractere;
                            if (aux.matches("[+*/!=<>|&;,(){}. \n\t]") || ascii == 45 || ascii == 91 || ascii == 93 || ascii == -1) {
                                if (lexema.matches("^(var|const|typedef|struct|extends|procedure|function|start|return|if|else|then|while|read|print|int|real|boolean|string|true|false|global|local)$"))
                                    this.inserirToken(new Token(lexema, linha_atual, 0));
                                else
                                    this.inserirToken(new Token(lexema, linha_atual, 3));
                                if (caractere != null)
                                    this.devolverCaractere(caractere);
                                estado_atual = "inicio";
                            }
                            else {
                                lexema = lexema + caractere;
                                estado_atual = "identificador_error";
                            }
                        }
                        break;
                    case "identificador_error":
                        String aux = "" + caractere;
                        if (aux.matches("[+*/!=<>|&;,(){}. \n\t]") || ascii == 45 || ascii == 91 || ascii == 93 || ascii == -1) {
                            this.inserirErro(new Erro(lexema, linha_atual, 3));
                            estado_atual = "inicio";
                            if (caractere != null)
                                this.devolverCaractere(caractere);
                        }
                        else
                            lexema = lexema + caractere;
                        break;
                    case "operador_ou_digito":
                        if (ascii >= 48 && ascii <= 57) {
                            estado_atual = "digito_s1";
                            lexema = lexema + caractere;
                        } else if (ascii != 9 && ascii != 32) {
                            lexema = "-";
                            this.inserirToken(new Token(lexema, linha_atual, 8));
                            if (caractere != null)
                                this.devolverCaractere(caractere);
                            estado_atual = "inicio";
                        }
                        break;
                    case "digito_s1":
                        if (ascii >= 48 && ascii <= 57) {
                            lexema = lexema + caractere;
                        } else if (ascii == 46) {
                            caractere = this.obterCaractere();
                            if (ascii >= 48 && ascii <= 57) {
                                lexema = lexema + ".";
                                estado_atual = "digito_s2";
                            } else {
                                this.inserirToken(new Token(lexema, linha_atual, 2));
                                estado_atual = "inicio";
                            }
                            if (caractere != null)
                                this.devolverCaractere(caractere);
                        } else {
                            this.inserirToken(new Token(lexema, linha_atual, 2));
                            estado_atual = "inicio";
                            if (caractere != null)
                                this.devolverCaractere(caractere);
                        }
                        break;
                    case "digito_s2":
                        if ((ascii >= 48 && ascii <= 57)) {
                            lexema = lexema + caractere;
                        } else {
                            this.inserirToken(new Token(lexema, linha_atual, 1));
                            estado_atual = "inicio";
                            if (caractere != null)
                                this.devolverCaractere(caractere);
                        }
                        break;
                }

                if (ascii == 10) {
                    linha_atual++;
                    System.out.println("Linha: " + linha_atual);
                }
            }
        }
    }


    public void escreverEmArquivo() throws IOException {

        OutputStream arquivo;
        OutputStreamWriter arquivoEscrita;

        File fileSaida= new File("output/"+"saida"+this.file.getName().replaceAll("[^0-9]","")+".txt");
        arquivo = new FileOutputStream(fileSaida);
        arquivoEscrita = new OutputStreamWriter(arquivo);
        Iterator it = getTokens().iterator();

        if(! this.tokensIsVazio())
            arquivoEscrita.write("Lista de Tokens\n\n");

        int i = 0;
        while(it.hasNext()){
            //System.out.println("Hello " + it.next().toString());
            arquivoEscrita.write(it.next().toString());
        }

        it = getErros().iterator();

        if(!this.errosIsVazio())
            arquivoEscrita.write("\n\nLista de Erros\n\n");

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