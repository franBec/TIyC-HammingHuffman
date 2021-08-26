package hamming;

import utilidades.UtilidadesBinario;
import utilidades.UtilidadesArchivos;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;
import javax.swing.JOptionPane;
import utilidades.UtilidadesMatematicas;

public abstract class Hamming {
    
    /*
    CODIFICACION DE HAMMING: beginHamming -> doHamming -> writeInProtectedFile
    
    CONSTANTES:
    Sea Hamming n, donde n es la cantidad de bits que retorna la codificacion Hamming, entonces:
        Bits de control = log n / log 2
        Bits de información = n - bits de control - 1
        El bit sobrante es bit de paridad, el cual no se encuentra protegido
    */
    
    //Hamming 16
    private static final int BITS_CONTROL_H16 = 4;
    private static final int BITS_INFO_H16 = 11;
    //Hamming 2048
    private static final int BITS_CONTROL_H2048 = 11;
    private static final int BITS_INFO_H2048 = 2036;
    //Hamming 16384
    private static final int BITS_CONTROL_H16384 = 14;
    private static final int BITS_INFO_H16384 = 16369;
    
    //contadores
    public static int contadorErrores = 0;
    public static int contadorBloques = 0;
    public static int contadorBitsAgregados = 0;
    
    //flag doble error
    public static boolean dobleError = false;
    
    //redundancia es calculable
    //obtener tamaños de archivo original y protegido
    
    private static void resetContadores(){
        contadorErrores = 0;
        contadorBloques = 0;
        contadorBitsAgregados = 0;
        dobleError = false;
    }
    
    
    //bits de informacion -> hamming
    public static void beginHamming(File fileInput, File fileOutput, int hammingNumber, int cantidadErrores) throws FileNotFoundException, IOException {
        /*
        Esta función unicamente lee y envia bloques de bits de información
        en un formato en el que doHamming() puede trabajarlos
        */
        
        resetContadores();
        int bitsInfo = 0, bitsControl=0;
        
        switch(hammingNumber){
            case 16 -> {
                bitsInfo = BITS_INFO_H16;
                bitsControl = BITS_CONTROL_H16;
            }
            case 2048 -> {
                bitsInfo = BITS_INFO_H2048;
                bitsControl = BITS_CONTROL_H2048;
            }
            case 16384 -> {
                bitsInfo = BITS_INFO_H16384;
                bitsControl = BITS_CONTROL_H16384;
            }
        }
        
        File fileAuxiliar = UtilidadesArchivos.escrituraAuxiliar(fileInput);
        
        FileInputStream inputStream = new FileInputStream(fileAuxiliar);
        OutputStream outputStream = new FileOutputStream(fileOutput);
        byte[] buffer = new byte[1];
        
        boolean[][] matrizG = UtilidadesBinario.newMatrizGeneradora(bitsInfo, bitsControl, hammingNumber);
        BitSet principal = new BitSet(bitsInfo);
        int sizePrincipal = 0;
        
        BitSet auxiliar = new BitSet(8); //en un peor caso, pueden sobrar 8 bits que no entren en principal
        int sizeAuxiliar = 0;
        
        /*
        while hayan bytes para leer
            If tengo algo en el auxiliar
                principal = auxiliar
                auxiliar.clear()
            If pueden entrar 8 bits en el principal
                principal.append(buffer)
            Else
                axiliar = buffer
                principal.append(lo que le quepa de auxiliar)
                auxiliar.shift(lo que ya di a principal)
            If principal está listo ser usado por Hamming
                enviarselo a Hamming
                limpiar principal
        
        si ya leí todo pero me quedaron cosas por enviar
            enviarlas
        */

        while (inputStream.read(buffer) != -1) {
            if (sizeAuxiliar != 0) {
                principal = UtilidadesBinario.concatBits(principal, auxiliar, 0, sizeAuxiliar);
                sizePrincipal += sizeAuxiliar;
                
                auxiliar.clear();
                sizeAuxiliar = 0;
            }
            if (sizePrincipal + 8 <= bitsInfo) {
                principal = UtilidadesBinario.concatBits(principal, UtilidadesBinario.octalTo8bits(buffer[0]), sizePrincipal, 8);
                sizePrincipal += 8;
            }
            else {
                auxiliar = UtilidadesBinario.octalTo8bits(buffer[0]);
                sizeAuxiliar = 8;
                principal = UtilidadesBinario.concatBits(principal, auxiliar, sizePrincipal, bitsInfo - sizePrincipal);
                
                //descarto de auxiliar los bits que ya copié
                auxiliar = UtilidadesBinario.shiftLeft(auxiliar, 8, bitsInfo - sizePrincipal);
                
                //actualizo sizes
                sizeAuxiliar -= (bitsInfo - sizePrincipal);
                sizePrincipal = bitsInfo;
            }
            //si BitSet principal está lleno, puede enviarse al Hamming correspondiente
            if (sizePrincipal == bitsInfo) {
                doHamming(principal, outputStream, hammingNumber, cantidadErrores, matrizG);
                contadorBitsAgregados += (bitsControl+1);
                
                principal.clear();
                sizePrincipal = 0;
            }
        }
        if (sizeAuxiliar != 0 || sizePrincipal != 0) {
            /*
            me quedaron cosas sueltas que no pude juntar en un bloque
            pero tengo que enviarlas igual
            
            Posibles situaciones
                1. Terminé de leer, principal llegó al tamaño para ser enviado, pero me quedaron cosas en auxiliar
                    paso lo de auxiliar a principal, y lo mando
                2. Terminé de leer y principal no llegó al tamaño para ser enviado
                    envio principal como está
            */
            if (sizeAuxiliar != 0) 
                principal = UtilidadesBinario.concatBits(principal, auxiliar, 0, sizeAuxiliar);
            
            contadorBitsAgregados += (hammingNumber-(sizeAuxiliar + sizePrincipal));
            doHamming(principal, outputStream, hammingNumber, cantidadErrores, matrizG);
        }
        inputStream.close();
        outputStream.close();
        fileAuxiliar.delete();
    }
    
