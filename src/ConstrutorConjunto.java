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



        conjuntos.put("PVar", new ArrayList<String>(Arrays.asList("var")));
        conjuntos.put("PTipoVar", new ArrayList<String>(Arrays.asList("int", "boolean", "string", "real","Id")));
        conjuntos.put("PIdVar", new ArrayList<String>(Arrays.asList("Id")));
        conjuntos.put("PVar2", new ArrayList<String>(Arrays.asList(",",";","=","[")));
        conjuntos.put("PVar3", new ArrayList<String>(Arrays.asList("}","PTipoVar")));
        conjuntos.put("PVar4", new ArrayList<String>(Arrays.asList(",",";")));
        conjuntos.put("PVetor3", new ArrayList<String>(Arrays.asList("[")));
        conjuntos.put("PMatriz", new ArrayList<String>(Arrays.asList("PVetor3","PVar4")));
        conjuntos.put("PListaParametros", new ArrayList<String>(Arrays.asList("PListaParametros2")));
        conjuntos.put("PContListaParametros", new ArrayList<String>(Arrays.asList(",")));
        conjuntos.put("PListaParametros2", new ArrayList<String>(Arrays.asList("PIdentificador","Numero","String")));
        conjuntos.put("PIdentificador", new ArrayList<String>(Arrays.asList("PEscopo","Id")));
        conjuntos.put("PIdentificador2", new ArrayList<String>(Arrays.asList(".","PVetor")));
        conjuntos.put("PIdentificador3", new ArrayList<String>(Arrays.asList("PIdentificador2","(")));
        conjuntos.put("PIdentificador4", new ArrayList<String>(Arrays.asList(".")));
        conjuntos.put("PVetor", new ArrayList<String>(Arrays.asList("[","PIdentificador4")));
        conjuntos.put("PVetor2", new ArrayList<String>(Arrays.asList("[")));
        conjuntos.put("PCorpo2", new ArrayList<String>(Arrays.asList("PComandos")));



        conjuntos.put("PIndiceVetor", new ArrayList<String>(Arrays.asList("IntPos", "PIdentificador")));
        conjuntos.put("PEscopo", new ArrayList<String>(Arrays.asList("local", "global")));
        conjuntos.put("PIdentificadorSemFuncao", new ArrayList<String>(Arrays.asList("PEscopo", "Id")));
        conjuntos.put("PExpressaoAritmetica", new ArrayList<String>(Arrays.asList("PT", "PIdentificadorAritmetico", "++", "--")));
        conjuntos.put("PE2", new ArrayList<String>(Arrays.asList("+", "-")));
        conjuntos.put("PT", new ArrayList<String>(Arrays.asList("PF")));
        conjuntos.put("PT2", new ArrayList<String>(Arrays.asList("*", "/")));
        conjuntos.put("PF", new ArrayList<String>(Arrays.asList("(", "Numero")));
        conjuntos.put("PIdentificadorAritmetico", new ArrayList<String>(Arrays.asList("PEscopo", "Id")));
        conjuntos.put("PExpressaoLogicaRelacional", new ArrayList<String>(Arrays.asList("PExpressaoLR", "(")));
        conjuntos.put("PExpressaoLR", new ArrayList<String>(Arrays.asList("PArgumentoLR2", "PArgumentoLR3", "POperadorRelacional")));
        conjuntos.put("PExpressaoLR2", new ArrayList<String>(Arrays.asList("POperadorRelacional", "PExpressaoLR3")));
        conjuntos.put("PExpressaoLR3", new ArrayList<String>(Arrays.asList("POperadorLogico")));
        conjuntos.put("PArgumentoLR", new ArrayList<String>(Arrays.asList("PArgumentoLR2", "PArgumentoLR3")));
        conjuntos.put("PArgumentoLR2", new ArrayList<String>(Arrays.asList("!", "true", "false")));
        conjuntos.put("PArgumentoLR2_1", new ArrayList<String>(Arrays.asList("Id", "true", "false")));
        conjuntos.put("PArgumentoLR3", new ArrayList<String>(Arrays.asList("String", "PExpressaoAritmetica")));
        conjuntos.put("POperadorRelacional", new ArrayList<String>(Arrays.asList("!=", "==", "<", ">", "<=", ">=")));
        conjuntos.put("POperadorLogico", new ArrayList<String>(Arrays.asList("&&", "||")));
        conjuntos.put("PPrint", new ArrayList<String>(Arrays.asList("print")));
        conjuntos.put("PPrint1", new ArrayList<String>(Arrays.asList("String", "Numero", "PIdentificador")));
        conjuntos.put("PAuxPrint", new ArrayList<String>(Arrays.asList("PPrintFim", ",")));
        conjuntos.put("PPrintFim", new ArrayList<String>(Arrays.asList(")")));
        conjuntos.put("PRead", new ArrayList<String>(Arrays.asList("read")));
        conjuntos.put("PRead1", new ArrayList<String>(Arrays.asList("PIdentificadorSemFuncao"))); 
        conjuntos.put("PAuxRead", new ArrayList<String>(Arrays.asList(",", "PReadFim")));
        conjuntos.put("PReadFim", new ArrayList<String>(Arrays.asList(")")));
        conjuntos.put("PCorpo", new ArrayList<String>(Arrays.asList("}", "PVar")));
        conjuntos.put("PComandos", new ArrayList<String>(Arrays.asList("PCondicional", "PLaco", "PRead", "PPrint", "PComandosReturn", "PIdentificadorComandos")));
        conjuntos.put("PIdentificadorComandos", new ArrayList<String>(Arrays.asList("PIdentificadorSemFuncao"))); 
        conjuntos.put("PIdentificadorComandos2", new ArrayList<String>(Arrays.asList("=", "(")));
        conjuntos.put("PIdentificadorComandos2_1", new ArrayList<String>(Arrays.asList("PExpressaoAritmetica", "String", "Boolean")));
        conjuntos.put("PComandosReturn", new ArrayList<String>(Arrays.asList("return")));
        conjuntos.put("PCodigosRetornos", new ArrayList<String>(Arrays.asList(";", "PExpressaoAritmetica")));
        conjuntos.put("PStart", new ArrayList<String>(Arrays.asList("start")));
        


        conjuntos.put("SIndiceVetor", new ArrayList<String>(Arrays.asList("]")));
        conjuntos.put("SEscopo", new ArrayList<String>(Arrays.asList("Id")));
        conjuntos.put("SIdentificadorSemFuncao", new ArrayList<String>(Arrays.asList("PT2", "PE2", "SExpressaoAritmetica", "PAuxRead","SIdentificadorComandos")));
        conjuntos.put("SExpressaoAritmetica", new ArrayList<String>(Arrays.asList("SE2", "ST2", ")", "SArgumentoLR3", "SIdentificadorComandos2_1", ";")));
        conjuntos.put("SE2", new ArrayList<String>(Arrays.asList("SExpressaoAritmetica", "SExpressaoAritmetica2", "SIdentificadorAritmetico3"))); 
        conjuntos.put("ST", new ArrayList<String>(Arrays.asList("PE2", "SExpressaoAritmetica")));
        conjuntos.put("ST2", new ArrayList<String>(Arrays.asList("PE2", "SExpressaoAritmetica", "SExpressaoAritmetica2", "SIdentificadorAritmetico3", "ST"))); 
        conjuntos.put("SF", new ArrayList<String>(Arrays.asList("PT2", "ST")));
        conjuntos.put("SIdentificadorAritmetico", new ArrayList<String>(Arrays.asList("SExpressaoAritmetica")));
        conjuntos.put("SExpressaoLogicaRelacional", new ArrayList<String>(Arrays.asList("SExpressaoLR3", ")")));
        conjuntos.put("SExpressaoLR", new ArrayList<String>(Arrays.asList("SExpressaoLogicaRelacional", ")")));
        conjuntos.put("SExpressaoLR2", new ArrayList<String>(Arrays.asList("SExpressaoLR")));
        conjuntos.put("SExpressaoLR3", new ArrayList<String>(Arrays.asList("SExpressaoLogicaRelacional", "SExpressaoLR", "SExpressaoLR2")));
        conjuntos.put("SArgumentoLR", new ArrayList<String>(Arrays.asList("PExpressaoLR3", "SExpressaoLR2", "SExpressaoLR")));
        conjuntos.put("SArgumentoLR2", new ArrayList<String>(Arrays.asList("PExpressaoLR2", "SExpressaoLR"))); 
        conjuntos.put("SArgumentoLR2_1", new ArrayList<String>(Arrays.asList("SArgumentoLR2")));
        conjuntos.put("SArgumentoLR3", new ArrayList<String>(Arrays.asList("POperadorRelacional", "SArgumentoLR")));
        conjuntos.put("SOperadorRelacional", new ArrayList<String>(Arrays.asList("PArgumentoLR")));
        conjuntos.put("SOperadorLogico", new ArrayList<String>(Arrays.asList("PExpressaoLogicaRelacional")));
        conjuntos.put("SPrint", new ArrayList<String>(Arrays.asList("SComandos")));
        conjuntos.put("SPrint1", new ArrayList<String>(Arrays.asList("SPrint", "SAuxPrint")));
        conjuntos.put("SAuxPrint", new ArrayList<String>(Arrays.asList("SPrint1")));
        conjuntos.put("SPrintFim", new ArrayList<String>(Arrays.asList("SAuxPrint")));
        conjuntos.put("SRead", new ArrayList<String>(Arrays.asList("SComandos")));
        conjuntos.put("SRead1", new ArrayList<String>(Arrays.asList("SRead", "SAuxRead")));
        conjuntos.put("SAuxRead", new ArrayList<String>(Arrays.asList("SRead1")));
        conjuntos.put("SReadFim", new ArrayList<String>(Arrays.asList("SAuxRead")));
        conjuntos.put("SCorpo", new ArrayList<String>(Arrays.asList("}", "SF2")));
        conjuntos.put("SComandos", new ArrayList<String>(Arrays.asList("PCorpo2", "SCorpo2")));
        conjuntos.put("SIdentificadorComandos", new ArrayList<String>(Arrays.asList("SComandos")));
        conjuntos.put("SIdentificadorComandos2", new ArrayList<String>(Arrays.asList(";")));
        conjuntos.put("SIdentificadorComandos2_1", new ArrayList<String>(Arrays.asList("SIdentificadorComandos2")));
        conjuntos.put("SComandosReturn", new ArrayList<String>(Arrays.asList("SComandos")));
        conjuntos.put("SCodigosRetornos", new ArrayList<String>(Arrays.asList("SComandosReturn")));
        conjuntos.put("SStart", new ArrayList<String>(Arrays.asList("SInicio")));









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



        //Observação Verificar na gramática se trocou o corpo para corpo2 na condicional


        conjuntos.put("SVar", new ArrayList<String>(Arrays.asList("PGerarFuncaoeProcedure","start","PCorpo2", "SCorpo")));
        conjuntos.put("STipoVar", new ArrayList<String>(Arrays.asList("SVar", "SVar3")));
        conjuntos.put("SIdVar", new ArrayList<String>(Arrays.asList("STipoVar","SVar2","SVar4")));
        conjuntos.put("SVar2", new ArrayList<String>(Arrays.asList("SIdVar")));
        conjuntos.put("SVar3", new ArrayList<String>(Arrays.asList("SVar2","SVar4")));
        conjuntos.put("SVar4", new ArrayList<String>(Arrays.asList("SVar2","SMatriz")));
        conjuntos.put("SVetor3", new ArrayList<String>(Arrays.asList("PVar4")));
        conjuntos.put("SMatriz", new ArrayList<String>(Arrays.asList("SVetor3")));
        conjuntos.put("SListaParametros", new ArrayList<String>(Arrays.asList("SContListaParametros",")")));
        conjuntos.put("SContListaParametros", new ArrayList<String>(Arrays.asList("SListaParametros")));
        conjuntos.put("SListaParametros2", new ArrayList<String>(Arrays.asList("SContListaParametros","SListaParametros")));
        conjuntos.put("SIdentificador", new ArrayList<String>(Arrays.asList("SListaParametros2","SIndiceVetor","PAuxPrint")));
        conjuntos.put("SIdentificador2", new ArrayList<String>(Arrays.asList("SIdentificador","SIdentificador3","SIdentificadorSemFuncao","PExpressaoAritmetica2","SIdentificadorAritmetico3","SIdentificadorAritmetico")));
        conjuntos.put("SIdentificador3", new ArrayList<String>(Arrays.asList("SIdentificador")));
        conjuntos.put("SIdentificador4", new ArrayList<String>(Arrays.asList("SVetor")));
        conjuntos.put("SVetor", new ArrayList<String>(Arrays.asList("SIdentificador2","SIdentificador4")));
        conjuntos.put("SVetor2", new ArrayList<String>(Arrays.asList("PIdentificador4","SVetor")));
        conjuntos.put("Corpo2", new ArrayList<String>(Arrays.asList("{")));

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
        c.save("teste");
        System.out.println("--------------------------------------");
        Conjunto conj = new Conjunto("teste");
        conj.seguinte("Parametro");
        conj.primeiro("Procedimento");
    }
}
