import java.util.*;

public class AnalisadorSintatico {

    private List<Token> tokens; // Lista de tokens
    private Token token; // Token atual
    private Conjunto c;

    public AnalisadorSintatico(List<Token> tokens) {

        this.tokens = tokens;
        this.token = tokens.size()>0?tokens.get(0):null;
        this.c = new Conjunto("conjunto");
    }

    private void nextToken() {

        if (tokens.size() > 0) {
            token = tokens.get(0);
            tokens.remove(0);
        }
        else
            token = null;
    }

    public void extends(){
        if(token.getLexema() == "extends"){
            nextToken();
            if (token.getTipo() == 3){
                nextToken();
                if (token.getLexema() == "{"){
                    nextToken();
                } else{
                    erro(c.seguinte("extends") + ";");
                    if(token.getLexema() == ";"){
                        nextToken();
                        struct3();
                    }
                }
            }else{
                erro("{" + c.seguinte("extends") + ";");
                if(token.getLexema() == "{"){
                    nextToken();
                }else if(token.getLexema() == ";"){
                    nextToken();
                    struct3();
                }
            }
        } else if(token.getLexema() == "{"){
            nextToken();
        }else{
            erro("{" + c.seguinte("extends") + ";");
            if(token.getLexema() == "{"){
                nextToken();
            }else if(token.getLexema() == ";"){
                nextToken();
                struct3();
            }
        }

    }

    private void sincronizar(List<LinkedHashSet> tokens_sincronizacao) {

        boolean encontrou = false;
        boolean eof = (token == null);

        while (!encontrou && !eof) {
            Iterator it = tokens_sincronizacao.iterator();
            while(it.hasNext() && !encontrou) {
                LinkedHashSet conjunto = (LinkedHashSet) it.next();
                if (token.getTipo() == 3) {
                    if (conjunto.contains("Id"))
                        encontrou = true;
                }
                else if (token.getTipo() == 2 && Integer.parseInt(token.getLexema()) >= 0) {
                    if (conjunto.contains("IntPos"))
                        encontrou = true;
                }
                else if(token.getTipo() == 0 && token.getLexema().matches("^(true|false)$")) {
                    if (conjunto.contains("Boolean"))
                        encontrou = true;
                }
                else if(token.getTipo() == 11) {
                    if (conjunto.contains("String"))
                        encontrou = true;
                }
                else if(token.getTipo() == 1 || token.getTipo() == 2) {
                    if (conjunto.contains("Numero"))
                        encontrou = true;
                }
                else if (conjunto.contains(token.getLexema())) {
                    encontrou = true;
                }
            }
            if (!encontrou) {
                nextToken();
                if (token == null) eof = true;
            }
        }

    }

    public void executarAnalise() {

    }

    public void escreverEmArquivo() {

    }

}