    //codificación de Hamming
    public static void doHamming(BitSet input, OutputStream outputStream, int hammingNumber, int cantidadErrores, boolean[][] matrizG) throws IOException{
        
        contadorBloques++;
        int bitsControl = 0 , bitsInfo = 0;
        switch(hammingNumber){
            case 16 -> {
                bitsControl = BITS_CONTROL_H16;
                bitsInfo = BITS_INFO_H16;
            }
            case 2048 -> {
                bitsControl = BITS_CONTROL_H2048;
                bitsInfo = BITS_INFO_H2048;
            }
            case 16384 -> {
                bitsControl = BITS_CONTROL_H16384;
                bitsInfo = BITS_INFO_H16384;
            }
        }
        
        BitSet output = new BitSet(hammingNumber);
        output = UtilidadesBinario.repartirBitsInfo(input, hammingNumber);
        output = UtilidadesBinario.setBitsControl(input, output, bitsControl, bitsInfo, matrizG);
        output = UtilidadesBinario.setBitParidad(output, hammingNumber);
        
        //INTRODUCCION DE UN UNICO ERROR
        if(cantidadErrores == 1){
            if(UtilidadesMatematicas.randomInt(10) == 0){
                //10% DE PROBABILIDAD
                output.flip(UtilidadesMatematicas.randomInt(hammingNumber-1));
                contadorErrores++;
            }
        }
        //INTRODUCCION DE UN DOBLE ERROR
        else if(cantidadErrores == 2){
          if(UtilidadesMatematicas.randomInt(20) == 0){
              //5% DE PROBABILIDAD
                output.flip(UtilidadesMatematicas.randomInt(hammingNumber-1));
                output.flip(UtilidadesMatematicas.randomInt(hammingNumber-1));
                contadorErrores += 2;
            }  
        }
        
        writeInProtectedFile(output, hammingNumber, outputStream);
    }
    
    //hamming -> archivo protegido
    public static void writeInProtectedFile(BitSet bitset, int hammingNumber, OutputStream outputStream) throws FileNotFoundException, IOException {       
        
        int bufferSize = hammingNumber / 8;
        byte[] buffer = new byte[bufferSize];
        
        for (int i = 0; i < bufferSize; i++)
            buffer[i] = UtilidadesBinario.binaryToAscii(bitset.get(i*8, (i+1)*8));
        
        outputStream.write(buffer);
    }

    //DECCODIFICACION DE HAMMING: beginUnHamming -> doUnHamming -> writeInDecodedFile
    
