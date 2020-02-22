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
        conjuntos.put("PPara2", new ArrayList<String>(Arrays.asList("[", "E")));
        conjuntos.put("PPara3", new ArrayList<String>(Arrays.asList("[", "E")));
        conjuntos.put("PF2", new ArrayList<String>(Arrays.asList("{")));
        conjuntos.put("PConst", new ArrayList<String>(Arrays.asList("const")));
        conjuntos.put("PTipoConst", new ArrayList<String>(Arrays.asList("PTipo")));
        conjuntos.put("PIndiceVetor", new ArrayList<String>(Arrays.asList("int pos", "PIdentificador")));
        conjuntos.put("PEscopo", new ArrayList<String>(Arrays.asList("local", "global")));
        conjuntos.put("PIdentificadorSemFuncao", new ArrayList<String>(Arrays.asList("PEscopo", "Identificador")));
        conjuntos.put("PExpressaoAritmetica", new ArrayList<String>(Arrays.asList("PT", "PIdentificadorAritmetico", "++", "--")));


        conjuntos.put("SProcedimento", new ArrayList<String>(Arrays.asList("SGeraFuncaoeProcedure","PGeraFuncaoeProcedure")));
        conjuntos.put("SParametro", new ArrayList<String>(Arrays.asList("PTipo")));
        conjuntos.put("SPara1", new ArrayList<String>(Arrays.asList("SParametro")));
        conjuntos.put("SPara2", new ArrayList<String>(Arrays.asList("PPara1")));
        conjuntos.put("SPara3", new ArrayList<String>(Arrays.asList("SPara2")));
        conjuntos.put("SF2", new ArrayList<String>(Arrays.asList("SPara1")));
        conjuntos.put("SConst", new ArrayList<String>(Arrays.asList("PStruct", "PVar", "PGeraFuncaoeProcedure", "PStart")));
        conjuntos.put("STipoConst", new ArrayList<String>(Arrays.asList("SConst", "SConst3")));

    }

    public Map<String, ArrayList<String>> getConjuntos(){
        return conjuntos;
    }


    public void conjuntoBuilder(String nTerminal){
        System.out.println(nTerminal);
        ArrayList<String> conjunto = conjuntos.get(nTerminal);
        System.out.println(conjunto);
        conjunto = nTerminalToTerminal(conjunto);
        LinkedHashSet conjuntoSemRepeticao = new LinkedHashSet<>(conjunto);
        System.out.println(conjuntoSemRepeticao);
        conjuntoFinal.put(nTerminal, conjuntoSemRepeticao);

    }

    public ArrayList<String> nTerminalToTerminal(ArrayList<String> conjunto){
        ArrayList<String> resultado = new ArrayList<String>();
        for (String entrada : conjunto) {
            if(conjuntos.containsKey(entrada)){
                resultado.addAll(nTerminalToTerminal(conjuntos.get(entrada)));
            }else{
                resultado.add(entrada);
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

        Conjunto conj = new Conjunto("teste");
        conj.seguinte("Parametro");
        conj.primeiro("Procedimento");
    }
}
