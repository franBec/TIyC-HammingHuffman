package huffman;

import utilidades.UtilidadesHuffman;
import utilidades.UtilidadesBinario;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import utilidades.UtilidadesArchivos;

public abstract class Huffman {

    private static final int CARACTER_INVALIDO = Integer.MIN_VALUE;

    public static HuffmanNode procesoReduccion(LinkedHashMap<Integer, Integer> mapa) {
        //priority queue que se comporta como parva de minimos
        PriorityQueue<HuffmanNode> q = new PriorityQueue<>(mapa.size(), new HuffmanComparator());

        //lleno la parva
        for (Integer key : mapa.keySet()) {
            Integer valor = mapa.get(key);
            HuffmanNode nodo = new HuffmanNode(key, valor);
            q.add(nodo);
        }

        HuffmanNode raiz = null; //raiz del arbol de Huffman a construir

        //mientras en la parva me quede mas de una cosa
        while (q.size() > 1) {
            //x = primer nodo con menos frecuencia
            //y = segundo nodo con menos frecuencia

            HuffmanNode x = q.peek();
            q.poll();
            HuffmanNode y = q.peek();
            q.poll();

            //f = nodo con la suma de las frecuencias de x y
            //tiene como hijo izquierdo a x
            //tiene como hijo derecho a y
            HuffmanNode f = new HuffmanNode(CARACTER_INVALIDO, x.getFrecuencia() + y.getFrecuencia(), x, y);

            //f es momentaneamente raiz
            raiz = f;

            //agrego f al heap
            q.add(f);
        }

        //retorno raiz del arbol de huffman resultante del proceso de reduccion
        return raiz;
    }

    public static LinkedHashMap<Integer, String> procesoAsignacion(HuffmanNode root, String s, LinkedHashMap<Integer, String> tablaCodificacion) {

        //caso base: soy un caracter y agrego a la tabla mi codificación de Huffman
        if (root.getLeft() == null && root.getRight() == null && root.getCaracter() != CARACTER_INVALIDO) {
            tablaCodificacion.put(root.getCaracter(), s);
            return tablaCodificacion;
        }

        //caso recursivo
        //si me voy a la izquierda agrego 0
        //si me voy a la derecha agrego 1
        tablaCodificacion = procesoAsignacion(root.getLeft(), s + "0", tablaCodificacion);
        tablaCodificacion = procesoAsignacion(root.getRight(), s + "1", tablaCodificacion);

        return tablaCodificacion;
    }

    public static void beginHuffman(String urlInput, String urlOutput) throws IOException {
        LinkedHashMap mapaLectura = UtilidadesArchivos.lecturaHuffman(urlInput);
        mapaLectura.put(-1, 0);
        HuffmanNode raiz = procesoReduccion(mapaLectura);
        LinkedHashMap<Integer, String> mapaHuffAux = new LinkedHashMap<>();
        mapaHuffAux = Huffman.procesoAsignacion(raiz, "", mapaHuffAux);
        LinkedHashMap<Integer, ArrayList<Boolean>> mapaHuff = UtilidadesHuffman.castTabla(mapaHuffAux);
        UtilidadesArchivos.tablaCodificacion(urlOutput, mapaHuff);
        UtilidadesArchivos.codificacionHuffman(urlInput, urlOutput, mapaHuff);
    }

    public static void decodeHuffman(String urlInput, String urlOutput) throws FileNotFoundException, IOException {
        FileInputStream inputStream = new FileInputStream(urlInput);

        int caracterLeido, caracterEscrito=0, cantPares, caracterValor, sizeCodificacion, techo;
        BitSet bits = new BitSet();
        ArrayList<Boolean> arr = new ArrayList<>();
        ArrayList<Boolean> arrAux = new ArrayList<>();
        LinkedHashMap<ArrayList<Boolean>, Integer> mapa = new LinkedHashMap<>();
        //Leo la cantidad de pares clave/valor que forman la tabla
        cantPares = inputStream.read();
        //Recorro la tabla de Huffman
        for (int i = 0; i < cantPares; i++) {
            //Leo directo el caracter
            caracterValor = inputStream.read();
            //Leo el tamaño de la codificacion en bits
            sizeCodificacion = inputStream.read();
            //Leo la cantidad necesaria de bytes
            techo = (int) Math.ceil((double) sizeCodificacion / (double) 8);
            for (int j = 0; j < techo; j++) {
                caracterLeido = inputStream.read();
                arr.addAll(UtilidadesBinario.asciiToBooleanArrayList(caracterLeido));
            }
            mapa.put(UtilidadesHuffman.subArray(arr, 0, sizeCodificacion), caracterValor);
            arr.clear();
        }
        //Decodificacion luego de generar la tabla de decodificacion
        OutputStream outputStream = new FileOutputStream(urlOutput);
        while ((caracterLeido = inputStream.read()) != -1) {
            arr.addAll(UtilidadesBinario.asciiToBooleanArrayList(caracterLeido));
            while (arr.size() > 0) {
                arrAux.add(arr.remove(0));
                if (mapa.containsKey(arrAux) && (mapa.get(arrAux) != 255)) {
                    caracterEscrito = mapa.get(arrAux);
                    outputStream.write(caracterEscrito);
                    arrAux.clear();
                }
                else if(mapa.containsKey(arrAux) && (mapa.get(arrAux) == 255)){
                    break;
                }
            }
        }
        if (arr.size() != 0) {
            while (arr.size() > 0) {
                arrAux.add(arr.remove(0));
                if (mapa.containsKey(arrAux) && (mapa.get(arrAux) != 255)) {
                    caracterEscrito = mapa.get(arrAux);
                    outputStream.write(caracterEscrito);
                }
                else if(mapa.containsKey(arrAux) && (mapa.get(arrAux) == 255)){
                    break;
                }
            }
        }
        outputStream.close();
    }

}
