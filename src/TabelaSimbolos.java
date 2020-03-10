import java.util.HashMap;


public class TabelaSimbolos {

    private HashMap<String, HashMap<String, Object>> tabela;

    public TabelaSimbolos(){
        tabela = new HashMap<String, HashMap<String, Object>>();
    }

    /***
     * Acessa informacao global
     * @param var
     * @param info
     * @return
     */
    public String getInfo(String var, String info){
        return (String)tabela.get(var).get(info);
    }

    /**
     * Acessa informacao local, dentro de uma funcao ou procedimento name
     * @param name
     * @param info
     * @return
     */
    public String getFuncProcInfo(String name, String info){
        HashMap local = (HashMap) tabela.get(name).get("valor");
        return (String)local.get(info);
    }

    /**
     * Insere informações globais
     * @param var
     * @param info
     * @param valor
     */
    public void setInfo(String var, String info, String valor){
        if(tabela.containsKey(var)){
            if(info.equals("tipo_identificador")){
                // se o tipo da variavel global eh funcao ou procedimento
                if(valor.equals("funcao") || valor.equals("procedimento")){
                    // em valor eh adicionado uma hashmap para valores local
                    tabela.get(var).put("valor", new HashMap<String, Object>());
                }
            }
            tabela.get(var).put(info, valor);

        }else{
            HashMap<String, Object> infos = new HashMap<String, Object>();
            infos.put(info, valor);
            tabela.put(var, infos);
        }
    }

    /**
     * Adiciona informacao local, dentro de uma funcao ou procedimento
     * @param name
     * @param info
     * @param valor
     */
    public void setFuncProc(String name, String info, String valor){
        HashMap local = (HashMap) tabela.get(name).get("valor");
        local.put(info, valor);
    }

}
