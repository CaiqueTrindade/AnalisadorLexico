import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Classe responsável por realizar a análise léxica
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */

public class AnalisadorLexico {

    private FileInputStream fileInputStream;  //Atributo que representa o inputStream
    private InputStreamReader inputStreamReader; //Atributo que representa o leitor do arquivo
    private PushbackReader pushbackReader; //Atributo que auxilia na etapa de leitura e look a head
    private File file;

    List<Token> tokens = new ArrayList<>(); //Atributo que armazena a lista de tokens
    List<Erro> erros = new ArrayList<>(); //Atributo que armazena a lista de erros

    /**
     * Construtor da classe do Analisador Léxico
     * @param file recebe o arquivo atual que será submetido a análise léxica
     * @throws FileNotFoundException lança essa exceção, caso o arquivo não exista
     */

    public AnalisadorLexico(File file) throws FileNotFoundException{

        this.file = file;
        this.fileInputStream = new FileInputStream(this.file);
        this.inputStreamReader = new InputStreamReader(this.fileInputStream);
        this.pushbackReader = new PushbackReader(this.inputStreamReader);


    }


    /**
     * Insere token na lista de tokens
     * @param token token atual a ser inserido
     */
    public void inserirToken(Token token){

        this.tokens.add(token);
    }


    /**
     * Insere erro na lista erros
     * @param erro erro atual a ser inserido
     */
    public void inserirErro(Erro erro){

        this.erros.add(erro);
    }

    /**
     * Recupera um token baseado no indice dele na lista
     * @param indice indice do token a ser recuperado
     * @return
     */
    public Token recuperaToken(int indice){

        return this.tokens.get(indice);
    }

    /**
     * Recupera a lista de tokens
     * @return a lista atual de tokens
     */

    public List<Token> getTokens(){

        return this.tokens;

    }

    /**
     * Recupera a lista de erros
     * @return a lista atual de erros
     */
    public List<Erro> getErros(){

        return this.erros;
    }

    /**
     * Verifica se a lista de tokens está vazia
     * @return true se estiver vazia ou false caso contrário
     */
    public boolean tokensIsVazio(){

        return this.tokens.isEmpty();

    }

    /**
     * Verifica se alista de erros está vazia
     * @return true se estiver vazia ou false caso contrário
     */
    public boolean errosIsVazio(){

        return this.erros.isEmpty();

    }

    /**
     * Realiza a leitura do caractere
     * @return o caractere lido, ou null caso seja final de arquivo
     * @throws IOException  exceção caso ocorra algum erro no processo de obtenção do caractere
     */
    public Character obterCaractere() throws  IOException{

        Character c = null;
        int i = this.pushbackReader.read();

        if(i != -1){
            c = (char) i;
        }

        return c;
    }

    /**
     * Devolve o caractere lido atualmente. Esse método é responável por auxiliar no processo de look a head
     * @param c o caracatere a ser devolvido
     * @throws IOException
     */
    public void devolverCaractere(Character c) throws IOException{

        this.pushbackReader.unread(c);
    }


    /**
     * Método responsável por fazer a análise léxica propriamente dita.
     * @throws IOException exceção caso ocorra algum problema no processo de análise léxica
     */
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
                        } else if (ascii == 10) {
                            linha_atual++;
                            System.out.println("Linha: " + linha_atual);
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


                        } else if (ascii == 43) {

                            lexema = lexema + caractere;
                            caractere = this.obterCaractere();

                            if (caractere != null) {
                                ascii = (int) caractere;

                                if (ascii == 43)
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

                            if (caractere != null)
                                ascii = (int) caractere;
                            else
                                ascii = -1;

                            System.out.println(ascii);

                            if (ascii == 45) {
                                estado_atual = "operador_incremento";
                            } else if (ascii == 9 || ascii == 32 || (ascii >= 48 && ascii <= 57)) {
                                estado_atual = "operador_ou_digito";
                            } else {
                                this.inserirToken(new Token(lexema, linha_atual, 8));
                            }

                            if (caractere != null)
                                this.devolverCaractere(caractere);

                        } else if (ascii == 42) {
                            this.inserirToken(new Token(caractere + "", linha_atual, 8));

                        } else if (ascii == 34) {
                            lexema = lexema + caractere;
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
                            if (ascii == 10)
                                this.devolverCaractere(caractere);
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

                        if (ascii != 10 && ascii != -1) {
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

                            if(ascii != -1)
                                this.devolverCaractere(caractere);
                            estado_atual = "inicio";
                        }
                        break;

                    case "cadeia_caractere_s2":

                        if (ascii != 10 && ascii != -1 ) {
                            lexema = lexema + caractere;

                            if (ascii >= 32 && ascii <= 126) {
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
                        else {
                            lexema = lexema + caractere;
                        }
                        break;
                    case "digito_s1":
                        if (ascii >= 48 && ascii <= 57) {
                            lexema = lexema + caractere;
                        } else if (ascii == 46) {
                            caractere = this.obterCaractere();

                            if (caractere != null)
                                ascii = (int) caractere;
                            else
                                ascii = -1;

                            if (ascii >= 48 && ascii <= 57) {
                                lexema = lexema + ".";
                                estado_atual = "digito_s2";
                            }
                            else {
                                this.inserirToken(new Token(lexema, linha_atual, 2));
                                this.inserirToken(new Token(".", linha_atual, 20));
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
                        if ((ascii >= 48 && ascii <= 57))
                            lexema = lexema + caractere;
                        else {
                            this.inserirToken(new Token(lexema, linha_atual, 1));
                            estado_atual = "inicio";
                            if (caractere != null)
                                this.devolverCaractere(caractere);
                        }
                        break;
                }
            }
        }
    }


    /**
     * Método responsável por escrever os tokens, bem como os erros no arquivo de saída
     * @throws IOException exceção caso ocorra algum erro no processo de escrita do arquivo
     */
    public void escreverEmArquivo() throws IOException {

        OutputStream arquivo;
        OutputStreamWriter arquivoEscrita;

        File fileSaida= new File("output/"+"saida"+this.file.getName().replaceAll("[^0-9]","")+".txt");
        arquivo = new FileOutputStream(fileSaida);
        arquivoEscrita = new OutputStreamWriter(arquivo);
        Iterator it = getTokens().iterator();

        if(!this.tokensIsVazio())
            arquivoEscrita.write("Lista de Tokens\n\n");

       
        while(it.hasNext()){

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
