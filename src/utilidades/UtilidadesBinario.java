package utilidades;

import java.util.ArrayList;
import java.util.BitSet;

public abstract class UtilidadesBinario {
        
    //funciones x_To_y
    public static BitSet integerToBinary(int number, int longitud){
        //retorna bitset de tamaño longitud, con la representacion binaria de number
        //comportamiento inesperado con numeros negativos
        BitSet bitset = new BitSet(longitud);
        
        for(int i = longitud-1; i>=0; i--){
            int modulo = number % 2;
            if(modulo == 0)
                bitset.set(i, false);
            else
                bitset.set(i, true);
            
            number /= 2;
        }        
        return bitset;
    }
    
    public static BitSet octalTo8bits(int ascii){
        //octal usualmente son los bytes con los que trabajan los Byte buffer[]
        BitSet bitset = new BitSet(8);
        
        if(ascii <0) ascii += 256;
        
        for(int i = 7; i>=0; i--){
            int modulo = ascii % 2;
            if(modulo == 0)
                bitset.set(i, false);
            else
                bitset.set(i, true);
            
            ascii /= 2;
        }        
        return bitset;
    }
    
    public static byte binaryToAscii(BitSet bitset){
        
        int valor = 0;
        int potencia = 7;
        for(int i = 0; i<8; i++){
            if(bitset.get(i))
                valor += Math.pow(2, potencia);
            potencia --;
        }

        if(valor > 127) valor -= 256;
        
        return Byte.decode(String.valueOf(valor));
    }
    
    public static int binaryToInt(BitSet bitset, int size){
        int valor = 0;
        int potencia = size-1;
        for(int i = 0; i<size; i++){
            if(bitset.get(i))
                valor += Math.pow(2, potencia);
            potencia --;
        }
        return valor;
    }
    
    public static ArrayList<Boolean> asciiToBooleanArrayList(int ascii){
        BitSet bitset = octalTo8bits(ascii);
        ArrayList<Boolean> arr = new ArrayList<>();
        for(int i = 0; i<8; i++)
            arr.add(bitset.get(i));
        return arr;
    }
    
    public static BitSet bufferToBitset(byte[] buffer, int cantBytes){
        //transforma lo leido por buffer en bitset de tamaño cantBytes*8
        BitSet bitset = new BitSet(cantBytes*8);
        
        for(int i=0; i<cantBytes; i++)
            bitset = concatBits(bitset, octalTo8bits(buffer[i]), i*8, 8);
        
        return bitset;
    }
    
    public static BitSet booleanArrayToBitset(boolean boo[], int size){
        BitSet bitset = new BitSet(size);
        for (int i = 0; i < size; i++)
            bitset.set(i, boo[i]);      
        
        return bitset;
    }

    public static BitSet booleanArrayListToBitset(ArrayList<Boolean> arr){
        BitSet bitset = new BitSet(arr.size());
        for(int i = 0; i<arr.size(); i++)
            bitset.set(i, arr.get(i));
        
        return bitset;
    }
    
    //MANEJO DE BITS
    public static BitSet concatBits(BitSet receptorBits, BitSet bitsToAppend, int posInicial, int cantidad){
        /*
        EJEMPLO DE COMO FUNCIONA, sea:
        
        recptorBits: 0101       posicionDesde = 4
        bitsToAppend: 000111    cantidad = 6
        
        return -> receptorBits = 0101000111
        
        OBSERVACION 1: Si posición desde no es igual al tamaño de receptorBits, sobreescribira lo que se encuentre en el camino
            esto puede resultar util para copiar/duplicar/sobreescribir si es necesario
        
        OBSERVACION 2: no existe nullPointerException
        */
        
        for(int i = 0; i<cantidad; i++)
            receptorBits.set(posInicial + i, bitsToAppend.get(i));
        
        return receptorBits;
    }
    
    public static BitSet shiftLeft(BitSet bitset, int sizeBitset, int shift){
        
        for(int i = 0; i < sizeBitset; i++)
            bitset.set(i, bitset.get(shift+i));
        
        return bitset;
    }
        
    public static BitSet mirrorBitset(BitSet b, int size){
        BitSet mirror = new BitSet(size);
        for(int i = size; i>0; i--){
            mirror.set(i-1,b.get(size-i));
        }
        return mirror;
    }
        
