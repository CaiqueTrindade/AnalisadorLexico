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
        int estado_atual = 0;
        int linha_atual = 1;
        String lexema = "";

        while(caractere != null){

            caractere = this.obterCaractere();

            switch (estado_atual){

                case 0:

                    if ((int)caractere == 47){
                        lexema = lexema + caractere;
                        caractere = this.obterCaractere();

                        if ((int)caractere == 47)
                            estado_atual = 1;

                        else if ((int)caractere == 42)
                                estado_atual = 2;
                        else {
                            estado_atual = 3;
                        }
                        this.devolverCaractere(caractere);

                    }

                case 1:

                    if ((int) caractere != 10 )
                        lexema = lexema + caractere;
                    else{
                        this.inserirToken(new Token(lexema,linha_atual,10));
                        lexema = "";
                        estado_atual = 0;
                    }

                case 2:

                case 3:


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


