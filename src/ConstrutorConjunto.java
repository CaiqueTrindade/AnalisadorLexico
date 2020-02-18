import java.util.*;

public class ConstrutorConjunto {
    private Map<String,ArrayList<String>> conjuntos = new HashMap<String, ArrayList<String>>();

    public ConstrutorConjunto(){
        conjuntos.put("PStruct", new ArrayList<String>(Arrays.asList("typedef", "E")));
        conjuntos.put("SStruct", new ArrayList<String>(Arrays.asList("PVar","PGeraFuncaoeProcedure","PStart")));
        conjuntos.put("PVar", new ArrayList<String>(Arrays.asList("one", "two")));
        conjuntos.put("PGeraFuncaoeProcedure", new ArrayList<String>(Arrays.asList("three", "four")));
        conjuntos.put("PStart", new ArrayList<String>(Arrays.asList("five", "PStruct")));

        conjuntos.put("PAlgo", new ArrayList<String>(Arrays.asList("SVar","PGeraFuncaoeProcedure","SStart")));
        conjuntos.put("SVar", new ArrayList<String>(Arrays.asList("one", "two")));
        conjuntos.put("SStart", new ArrayList<String>(Arrays.asList("five", "SStruct")));
    }

    public void primeiro(String nTerminal){
        nTerminal = "P".concat(nTerminal);
        System.out.println(nTerminal);
        ArrayList<String> conjunto = conjuntos.get(nTerminal);
        System.out.println(conjunto);
        conjunto = nTerminalToTerminal(conjunto);
        LinkedHashSet conjuntoSemRepeticao = new LinkedHashSet<>(conjunto);
        System.out.println(conjuntoSemRepeticao);
    }

    public void seguinte(String nTerminal){
        nTerminal = "S".concat(nTerminal);
        System.out.println(nTerminal);
        ArrayList<String> conjunto = conjuntos.get(nTerminal);
        System.out.println(conjunto);
        conjunto = nTerminalToTerminal(conjunto);
        LinkedHashSet conjuntoSemRepeticao = new LinkedHashSet<>(conjunto);
        System.out.println(conjuntoSemRepeticao);
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
    public static void main(String[] args) {
        ConstrutorConjunto c = new ConstrutorConjunto();
        c.seguinte("Struct");
        c.primeiro("Algo");
    }
}
