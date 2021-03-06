import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public enum GerenciadorTokens {
    INSTANCE;
    List<Token> tokens = new ArrayList<>();
    List<Erro> erros = new ArrayList<>();

    GerenciadorTokens(){}


    public static GerenciadorTokens getInstance(){

        return INSTANCE;
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

    public void limparLista(){

        this.tokens.clear();
        this.erros.clear();

    }

    public boolean tokensIsVazio(){

        return this.tokens.isEmpty();

    }

    public boolean errosIsVazio(){

        return this.erros.isEmpty();

    }


    public void escreverEmArquivo(String caminho){

        OutputStream arquivo;
        OutputStreamWriter arquivoEscrita;

        try{

            arquivo = new FileOutputStream(caminho);
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
        catch (IOException e){

        }


    }






}
