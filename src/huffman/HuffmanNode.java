package huffman;

public class HuffmanNode {
    private int frecuencia;
    private int caracter;
    private HuffmanNode left;
    private HuffmanNode right;

    public HuffmanNode(int caracter, int frecuencia) {
        this.frecuencia = frecuencia;
        this.caracter = caracter;
        this.left = null;
        this.right = null;
    }

    public HuffmanNode(int caracter, int frecuencia, HuffmanNode left, HuffmanNode right) {
        this.frecuencia = frecuencia;
        this.caracter = caracter;
        this.left = left;
        this.right = right;
    }

    public int getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(int frecuencia) {
        this.frecuencia = frecuencia;
    }

    public int getCaracter() {
        return caracter;
    }

    public void setCaracter(int caracter) {
        this.caracter = caracter;
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public void setLeft(HuffmanNode left) {
        this.left = left;
    }

    public HuffmanNode getRight() {
        return right;
    }

    public void setRight(HuffmanNode right) {
        this.right = right;
    }
    
}