    public static BitSet repartirBitsInfo(BitSet input, int vectorSize){
        /*
        Por ejemplo en Hamming 16, se trabaja con 11 bits de informacion
        pero esos 11 bits de informacion hay que distribuirlos en 16bits
        en las posiciones correspondientes

        en este caso:
            C C I C I I I C I I I I I I I P

        Entonces esta funcion retorna para este ejemplo
            0 0 I 0 I I I 0 I I I I I I I 0

        */
        BitSet output = new BitSet(vectorSize);
        int j = 0;
        for(int i=0; i<vectorSize; i++){
            if(!UtilidadesMatematicas.isPotenciaDeDos(i+1)){
                output.set(i, input.get(j));
                j++;
            }
        }
        return output;
    }
    
    public static BitSet setBitsControl(BitSet input, BitSet output, int cantBitsControl, int cantBitsInfo, boolean[][] matrizG){
        boolean[] aux = new boolean[cantBitsInfo];
        
        for(int i = 0; i<cantBitsControl; i++){
            boolean result = false;
            int pos = (int) Math.pow(2, i);
            for(int j = 0; j<cantBitsInfo; j++){
                aux[j] = input.get(j) && matrizG[j][i];
                result = result ^ aux[j];
            }
            output.set(pos-1, result);
        }
        return output;
    }
    
    public static BitSet setBitParidad(BitSet bitset, int hammingNumber){
        boolean result = false;
        
        for(int i=0;i<(hammingNumber-1);i++)
            result = result ^ bitset.get(i);
        
        bitset.set(hammingNumber-1, result);
        return bitset;
    }
    
    //MATRIZ GENERADORA G
    public static boolean[][] newMatrizGeneradora(int cantBitsInfo, int cantBitsControl, int bitSetSize){
	        
        //filas = cantBitsInfo
	//columnas = cantBitsControl
        boolean[][] matriz = new boolean[cantBitsInfo][cantBitsControl];
	int j;
	for(int i=0; i<cantBitsControl; i++){		
            j=0;
            for (int k=0; k<bitSetSize; k++){

                //si k+1 es potencia de dos no me interesa
                //porque sé que es un bit de control
                if(!UtilidadesMatematicas.isPotenciaDeDos(k+1)){

                    //si estoy aca soy
                    //el bit de información I_j
                    //me encuentro en la posición k+1

                    //si en la representación binaria de donde me encuentro
                    //en el digito d_i hay un 1
                        //matriz[j][i] = true
                    //else
                        //matriz[j][i] = false

                    matriz[j][i] = integerToBinary(k+1,cantBitsControl).get(cantBitsControl-i-1);
                    j++; //esto indica que ahora voy a buscar el siguiente bit de información
                }
            }
	}
	return matriz;
    }
    
    //MATRIZ DECODIFICADORA H
    public static boolean[][] newMatrizDecod(int cantBits, int cantBitsControl){
    //los numeros del 0 al hamming - 1 en binario, y en espejo
        boolean[][] matriz = new boolean[cantBits][cantBitsControl];
        BitSet numero = new BitSet(cantBitsControl);
        
        for(int i=0;i<cantBits;i++){
            numero = integerToBinary(i+1, cantBitsControl);
            for(int j=0;j<cantBitsControl;j++){
                matriz[i][j] = numero.get((cantBitsControl-1)-j);
            }
        }        
        return matriz;
    }

    //SINDROME
    public static int getSindrome(BitSet input, int hammingNumber, int cantBitsControl, boolean[][] matrizD){
        boolean[] sindrome = new boolean[cantBitsControl];
        boolean[] aux = new boolean[hammingNumber];
        
        for(int i=0; i<cantBitsControl;i++){
            boolean result = false;
            for(int j = 0; j<(hammingNumber-1); j++){
                aux[j] = input.get(j) && matrizD[j][i];
                result = result ^ aux[j];
            }
            sindrome[i] = result;
        }

        return binaryToInt(mirrorBitset(booleanArrayToBitset(sindrome, cantBitsControl), cantBitsControl),cantBitsControl);
    }
    
    //FUNCIONES DE DEBUG
    public static void printBitSet(BitSet bitset, int size){
        for(int i = 0; i<size; i++){
            if(bitset.get(i))
                System.out.print(" 1 ");
            else
                System.out.print(" 0 ");
        }
        System.out.println("");
    }
    
    public static void printBooleanMatriz(boolean[][] matriz, int filas, int columnas){
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if(matriz[i][j])
                    System.out.print("1");
                else
                    System.out.print("0");
                System.out.print(" ");
            }
            System.out.println("");
        }
    }
}
