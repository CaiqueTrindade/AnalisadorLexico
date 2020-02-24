import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class ConstrutorConjunto {
    private Map<String,ArrayList<String>> conjuntos = new HashMap<String, ArrayList<String>>();
    private Map<String,LinkedHashSet<String>> conjuntoFinal = new HashMap<String, LinkedHashSet<String>>();

    public ConstrutorConjunto(){
        conjuntos.put("PProcedimento", new ArrayList<String>(Arrays.asList("procedure")));
        conjuntos.put("PParametro", new ArrayList<String>(Arrays.asList("PTipo")));
        conjuntos.put("PPara1", new ArrayList<String>(Arrays.asList(",", ")")));
        conjuntos.put("PPara2", new ArrayList<String>(Arrays.asList("[")));
        conjuntos.put("PPara3", new ArrayList<String>(Arrays.asList("[")));
        conjuntos.put("PF2", new ArrayList<String>(Arrays.asList("{")));
        conjuntos.put("PConst", new ArrayList<String>(Arrays.asList("const")));
        conjuntos.put("PTipoConst", new ArrayList<String>(Arrays.asList("PTipo")));
        conjuntos.put("PIndiceVetor", new ArrayList<String>(Arrays.asList("IntPos", "PIdentificador")));
        conjuntos.put("PEscopo", new ArrayList<String>(Arrays.asList("local", "global")));
        conjuntos.put("PIdentificadorSemFuncao", new ArrayList<String>(Arrays.asList("PEscopo", "Id")));
        conjuntos.put("PExpressaoAritmetica", new ArrayList<String>(Arrays.asList("PT", "PIdentificadorAritmetico", "++", "--")));
        conjuntos.put("PE2", new ArrayList<String>(Arrays.asList("+", "-")));
        conjuntos.put("PT", new ArrayList<String>(Arrays.asList("PF")));
        conjuntos.put("PT2", new ArrayList<String>(Arrays.asList("*",  "/")));
        conjuntos.put("PF", new ArrayList<String>(Arrays.asList("(", "Numero")));
        conjuntos.put("PIdentificadorAritmetico", new ArrayList<String>(Arrays.asList("PEscopo", "Id")));
        conjuntos.put("PStruct3", new ArrayList<String>(Arrays.asList("}", "PTipoStruct")));
        conjuntos.put("PValor", new ArrayList<String>(Arrays.asList("Numero", "String", "Boolean", "Id")));
        conjuntos.put("PValorVetor", new ArrayList<String>(Arrays.asList("IntPos", "Id")));
        conjuntos.put("PTipo", new ArrayList<String>(Arrays.asList("int", "boolean", "string", "real", "Id")));
        conjuntos.put("PInicio", new ArrayList<String>(Arrays.asList("PConst", "PStruct", "PVar", "PGeraFuncaoeProcedure", "PStart")));
        conjuntos.put("PCondicional", new ArrayList<String>(Arrays.asList("if")));
        conjuntos.put("PCondEnd", new ArrayList<String>(Arrays.asList("else")));
        conjuntos.put("PLaco", new ArrayList<String>(Arrays.asList("while")));
        conjuntos.put("PGeraFuncaoeProcedure", new ArrayList<String>(Arrays.asList("PFuncao", "PProcedimento")));


        conjuntos.put("SProcedimento", new ArrayList<String>(Arrays.asList("SGeraFuncaoeProcedure","PGeraFuncaoeProcedure")));
        conjuntos.put("SParametro", new ArrayList<String>(Arrays.asList("PTipo")));
        conjuntos.put("SPara1", new ArrayList<String>(Arrays.asList("SParametro")));
        conjuntos.put("SPara2", new ArrayList<String>(Arrays.asList("PPara1")));
        conjuntos.put("SPara3", new ArrayList<String>(Arrays.asList("SPara2")));
        conjuntos.put("SF2", new ArrayList<String>(Arrays.asList("SPara1")));
        conjuntos.put("SConst", new ArrayList<String>(Arrays.asList("PStruct", "PVar", "PGeraFuncaoeProcedure", "PStart")));
        conjuntos.put("STipoConst", new ArrayList<String>(Arrays.asList("SConst", "SConst3")));
        conjuntos.put("SIndiceVetor", new ArrayList<String>(Arrays.asList("]")));
        conjuntos.put("SEscopo", new ArrayList<String>(Arrays.asList("Id")));
        conjuntos.put("SIdentificadorSemFuncao", new ArrayList<String>(Arrays.asList("PT2", "PE2", "SExpressaoAritmetica", "PAuxRead", "SIdentificadorComandos")));
        conjuntos.put("SExpressaoAritmetica", new ArrayList<String>(Arrays.asList("SE2", "ST2", ")", "SArgumentoLR3", "SIdentificadorComandos2_1", ";")));
        conjuntos.put("SE2", new ArrayList<String>(Arrays.asList("SExpressaoAritmetica", "SExpressaoAritmetica2", "SIdentificadorAritmetico3")));
        conjuntos.put("ST", new ArrayList<String>(Arrays.asList("SExpressaoAritmetica", "PE2")));
        conjuntos.put("ST2", new ArrayList<String>(Arrays.asList("PE2", "SExpressaoAritmetica", "SExpressaoAritmetica2", "ST", "SIdentificadorAritmetico3")));
        conjuntos.put("SF", new ArrayList<String>(Arrays.asList("PT2", "ST")));
        conjuntos.put("SIdentificadorAritmetico", new ArrayList<String>(Arrays.asList("SExpressaoAritmetica")));
        conjuntos.put("SStruct3", new ArrayList<String>(Arrays.asList("SStruct2")));
        conjuntos.put("SValor", new ArrayList<String>(Arrays.asList("PVar4","PConst2")));
        conjuntos.put("SValorVetor", new ArrayList<String>(Arrays.asList("]")));
        conjuntos.put("STipo", new ArrayList<String>(Arrays.asList("Id", "PIdConst", "PIdStruct", "PIdVar")));
        conjuntos.put("SInicio", new ArrayList<String>(Arrays.asList("$")));
        conjuntos.put("SCondicional", new ArrayList<String>(Arrays.asList("SComandos")));
        conjuntos.put("SCondEnd", new ArrayList<String>(Arrays.asList("SCondicional")));
        conjuntos.put("SLaco", new ArrayList<String>(Arrays.asList("SComandos")));
        conjuntos.put("SGeraFuncaoeProcedure", new ArrayList<String>(Arrays.asList("PStart")));

    }

    public Map<String, ArrayList<String>> getConjuntos(){
        return conjuntos;
    }


    public void conjuntoBuilder(String nTerminal){
        ArrayList<String> chamadas = new ArrayList<String>();
        chamadas.add(nTerminal);
        ArrayList<String> conjunto = conjuntos.get(nTerminal);
        conjunto = nTerminalToTerminal(conjunto, chamadas);
        LinkedHashSet conjuntoSemRepeticao = new LinkedHashSet<>(conjunto);
        if(checkNTerminal(conjuntoSemRepeticao)){
            System.out.println(nTerminal);
            System.out.println(conjuntoSemRepeticao);
        }
        conjuntoFinal.put(nTerminal, conjuntoSemRepeticao);

    }

    public boolean checkNTerminal(LinkedHashSet<String> conjunto){
        for (String value: conjunto) {
            if (Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1))){

                return true;
            }
        }
        return false;
    }

    public ArrayList<String> nTerminalToTerminal(ArrayList<String> conjunto, ArrayList<String> chamadas){
        ArrayList<String> resultado = new ArrayList<String>();
        for (String entrada : conjunto) {
            if(!chamadas.contains(entrada)) {
                if (conjuntos.containsKey(entrada)) {
                    chamadas.add(entrada);
                    resultado.addAll(nTerminalToTerminal(conjuntos.get(entrada), chamadas));
                } else {
                    resultado.add(entrada);
                }

            }

        }
        return resultado;
    }

    public void save(String nomeArq) {
        File arq = new File(nomeArq);
        try {
            arq.delete();
            arq.createNewFile();

            ObjectOutputStream objOutput = new ObjectOutputStream(new FileOutputStream(arq));
            objOutput.writeObject(conjuntoFinal);
            objOutput.close();

        } catch(IOException erro) {
            System.out.printf("Erro: %s", erro.getMessage());
        }
    }

    public static void main(String[] args) {
        ConstrutorConjunto c = new ConstrutorConjunto();
        for( Map.Entry<String, ArrayList<String>> entry : c.getConjuntos().entrySet()) {
            String key = entry.getKey();
            c.conjuntoBuilder(key);

        }
        c.save("conjunto");
        System.out.println("--------------------------------------");
        Conjunto conj = new Conjunto("conjunto");
        conj.seguinte("Parametro");
        conj.primeiro("Procedimento");
    }
}
