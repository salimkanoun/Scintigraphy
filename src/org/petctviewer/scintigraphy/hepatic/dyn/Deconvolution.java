package org.petctviewer.scintigraphy.hepatic.dyn;
import java.util.Arrays;
 
public class Deconvolution {
    public static Double[] deconv(Double[] doubles, Double[] doubles2) {
        Double[] h = new Double[doubles.length - doubles2.length + 1];
        for (int n = 0; n < h.length; n++) {
            h[n] = doubles[n];
            int lower = Math.max(n - doubles2.length + 1, 0);
            for (int i = lower; i < n; i++)
                h[n] -= h[i] * doubles2[n - i];
            h[n] /= doubles2[0];
        }
        return h;
    }
    
    public static void main(String[] args) {
    	System.out.println(Arrays.toString(deconv(new Double[] {15.0}, new Double[] {3.0})));
    }
}