    //bits codificados -> unHamming
    public static void beginUnHamming(File fileInput, File fileOutput, int hammingNumber, boolean correccion) throws FileNotFoundException, IOException{
        int bufferSize = hammingNumber/8;
        int bitsInfo = 0,bitsControl = 0, i;
        switch(hammingNumber){
            case 16 -> {
                bitsControl = BITS_CONTROL_H16;
                bitsInfo = BITS_INFO_H16;
            }
            case 2048 -> {
                bitsControl = BITS_CONTROL_H2048;
                bitsInfo = BITS_INFO_H2048;
            }
            case 16384 -> {
                bitsControl = BITS_CONTROL_H16384;
                bitsInfo = BITS_INFO_H16384;
            }
        }
        boolean[][] matrizD = UtilidadesBinario.newMatrizDecod(hammingNumber-1, bitsControl);
        FileInputStream inputStream = new FileInputStream(fileInput);
        byte[] buffer = new byte[bufferSize];
        int contadorOutput, posInfo, contAux=0, contEscritor=0;
        BitSet info = new BitSet(bitsInfo);
        BitSet escritor = new BitSet(8);
        BitSet auxiliar = new BitSet(8);
        File auxiliarFile = new File("auxiliar2.txt");
        OutputStream outputStream = new FileOutputStream(auxiliarFile);

        while (inputStream.read(buffer) != -1){
            info = doUnHamming(UtilidadesBinario.bufferToBitset(buffer, bufferSize), hammingNumber, correccion, matrizD);
            if(info.get(bitsInfo+1)){
                JOptionPane.showMessageDialog(null, "Se ha encontrado un doble error que no puede ser corregido", "DOBLE ERROR DETECTADO", JOptionPane.ERROR_MESSAGE);
                break;
            }
            posInfo = 0;
            contadorOutput = bitsInfo;
            if(contAux!=0){
                for(i=0; i<contAux; i++){
                    escritor.set(i,auxiliar.get(i));
                }
                contEscritor=contAux;
                contAux=0;
                for(i=0;i<8-contEscritor;i++){
                    escritor.set(contEscritor+i,info.get(i));
                }
                posInfo+=i;
                contadorOutput-=i;
                //contEscritor=0;
                writeInDecodedFile(escritor, outputStream);
            }
            while(contadorOutput>=8){
                for(i=0; i<8; i++){
                    escritor.set(i,info.get(posInfo+i));
                }
                posInfo +=8;
                contadorOutput -=8;
                writeInDecodedFile(escritor, outputStream);
            }
            if(contadorOutput!=0){
                for(i=0; i<contadorOutput; i++){
                    auxiliar.set(i,info.get(posInfo+i));
                }
                contAux=contadorOutput;
            }
            
        }
        UtilidadesArchivos.copiaAuxiliar(auxiliarFile.getAbsolutePath(), fileOutput.getAbsolutePath(), correccion);
        inputStream.close();
        outputStream.close();
        auxiliarFile.delete();
    }
    
    //decodificación de Hamming
    public static BitSet doUnHamming(BitSet input, int hammingNumber, boolean correccion, boolean[][] matrizD){
        int bitsControl = 0 , bitsInfo = 0;
        switch(hammingNumber){
            case 16 -> {
                bitsControl = BITS_CONTROL_H16;
                bitsInfo = BITS_INFO_H16;
            }
            case 2048 -> {
                bitsControl = BITS_CONTROL_H2048;
                bitsInfo = BITS_INFO_H2048;
            }
            case 16384 -> {
                bitsControl = BITS_CONTROL_H16384;
                bitsInfo = BITS_INFO_H16384;
            }
        }
        BitSet output = new BitSet(bitsInfo);

        if(correccion){
            int sindrome = UtilidadesBinario.getSindrome(input, hammingNumber, bitsControl, matrizD);
            if(sindrome!=0)
                input.flip(sindrome-1);
        
            //comprobar si existe doble error
            boolean boo = false;
            for(int i = 0; i<hammingNumber; i++){
                boo = boo ^ input.get(i);
            }
            if(boo){
                dobleError = true;
            }
        }
        
        
        int contadorOutput = 0;
        for(int i = 2; i<hammingNumber; i++){
            if(!UtilidadesMatematicas.isPotenciaDeDos(i+1)){
                output.set(contadorOutput, input.get(i));
                contadorOutput++;
            }
        }
        return output;
    }

    //UnHamming -> archivo decodificado
    public static void writeInDecodedFile(BitSet outputBitset, OutputStream outputStream) throws FileNotFoundException, IOException{
        //escribe 8 bits = 1 byte
        byte[] buffer = new byte[1];
        buffer[0] = UtilidadesBinario.binaryToAscii(outputBitset);
        outputStream.write(buffer);
    }
    
}