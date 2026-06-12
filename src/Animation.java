
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Animation {
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1; i++) {
            double ease = (double)i/23.0;
            drawFrame(cubic(ease), i);
        }
    }

    private static double cubic(double in){
        if (in < 0.5){
            return in * in * in * 4;
        } else {
            return ((in - 1)*((in*2) - 2)*((in*2) - 2)) + 1;
        }
    }

    private static void drawFrame(double delta, int i) throws Exception{
        int LSpos = (int)((double)480*delta) - 240;
        FileWriter fw = new FileWriter("cubes.cdata");
        BufferedWriter out = new BufferedWriter(fw);
        out.write("50,120,250,120,tex.bmp,0,0,0,0,0,0");
        out.newLine();
        out.write("-150,50,250,50,tex3.bmp,3,0,3,0,0,0");
        out.newLine();
        out.write("-100,10,150,10,tex4.bmp,0,0,0,0,0,0");
        out.newLine();
        out.write("LS,"+LSpos+",300,300");
        out.newLine();
        out.write("//");
        out.close();
        App.main(new String[] {"anim/img" + (i+1) + ".bmp", "" + 500, "" + 1});
    }
}
