package utilidades;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public abstract class UtilidadesArchivos {

    public static enum ExtensionHamming{HE, DE, DC} //extensiones de los archivos generados
    
    public static enum ExtensionHuffman{huf, dhu} //extensiones de los archivos generados
    
    public static File seleccionarArchivo(){
        JFileChooser fileChooser = new JFileChooser();
        File fileInput;

        if(fileChooser.showOpenDialog(fileChooser) == JFileChooser.APPROVE_OPTION){
            fileInput = fileChooser.getSelectedFile();
        }
        else{
            JOptionPane.showMessageDialog(null, "No se seleccionó archivo.","ERROR",JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return fileInput;
    }
    
    public static String archivoToString(String url) throws FileNotFoundException, IOException{
        String cadena;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(url), "utf-8"))) {
            while((cadena = in.readLine())!=null) {
                sb.append(cadena).append("\n");
            }
        }
        return sb.toString();
    }
        
    public static void copiaAuxiliar(String urlInput, String urlOutput, boolean correccion) throws FileNotFoundException, IOException{
        String primeraLinea = "";
        int cantCaracteresTotal = -1;
        
        if(correccion){
            //en la primer linea se encuentra la cantidad de caracteres que tiene el archivo original
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(urlInput), "utf-8"))) {
                primeraLinea = in.readLine();
            }
            //pasar de binario a int
            cantCaracteresTotal = Integer.parseInt(primeraLinea);
        }    
        
        FileInputStream inputStream = new FileInputStream(urlInput);
        OutputStream outputStream = new FileOutputStream(urlOutput);
        int caracterActual, cantCaracteresPrimeraLinea = 0,  cantCaracteresCopiados = 0;
        
        while((caracterActual = inputStream.read()) != -1){
            if(correccion && cantCaracteresPrimeraLinea <= primeraLinea.length()){
                //simplemente consumo la primer linea sin hacer nada, no me interesa escribirla en el output file
                cantCaracteresPrimeraLinea++;
            }
            else{
                outputStream.write(caracterActual);
                /*
                Explicación del if(caracterActual != 195)
                
                195 es un byte de control que (supongo) usa utf-8 para advertir que lo siguiente es fuera del ASCII estandar
                esto sucede por ejemplo con Á É Í Ó Ú á é í ó ú ñ Ñ y demas caracteres del español
                
                como es byte de control, no debo contarlo como caracter copiado
                caso contrario en el output file me quedare corto de caracteres
                */
                if(caracterActual != 195) cantCaracteresCopiados++;
                
                if(cantCaracteresCopiados==cantCaracteresTotal){
                    //si ya copié todo lo que necesitaba, el resto son 0s sin significado
                    break;
                }
            }
        }
        inputStream.close();
        outputStream.close();
    }
    
    public static File escrituraAuxiliar(File fileOriginal) throws FileNotFoundException, IOException{        
        //Entero a binario aqui
        String cantCaracteres = String.valueOf(UtilidadesArchivos.archivoToString(fileOriginal.getAbsolutePath()).length()-1);
        cantCaracteres = cantCaracteres.concat("\n");
        
        File fileOutput = new File("auxiliar.txt");
        OutputStream outputStream = new FileOutputStream(fileOutput);
        
        outputStream.write(cantCaracteres.getBytes());
        outputStream.write(archivoToString(fileOriginal.getAbsolutePath()).getBytes());
        outputStream.close();
        return fileOutput;
    }
    
    public static File crearArchivoHamming(File fileInput, ExtensionHamming extension, int huffmanNumber){
        String path = fileInput.getParent()+"\\"+generacionNombresHamming(fileInput, extension, huffmanNumber);
        File fileOutput = new File(path);
        try {
            if (!fileOutput.createNewFile()) {
                fileOutput.delete();
                fileOutput = new File(path);
                fileOutput.createNewFile();
            }
        } catch (IOException ex) {
            Logger.getLogger(UtilidadesArchivos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileOutput;
    }
    
    private static String generacionNombresHamming(File entrada, ExtensionHamming extension, int hammingNumber){
        String nombre = entrada.getName().split("\\.")[0];
        String extensionActual = entrada.getName().split("\\.")[1];
        if(extensionActual.equals("huf")){
            return nombre.concat(".fHE"+String.valueOf(hammingNumber));
        }
        else if(extensionActual.equals("fHE16") || extensionActual.equals("fHE2048") || extensionActual.equals("fHE16384")){
            return nombre.concat(".huf");
        }
        else{    
            switch(extension){
                case HE ->{return nombre.concat(".HE"+String.valueOf(hammingNumber));}
                case DE ->{return nombre.concat(".DE"+String.valueOf(hammingNumber));}
                case DC ->{return nombre.concat(".DC"+String.valueOf(hammingNumber));}
            }
        }
        return "";
    }

    public static LinkedHashMap lecturaHuffman(String url) throws IOException {
        LinkedHashMap<Integer, Integer> mapaCaracteres = new LinkedHashMap<>();
        FileInputStream inputStream = new FileInputStream(url);
        int caracter;
        while ((caracter = inputStream.read()) != -1) {
            if (mapaCaracteres.containsKey(caracter)) {
                mapaCaracteres.replace(caracter, mapaCaracteres.get(caracter) + 1);
            } else {
                mapaCaracteres.put(caracter, 1);
            }
        }
        inputStream.close();
        return mapaCaracteres;
    }

    public static File crearArchivoHuffman(File fileInput, ExtensionHuffman extension) {
        String path = fileInput.getParent() + "\\" + generacionNombresHuffman(fileInput, extension);
        File fileOutput = new File(path);
        try {
            if (!fileOutput.createNewFile()) {
                fileOutput.delete();
                fileOutput = new File(path);
                fileOutput.createNewFile();
            }
        } catch (IOException ex) {
            Logger.getLogger(UtilidadesArchivos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileOutput;
    }

    public static void codificacionHuffman(String urlInput, String urlOutput, LinkedHashMap<Integer, ArrayList<Boolean>> mapa) throws FileNotFoundException, IOException {
        FileInputStream inputStream = new FileInputStream(urlInput);
        OutputStream outputStream = new FileOutputStream(urlOutput, true);
        int caracterLeido;
        int caracterEscrito;
        ArrayList<Boolean> eof = new ArrayList<>();
        ArrayList<Boolean> escritora = new ArrayList<>();
        ArrayList<Boolean> auxMayor = new ArrayList<>();
        ArrayList<Boolean> auxiliar = new ArrayList<>();
        BitSet bits = new BitSet();
        while ((caracterLeido = inputStream.read()) != -1) {
            if (auxiliar.size() != 0) {
                escritora.addAll(auxiliar);
                auxiliar.clear();
            }
            if ((mapa.get(caracterLeido).size() + escritora.size()) <= 8) {
                escritora.addAll(mapa.get(caracterLeido));
            } else {
                auxiliar = UtilidadesHuffman.copiaSuperficial(mapa.get(caracterLeido));
                for (int i = escritora.size(); i < 8; i++) {
                    escritora.add(auxiliar.remove(0));
                }
            }
            if (escritora.size() == 8) {
                bits = UtilidadesBinario.booleanArrayListToBitset(escritora);
                caracterEscrito = UtilidadesBinario.binaryToAscii(bits);
                outputStream.write(caracterEscrito);
                bits.clear();
                escritora.clear();
            } else if (escritora.size() > 8) {
                int cociente = escritora.size() / 8;
                for (int j = 0; j < cociente; j++) {
                    for (int i = 0; i < 8; i++) {
                        auxMayor.add(escritora.remove(0));
                    }
                    bits = UtilidadesBinario.booleanArrayListToBitset(auxMayor);
                    caracterEscrito = UtilidadesBinario.binaryToAscii(bits);
                    outputStream.write(caracterEscrito);
                    auxMayor.clear();
                    bits.clear();
                }
            }
        }
        if (auxiliar.size() != 0 || escritora.size() != 0) {
            if (auxiliar.size() != 0) {
                escritora.addAll(auxiliar);
            }
            //Obtengo el eof
            eof.addAll(mapa.get(-1));
            if (eof.size() + escritora.size() <= 8) {
                escritora.addAll(eof);
                bits = UtilidadesBinario.booleanArrayListToBitset(escritora);
                caracterEscrito = UtilidadesBinario.binaryToAscii(bits);
                outputStream.write(caracterEscrito);
            } else {
                if (escritora.size() > 8) {
                    int cociente = escritora.size() / 8;
                    for (int j = 0; j < cociente; j++) {
                        for (int i = 0; i < 8; i++) {
                            auxMayor.add(escritora.remove(0));
                        }
                        bits = UtilidadesBinario.booleanArrayListToBitset(auxMayor);
                        caracterEscrito = UtilidadesBinario.binaryToAscii(bits);
                        outputStream.write(caracterEscrito);
                        auxMayor.clear();
                        bits.clear();
                    }
                }
                for (int i = escritora.size(); i < 8; i++) {
                    escritora.add(eof.remove(0));
                }
                bits = UtilidadesBinario.booleanArrayListToBitset(escritora);
                caracterEscrito = UtilidadesBinario.binaryToAscii(bits);
                outputStream.write(caracterEscrito);
                bits.clear();
                //eof le sobran cosas
                if (eof.size() > 8) {
                    int cociente = eof.size() / 8;
                    for (int j = 0; j < cociente; j++) {
                        for (int i = 0; i < 8; i++) {
                            auxMayor.add(eof.remove(0));
                        }
                        bits = UtilidadesBinario.booleanArrayListToBitset(auxMayor);
                        caracterEscrito = UtilidadesBinario.binaryToAscii(bits);
                        outputStream.write(caracterEscrito);
                        auxMayor.clear();
                        bits.clear();
                    }
                }
                if (eof.size() != 0) {
                    bits = UtilidadesBinario.booleanArrayListToBitset(eof);
                    caracterEscrito = UtilidadesBinario.binaryToAscii(bits);
                    outputStream.write(caracterEscrito);
                }
            }
        }
        outputStream.close();
        inputStream.close();
    }

    public static void tablaCodificacion(String urlOutput, LinkedHashMap<Integer, ArrayList<Boolean>> mapa) throws FileNotFoundException, IOException {
        OutputStream outputStream = new FileOutputStream(urlOutput);
        //Escribo la cantidad de caracteres codificados
        int cantCodificados = mapa.size();
        ArrayList<Boolean> aux = new ArrayList<>();
        BitSet bitS = UtilidadesBinario.integerToBinary(cantCodificados, 8);
        Byte buff = UtilidadesBinario.binaryToAscii(bitS);
        outputStream.write(buff);
        //Recorro el mapa buscando las claves y valores
        Iterator it = mapa.keySet().iterator();
        while (it.hasNext()) {
            Integer key = (Integer) it.next();
            //Escribo el caracter directo
            outputStream.write(key);
            //Escribo el tamaño de la codificacion
            aux = UtilidadesHuffman.copiaSuperficial(mapa.get(key));
            bitS = UtilidadesBinario.integerToBinary(aux.size(), 8);
            buff = UtilidadesBinario.binaryToAscii(bitS);
            outputStream.write(buff);
            //Segun el tamaño de la codificacion es la cantidad de bytes que escribo
            if (aux.size() % 8 != 0) {
                for (int i = 0; i < aux.size() % 8; i++) {
                    aux.add(Boolean.FALSE);
                }
            }
            bitS = UtilidadesBinario.booleanArrayListToBitset(aux);
            for (int i = 0; i < aux.size() / 8; i++) {
                buff = UtilidadesBinario.binaryToAscii(bitS.get(i * 8, (i + 1) * 8));
                outputStream.write(buff);
            }
        }
        outputStream.close();
    }

    public static boolean checkHammingExtension(String fileName){
        String extension = fileName.split("\\.")[1];
        return "HE16".equals(extension) || "HE2048".equals(extension) || "HE16384".equals(extension) || "fHE16".equals(extension) || "fHE2048".equals(extension) || "fHE16384".equals(extension);
    }
    
    public static boolean checkHuffmanExtension(String fileName){
        String extension = fileName.split("\\.")[1];
        return "huf".equals(extension);
    }
    
    private static String generacionNombresHuffman(File entrada, ExtensionHuffman extension) {
        String nombre = entrada.getName().split("\\.")[0];
        switch (extension) {
            case huf -> {
                return nombre.concat(".huf");
            }
            case dhu -> {
                return nombre.concat(".dhu");
            }
        }
        return "";
    }
    
}
