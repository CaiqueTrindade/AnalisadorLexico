import java.io.*;

public class Principal {
    public static void main(String[] args) {

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
    }
}
