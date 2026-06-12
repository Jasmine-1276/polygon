import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Image {

    private byte[] imgData;
    private int height;
    private int width;
    private String p;

    public Image(Path bmp) throws IOException{
        this.p = bmp.toString();
        this.imgData = Files.readAllBytes(bmp);
        // Read little-endian width and height from DIB header
        this.width = ((imgData[21] & 0xFF) << 24) | ((imgData[20] & 0xFF) << 16) | ((imgData[19] & 0xFF) << 8) | (imgData[18] & 0xFF);
        this.height = ((imgData[25] & 0xFF) << 24) | ((imgData[24] & 0xFF) << 16) | ((imgData[23] & 0xFF) << 8) | (imgData[22] & 0xFF);
    }

    public int getwidth(){
        return this.width;
    }

    public int getheight(){
        return this.height;
    }

    public int getColourData(int x, int y){
        int index = imgData.length - 4 - (x*4) - (y*width*4);
        byte r = imgData[index];
        byte g = imgData[index + 1];
        byte b = imgData[index + 2];
        byte a = imgData[index + 3];
        return ((a & 0xFF) << 24) | ((b & 0xFF) << 16) | ((g & 0xFF) << 8) | r & 0xFF;
    }

    public byte[] getFullData(){
        return this.imgData;
    }

    public String toString(){
        return this.p;
    }
}
