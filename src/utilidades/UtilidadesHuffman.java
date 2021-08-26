package utilidades;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public abstract class UtilidadesHuffman {

    //Tratamiento de estructuras auxiliares de huffman
    public static LinkedHashMap<Integer, ArrayList<Boolean>> castTabla(LinkedHashMap<Integer, String> tablaOriginal) {
        LinkedHashMap<Integer, ArrayList<Boolean>> tablaCasteada = new LinkedHashMap<>();

        for (Integer key : tablaOriginal.keySet()) {
            String valor = tablaOriginal.get(key);
            ArrayList<Boolean> arr = new ArrayList<>();

            for (int i = 0; i < valor.length(); i++) {
                arr.add(valor.charAt(i) == '1');
            }

            tablaCasteada.put(key, arr);
        }
        return tablaCasteada;
    }
     
    public static ArrayList<Boolean> copiaSuperficial(ArrayList<Boolean> arr) {
        ArrayList<Boolean> retorno = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            retorno.add(arr.get(i));
        }
        return retorno;
    }

    public static ArrayList<Boolean> subArray(ArrayList<Boolean> arrIn, int min, int max) {
        ArrayList<Boolean> arrOut = new ArrayList<>();
        for (int i = min; i < max; i++) {
            arrOut.add(arrIn.get(i));
        }
        return arrOut;
    }

    public static String arrayToString(ArrayList<Boolean> arr) {
        StringBuilder sb = new StringBuilder();
        for (Boolean boo : arr) {
            if (boo) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }
        return sb.toString();
    }
    
    //FUNCIONES DE DEBUG
    public static void printMapaII(LinkedHashMap<Integer, Integer> mapa) {
        mapa.entrySet().forEach((entry) -> {
            System.out.printf("Clave : %s - Valor: %s %n", entry.getKey(), entry.getValue());
        });
    }

    public static void printMapaIS(LinkedHashMap<Integer, String> mapa) {
        Iterator it = mapa.keySet().iterator();

        while (it.hasNext()) {
            Integer key = (Integer) it.next();
            System.out.printf("Clave: %d -> Valor: %s \n", key, mapa.get(key));
        }
    }

    public static void printMapaBI(LinkedHashMap<ArrayList<Boolean>, Integer> mapa) {
        Iterator it = mapa.keySet().iterator();
        while (it.hasNext()) {
            StringBuilder clave = new StringBuilder();
            ArrayList<Boolean> key = (ArrayList<Boolean>) it.next();
            for (Boolean boo : key) {
                if (boo) {
                    clave.append("1");
                } else {
                    clave.append("0");
                }
            }
            System.out.printf("Clave: %s -> Valor: %d \n", clave.toString(), mapa.get(key));

        }
    }

    public static void printMapaIB(LinkedHashMap<Integer, ArrayList<Boolean>> mapa) {
        Iterator it = mapa.keySet().iterator();
        while (it.hasNext()) {
            StringBuilder clave = new StringBuilder();
            int key = (int) it.next();
            ArrayList<Boolean> arr = mapa.get(key);
            for (Boolean boo : arr) {
                if (boo) {
                    clave.append("1");
                } else {
                    clave.append("0");
                }
            }
            System.out.printf("Clave: %d -> Valor: %s \n", key, clave.toString());

        }
    }

}
