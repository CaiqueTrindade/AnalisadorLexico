import java.util.*;

public class AnalisadorSintatico {

    private List<Token> tokens; // Lista de tokens
    private Token token; // Token atual

    public AnalisadorSintatico(List<Token> tokens) {

        this.tokens = tokens;
        this.token = tokens.size()>0?tokens.get(0):null;
    }

    private void nextToken() {

        if (tokens.size() > 0) {
            token = tokens.get(0);
            tokens.remove(0);
        }
        else token = null;
    }

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
