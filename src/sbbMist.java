import java.util.Collections;
import java.util.Vector;
import java.util.zip.Deflater;

/**
 * Created by happywu on 19/08/16.
 */
public class sbbMist {
    public static int _TRAIN_PHASE = 0;
    public static int _TEST_PHASE= 0;
    public static int _PLAY_PHASE = 3;
    public final double EPSILON_SBB = 1e-5;
    public static double vecMedian(Vector<Double> vec){
        Collections.sort(vec);
        int mid = vec.size()/2;
        return vec.size() % 2 == 0 ? (vec.get(mid)+vec.get(mid+1))/2 : vec.get(mid);
    }
    public static double discretize(double f, double min, double max, int steps){
        double d = Math.round((f-min)/(max-min)*(steps-1));
        return d>steps?steps-1:d;
    }

    public static double normalizedCompressionDistance(Vector<Integer> v1, Vector<Integer> v2){
        String x = v1.toString();
        String y = v2.toString();
        int cx = C(x);
        int cy = C(y);
        int cxy = C(x+y);
        return (cxy - (double) Math.min(cx, cy)) / Math.max(cx, cy);
    }
    // Find the length of a String after compression
    public static int C(String inputString) {
        // Encode a String into bytes
        byte[] input = inputString.getBytes();

        // Compress the bytes
        byte[] output = new byte[input.length + 100];
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();

        // Return the *length* of the compressed version.
        return compresser.deflate(output);
    }
    public boolean isEqual(double x, double y){ return Math.abs(x-y) < EPSILON_SBB;}
    public static boolean isEqual(double x, double y, double e){ return Math.abs(x-y) < e;}
    public boolean isEqual(Vector<Integer> x, Vector<Integer> y){
        if(x.size() != y.size())return false;
        for(int i = 0;i<x.size();i++)
            if(x.get(i)!=y.get(i))return false;
        return true;
    }
    public static boolean isEqual(Vector<Double> x, Vector<Double> y, double e){
        if(x.size() != y.size())return false;
        for(int i = 0;i<x.size();i++)
            if(isEqual(x.get(i), y.get(i), e) == false) return false;
        return true;
    }

}
