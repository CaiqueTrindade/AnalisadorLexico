

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

//import sun.awt.image.ImageWatched;


public class Conjunto {
    private Map<String, LinkedHashSet<String>> conjuntos = new HashMap<String, LinkedHashSet<String>>();

    public Conjunto(String nomeArq){
        conjuntos = open(nomeArq);
    }
    public ArrayList<String> primeiro(String nTerminal) {
        nTerminal = "P".concat(nTerminal);
        //System.out.println(nTerminal);
        LinkedHashSet<String> conjunto = conjuntos.get(nTerminal);
        //System.out.println(conjunto);
        return new ArrayList<String>(conjunto);
    }

    public ArrayList<String> seguinte(String nTerminal) {
        nTerminal = "S".concat(nTerminal);
        //System.out.println(nTerminal);
        LinkedHashSet<String> conjunto = conjuntos.get(nTerminal);
        //System.out.println(conjunto);
        return new ArrayList<>(conjunto);
    }

    public Map<String, LinkedHashSet<String>> open(String nomeArq){
        Map<String, LinkedHashSet<String>> map = new HashMap<String, LinkedHashSet<String>>();
        try {
            File arq = new File(nomeArq);
            if (arq.exists()) {
                ObjectInputStream objInput = new ObjectInputStream(new FileInputStream(arq));
                map = (HashMap<String, LinkedHashSet<String>>)objInput.readObject();
                objInput.close();
            }
        } catch(IOException erro1) {
            System.out.printf("Erro: %s", erro1.getMessage());
        } catch(ClassNotFoundException erro2) {
            System.out.printf("Erro: %s", erro2.getMessage());
        }

        return map;
    }

}