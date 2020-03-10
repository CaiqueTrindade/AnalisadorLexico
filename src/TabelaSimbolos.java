import java.util.HashMap;

public class TabelaSimbolos {

    private HashMap<String, HashMap<String, Object>> tabela;

    public TabelaSimbolos(){
        tabela = new HashMap<String, HashMap<String, Object>>();
    }

    public String getInfo(String var, String info){
        return (String)tabela.get(var).get(info);
    }

    public String getFuncProcInfo(String name, String info){
        HashMap local = (HashMap) tabela.get(name).get("valor");
        return (String)local.get(info);
    }

    public void setInfo(String var, String info, String valor){
        if(tabela.containsKey(var)){
            if(info.equals("tipo_identificador")){
                tabela.get(var).put("valor", new HashMap<String, Object>());
            }
            tabela.get(var).put(info, valor);

        }else{
            HashMap<String, Object> infos = new HashMap<String, Object>();
            infos.put(info, valor);
            tabela.put(var, infos);
        }
    }

    public void setFuncProc(String name, String info, String valor){
        HashMap local = (HashMap) tabela.get(name).get("valor");
        local.put(info, valor);
    }

}
