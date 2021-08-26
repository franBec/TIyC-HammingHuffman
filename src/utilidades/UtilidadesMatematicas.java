/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilidades;

import java.math.BigDecimal;
import java.util.Random;

public abstract class UtilidadesMatematicas {

    public static boolean isPotenciaDeDos(double number) {
        double log = Math.log(number) / Math.log(2);
        return getFraccional(log) == 0.0;
    }

    public static int randomInt(int limiteSup) {
        int i = new Random().nextInt(limiteSup);
        return i;
    }

    public static double getFraccional(double number) {
        int decimal = (int) number;
        return number - decimal;
    }

    public static float redondeo(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    
}
