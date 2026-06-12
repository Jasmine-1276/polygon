
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class superSampler {
    public static void ssaa(Image in, int level) throws IOException{
        int ls = level*level;
        Path outPath = Path.of((in.toString().replace(".bmp", ""))+ "Post.bmp");
        int res = in.getheight();
        int[][][] data = new int[res][res][3];
        FileChannel output = FileChannel.open(outPath,StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        ByteBuffer buffer = ByteBuffer.allocate((res/level)*4);
        ByteBuffer header = ByteBuffer.allocate(54);
        long start = System.nanoTime();
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                int temp = in.getColourData(x, y);
                data[y][x][0] = temp & 0xFF;
                data[y][x][1] = (temp >> 8) & 0xFF;
                data[y][x][2] = (temp >> 16) & 0xFF;
            }
        }
        System.out.println(System.nanoTime()-start);
        App.createHeader(res/level, header);
        header.flip();
        output.write(header);
        output.force(true);
        for (int y = 1; y <= res; y+=level) {
            for (int x = 1; x <= res; x+=level) {
                double[] out = new double[3];
                for (int i = 0; i < 3; i++) {
                    for (int xx = 0; xx < level; xx++) {
                        for (int yy = 0; yy < level; yy++) {
                            out[i] += data[res-yy-y][res-xx-x][i];
                        }
                    }
                    out[i] /= ls;
                }
                App.writeByte(buffer, (int)out[0]);
                App.writeByte(buffer, (int)out[1]);
                App.writeByte(buffer, (int)out[2]);
                App.writeByte(buffer, 0xFF);
            }
            buffer.flip();
            output.write(buffer);
            output.force(true);
            buffer.clear();
        }
    }
}
