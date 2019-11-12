import java.util.ArrayList;
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

    public void inserirToken(Erro erro){

        this.erros.add(erro);
    }

    public Token recuperaToken(int indice){

        return this.tokens.get(indice);
    }

    public void limparLista(){

        tokens.clear();
    }

    public void escreverEmArquivo(String caminho){

    }






}
