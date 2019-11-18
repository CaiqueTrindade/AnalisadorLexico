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

            // Verifica se o caractere lido não é um caractere de retorno de linha (\r), ignorando-o na análise
            if (ascii != 13) {

                // Switch que “simula” um autômato para reconhecer os padrões léxicos da linguagem
                switch (estado_atual) {
                    case "inicio":
                        lexema = "";

                        // Verifica se o caractere lido é uma letra do alfabeto (em regex: [a-zA-Z])
                        if ((ascii >= 65 && ascii <= 90) || (ascii >= 97 && ascii <= 122)) {
                            lexema = lexema + caractere;
                            estado_atual = "identificador";
                        // Verifica se o caractere é um dígito numérico (em regex: [0-9]
                        } else if (ascii >= 48 && ascii <= 57) {
                            lexema = lexema + caractere;
                            estado_atual = "digito_s1";
                        // Verifica se o caractere é uma quebra de linha (\n)
                        } else if (ascii == 10) {
                            linha_atual++; //  Autoincremento na contagem de linhas
                        //Condição para quando o caractere lido é uma / (barra)
                        } else if (ascii == 47) {
                            lexema = lexema + caractere; //Concatena a barra
                            caractere = this.obterCaractere(); //Faz o look a head

                            //Verifica se o caractere lido é o final do arquivo
                            if (caractere != null) {

                                ascii = (int) caractere;

                                //Caso o caractere do look a head seja outra barra, entra nessa condição
                                if (ascii == 47)
                                    estado_atual = "comentario_linha";
                                //Caso o caractere do look a head seja o *, entra nessa condição
                                else if (ascii == 42)
                                    estado_atual = "comentario_bloco_s1";
                                else {
                                    //Insere o Token de operador aritmético na lista de tokens
                                    this.inserirToken(new Token(lexema, linha_atual, 8));
                                }

                                //Devolve o caractere lido pelo look a head
                                this.devolverCaractere(caractere);

                            } else
                                //Insere o Token de operador aritmético na lista de tokens
                                this.inserirToken(new Token(lexema, linha_atual, 8));

                        //Condicional para o caractere +
                        } else if (ascii == 43) {

                            lexema = lexema + caractere;
                            //Faz o look a head
                            caractere = this.obterCaractere();
                            //Verifica se o caractere lido é o final do arquivo
                            if (caractere != null) {
                                ascii = (int) caractere;

                                //Caso o caractere do look a head seja outro +, entra nessa condição
                                if (ascii == 43)
                                    estado_atual = "operador_incremento";
                                else {
                                    //Insere o Token de operador aritmético na lista de tokens
                                    this.inserirToken(new Token(lexema, linha_atual, 8));
                                }
                                //Devolve o caractere lido pelo look a head
                                this.devolverCaractere(caractere);

                            } else
                                //Insere o Token de operador aritmético na lista de tokens
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
                        //Condicional para quando o caractere lido é um *
                        } else if (ascii == 42) {
                            this.inserirToken(new Token(caractere + "", linha_atual, 8));

                        //Condição para quando o caractere lido é uma " (aspas)
                        } else if (ascii == 34) {
                            lexema = lexema + caractere;
                            estado_atual = "cadeia_caractere_s1";

                        // Condição para quando o caractere lido é um ; (ponto-e-vírgula)
                        } else if (ascii == 59) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 12));
                        // Condição para quando o caractere lido é uma , (vírgula)
                        } else if (ascii == 44) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 13));
                        // Condição para quando o caractere lido é um ( (abertura de parêntese)
                        } else if (ascii == 40) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 14));
                        // Condição para quando o caractere lido é um ) (fechamento de parêntese)
                        } else if (ascii == 41) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 15));
                        // Condição para quando o caractere lido é um [ (abertura de colchete)
                        } else if (ascii == 91) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 16));
                        // Condição para quando o caractere lido é um ] (fechamento de colchete)
                        } else if (ascii == 93) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 17));
                        // Condição para quando o caractere lido é uma { (abertura de chave)
                        } else if (ascii == 123) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 18));
                        // Condição para quando o caractere lido é uma } (fechamento de chave)
                        } else if (ascii == 126) {
                            this.inserirToken(new Token(caractere.toString(), linha_atual, 19));
                        // Condição para quando o caractere lido é um . (ponto)
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
                        //Verifica se não é final de linha enm final de arquivo
                        if (ascii != 10 && caractere != null)
                            lexema = lexema + caractere;
                        else {
                            //Insere token de Delimitador de Comentário na lista de tokens
                            this.inserirToken(new Token(lexema, linha_atual, 10));
                            estado_atual = "inicio";
                            //verifica se chegou ao fim da linha
                            if (ascii == 10)
                                //Devolve o caractere de final de linha
                                this.devolverCaractere(caractere);
                        }
                        break;

                    case "comentario_bloco_s1":
                        //Concatena o caractere lido
                        lexema = lexema + caractere;
                        estado_atual = "comentario_bloco_s2";
                        break;

                    case "comentario_bloco_s2":

                        //Verifica se o caractere lido é o final de arquivo
                        if (caractere != null) {
                            lexema = lexema + caractere;
                            //Verifica se o caractere lido é o *
                            if (ascii == 42)
                                estado_atual = "comentario_bloco_s3";
                        } else {
                            //Insere erro de mal formação de Delmitador de comentários na lista de erros
                            this.inserirErro(new Erro(lexema, linha_atual, 10));
                            estado_atual = "inicio";
                        }


                        break;

                    case "comentario_bloco_s3":

                        //Verifica se ainda não chegou no final do arquivo
                        if (caractere != null) {
                            lexema = lexema + caractere;
                            ascii = (int) caractere;

                            //Verifica se o caracctere lido é diferente de * e também di
                            if (ascii != 42 && ascii != 47) {
                                estado_atual = "comentario_bloco_s2";
                            } else if (ascii == 47) {
                                //Insere o token de Delimitador de Comentário na lista de tokens
                                this.inserirToken(new Token(lexema, linha_atual, 10));
                                estado_atual = "inicio";
                            }

                        } else {
                            //Insere o token de mal formação de Delimitador de Comentário na lista de erros
                            this.inserirErro(new Erro(lexema, linha_atual, 10));
                            estado_atual = "inicio";

                        }

                        break;

                    case "operador_incremento":
                        //Faz a concatenação
                        lexema = lexema + caractere;
                        //Insere o token de operador de incremento/decremento
                        this.inserirToken(new Token(lexema, linha_atual, 9));
                        estado_atual = "inicio";
                        break;

                    case "cadeia_caractere_s1":

                        //Verifica se não chegou no final de linha nem no final de arquivo
                        if (ascii != 10 && ascii != -1) {
                            lexema = lexema + caractere;

                            //Verifica se o caractere lido é uma / (barra)
                            if (ascii == 92)
                                estado_atual = "cadeia_caractere_s2";
                            //Verifica se o caractere lido é uma " (aspas)
                            else if (ascii == 34) {
                                this.inserirToken(new Token(lexema, linha_atual, 11));
                                estado_atual = "inicio";
                            //Verifica se o caractere lido não faz parte da gramática
                            } else if (ascii < 32 || ascii > 126) {
                                //Insere erro de mal formação de cadeia de caracteres
                                this.inserirErro(new Erro(lexema, linha_atual, 11));
                                estado_atual = "inicio";
                            }

                        } else {
                            //Insere erro de mal formação de cadeia de caracteres
                            this.inserirErro(new Erro(lexema, linha_atual, 11));

                            //Verifica se chegou ao final do arquivo
                            if(ascii != -1)
                                // Devolve o caractere
                                this.devolverCaractere(caractere);
                            estado_atual = "inicio";
                        }
                        break;

                    case "cadeia_caractere_s2":

                        //Verifica se não chegou no final de linha nem se chegou no final do arquivo
                        if (ascii != 10 && ascii != -1 ) {
                            lexema = lexema + caractere;
                            //Verifica se o caractere lido está no intervalo 32-126
                            if (ascii >= 32 && ascii <= 126) {
                                estado_atual = "cadeia_caractere_s1";
                            } else {
                                //Insere erro de mal formação de cadeia de caracteres
                                this.inserirErro(new Erro(lexema, linha_atual, 11));
                                estado_atual = "inicio";
                            }
                        } else {
                            //Insere erro de mal formação de cadeia de caracteres
                            this.inserirErro(new Erro(lexema, linha_atual, 11));
                            estado_atual = "inicio";
                        }
                        break;

                    case "identificador":
                        // Verifica se o caractere lido é alfanumérico ou _ (underline) (em regex: [a-zA-Z0-9_])
                        if ((ascii >= 65 && ascii <= 90) || (ascii >= 97 && ascii <= 122) || (ascii >= 48 && ascii <= 57) || ascii == 95)
                            lexema = lexema + caractere;
                        else {
                            String aux = "" + caractere;
                            // Verifica se o caractere lido é um caractere de sincronização, além do regex apresentado, os caracteres "-" "[" "]" ou final de arquivo
                            if (aux.matches("[+*/!=<>|&;,(){}. \n\t]") || ascii == 45 || ascii == 91 || ascii == 93 || ascii == -1) {
                                // Verifica se o lexema lido é uma palvra reservada
                                if (lexema.matches("^(var|const|typedef|struct|extends|procedure|function|start|return|if|else|then|while|read|print|int|real|boolean|string|true|false|global|local)$"))
                                    this.inserirToken(new Token(lexema, linha_atual, 0));
                                // Se não, reconhece o token como identificador
                                else
                                    this.inserirToken(new Token(lexema, linha_atual, 3));

                                if (caractere != null)
                                    this.devolverCaractere(caractere);

                                estado_atual = "inicio";
                            }
                            // Se não, entende que se trata de um identificador malformado
                            else {
                                lexema = lexema + caractere;
                                estado_atual = "identificador_error";
                            }
                        }
                        break;
                    case "identificador_error":
                        String aux = "" + caractere;
                        // Verifica se o caractere lido é um caractere de sincronização, mesma verificação comentada um pouco acima
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
                        // Verifica se o caractere lido é um dígito numérico (em regex [0-9])
                        if (ascii >= 48 && ascii <= 57) {
                            // Reconhece a ocorrência do "-" como parte de um número negativo (real ou inteiro)
                            estado_atual = "digito_s1";
                            lexema = lexema + caractere;
                        // Se não for um espaço ou caractere de tabulação, entende que a ocorrência do "-" se trata de um operador aritmético
                        } else if (ascii != 9 && ascii != 32) {
                            lexema = "-";
                            this.inserirToken(new Token(lexema, linha_atual, 8));

                            if (caractere != null)
                                this.devolverCaractere(caractere);

                            estado_atual = "inicio";
                        }
                        // Else que entrará apenas quando for um espaço ou caractere de tabulação (ainda indeciso se é um número negativo ou operador aritmético)
                        else {
                            lexema = lexema + caractere;
                        }
                        break;
                    case "digito_s1":
                        // Verifica se o caractere lido é um dígito numérico (em regex [0-9])
                        if (ascii >= 48 && ascii <= 57) {
                            lexema = lexema + caractere;
                        // Se não, verifica se é um . (ponto)
                        } else if (ascii == 46) {
                            // Realiza um “look-ahead”
                            caractere = this.obterCaractere();

                            if (caractere != null)
                                ascii = (int) caractere;
                            else
                                ascii = -1;

                            // Verifica se o caractere seguinte é um dígito numérico
                            if (ascii >= 48 && ascii <= 57) {
                                lexema = lexema + ".";
                                estado_atual = "digito_s2";
                            }
                            // Se não, entende que o número é formado até o caractere antecessor ao ponto e o ponto é um delimitador
                            else {
                                this.inserirToken(new Token(lexema, linha_atual, 2));
                                this.inserirToken(new Token(".", linha_atual, 20));
                                estado_atual = "inicio";
                            }

                            if (caractere != null)
                                this.devolverCaractere(caractere);
                        // Em último caso, reconhece o lexema lido até então como um número e retoma ao estado inicial da máquina
                        } else {
                            this.inserirToken(new Token(lexema, linha_atual, 2));

                            estado_atual = "inicio";

                            if (caractere != null)
                                this.devolverCaractere(caractere);
                        }
                        break;
                    case "digito_s2":
                        // Verifica se o caractere lido é um dígito numérico
                        if ((ascii >= 48 && ascii <= 57))
                            lexema = lexema + caractere;
                        // Caso contrário, reconhece o lexema lido até então como um número e retoma ao estado inicial da máquina
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
        //Estabelece as variáveis de referências dos output de escrita
        OutputStream arquivo;
        OutputStreamWriter arquivoEscrita;

        //Instancia um objeto file, criando o arquivo de saída correpondente
        File fileSaida = new File("output/"+"saida"+this.file.getName().replaceAll("[^0-9]","")+".txt");
        //Instancia o stream de saída
        arquivo = new FileOutputStream(fileSaida);
        arquivoEscrita = new OutputStreamWriter(arquivo);
        //Recupera o Iterado da lista de tokens
        Iterator it = getTokens().iterator();

        //Verifica se a lista de tokens não está vazia
        if(!this.tokensIsVazio())
            arquivoEscrita.write("Lista de Tokens\n\n");

        //Varre a lista de tokens
        while(it.hasNext()){
            //Escreve no arquivo de saída correpondente
            arquivoEscrita.write(it.next().toString());
        }

        //Recupera o iterador para a lista de erros
        it = getErros().iterator();

        //Verifica se a lista de erros não está vazia
        if(!this.errosIsVazio())
            arquivoEscrita.write("\n\nLista de Erros\n\n");

        //Varre a lista de erros
        while (it.hasNext()){
            //Escreve no arquivo de saída correspondente
            arquivoEscrita.write(it.next().toString());
        }

        //Obriga que os dados que estão no buffer sejam escritos imediatamente
        arquivoEscrita.flush();
        arquivoEscrita.close();
    }
}
