
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;

public class App {
    private static int[] camera = {0, 100, 0};
    private static int[] LS = {250, 300, 300};
    public static void writeIntLE(ByteBuffer dos, int value) throws IOException {
        dos.put((byte)(value & 0xFF));
        dos.put((byte)((value >> 8) & 0xFF));
        dos.put((byte)((value >> 16) & 0xFF));
        dos.put((byte)((value >> 24) & 0xFF));
    }

    public static void writeShortLE(ByteBuffer dos, int value) throws IOException {
        dos.put((byte)(value & 0xFF));
        dos.put((byte)((value >> 8) & 0xFF));
    }

    public static void writeByte(ByteBuffer dos, int value) throws IOException {
        dos.put((byte)(value & 0xFF));
    }

    public static void main(String[] args) throws Exception {
        String out = "img.bmp";
        if (args.length != 0){
            out = args[0];
            out += ".bmp";
        }
        int FOV = 90;
        int res = 1000; //final output resolution, midstep renders a square at (res x antialiasing level) 
        if (args.length > 1){
            res = Integer.parseInt(args[1]);
        }
        int AA = 2; //increases time taken squared (ex. AA = 2 results in 4x time to execute)
        if (args.length > 2){
            AA = Integer.parseInt(args[2]);
        }
        long count = 0; //number of pixels rendered
        long timer = 0; //total time spent (ns)
        Image bg = cache.getCache("bg.bmp");
        double bgConstant = (double)(bg.getheight())/(double)(res*AA);
        FileChannel output = FileChannel.open(Path.of(out),StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        ByteBuffer header = ByteBuffer.allocate((54));
        int[] check = {0, 0};
        createHeader(res*AA, header);
        header.flip();
        output.write(header);
        output.force(true);
        ByteBuffer buffer= ByteBuffer.allocate(res*AA*5);
        Polygon ground = new Polygon(new int[][]{{-300, 0, 100},{300, 0, 100},{300, 0, 300},{-300, 0, 300}},
                                    cache.getCache("tex2.bmp"), (byte)2, (byte)1);
        ArrayList<Polygon> poly3d = new ArrayList<>();
        ArrayList<Cube> cubes = new ArrayList<>();
        getCubes(cubes);
        for (Cube c : cubes) {
            poly3d.addAll(c.toPolyArray(camera));
        }
        poly3d.add(ground);
        ArrayList<Polygon2d> shadows = new ArrayList<>();
        // Create shadow 2D polygons
        for (Cube c : cubes){
            for (Polygon p : c.toPolyArray()) {
                int[][] verts = p.getVerts();
                int[][] shadowVerts = new int[4][];
                boolean valid = true;
                for (int i = 0; i < 4; i++) {
                    shadowVerts[i] = projectToGround(verts[i], LS);
                    if (shadowVerts[i] == null) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    Polygon shadow = new Polygon(shadowVerts, cache.getCache("tex3.bmp"), (byte)0, (byte)0);
                    shadows.add(shadow.castTo2d(camera, res*AA, FOV));
                }
            }
        }
        
        ArrayList<Polygon2d> draws = new ArrayList<>();
        ArrayList<Polygon> drawPolys = new ArrayList<>();
        for (Polygon p : poly3d) {
            draws.add(p.castTo2d(camera, res*AA, FOV));
            drawPolys.add(p);
        }
        for (int i = (res*AA) - 1; i >= 0; i--) {
            for (int j = 0; j < res*AA; j++) {
                long start = System.nanoTime();
                check[0] = j;
                check[1] = i;
                boolean found = false;
                for (int k = 0; k < draws.size(); k++) {
                    Polygon2d poly2d = draws.get(k);
                    Polygon poly = drawPolys.get(k);
                    if (inPoly(poly2d.getVerts(), check)){
                        int argb;
                        argb = findQuadCords(poly2d, new int[] {j, i});
                        if (isGround(poly) && isInShadow(check, shadows) || !poly.getLit(LS)) {
                            writeByte(buffer, (int)((argb & 0xFF)*0.5));
                            writeByte(buffer, (int)(((argb >> 8) & 0xFF)*0.5));
                            writeByte(buffer, (int)(((argb >> 16) & 0xFF)*0.5));
                            writeByte(buffer, (int)(((argb >> 24) & 0xFF)));
                        } else {
                            writeIntLE(buffer, argb);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    //writeIntLE(buffer, 0xFF000000);
                    writeIntLE(buffer, bg.getColourData((int)(j*bgConstant), (int)(i*bgConstant)));
                }
                timer += System.nanoTime() - start;
                count++;
            }
            buffer.flip();
            output.write(buffer);
            buffer.clear();
        }
        double avgTime = timer/count;
        System.out.println(avgTime);
        if (AA > 1){
            long start = System.nanoTime();
            superSampler.ssaa(cache.getCache(out), AA);
            System.out.println(System.nanoTime()-start);
        }
        
    }

    public static void createHeader(int res, ByteBuffer dos) throws IOException {
        // BMP file header (14 bytes)
        dos.put(dos.wrap("BM".getBytes()));
        int fileSize = 54 + (res * res * 4);  // File size
        writeIntLE(dos, fileSize);  // File size: 4 bytes
        writeIntLE(dos, 0);  // Reserved: 4 bytes
        writeIntLE(dos, 54);  // Data offset: 4 bytes
        
        // DIB header (BITMAPINFOHEADER, 40 bytes)
        writeIntLE(dos, 40);  // DIB header size: 4 bytes
        writeIntLE(dos, res);  // Width: 4 bytes
        writeIntLE(dos, res);  // Height: 4 bytes
        writeShortLE(dos, 1);  // Planes: 2 bytes
        writeShortLE(dos, 32);  // Bits per pixel: 2 bytes
        writeIntLE(dos, 0);  // Compression: 4 bytes
        writeIntLE(dos, 0);  // Image size: 4 bytes
        writeIntLE(dos, 0);  // X pixels per meter: 4 bytes
        writeIntLE(dos, 0);  // Y pixels per meter: 4 bytes
        writeIntLE(dos, 0);  // Colors used: 4 bytes
        writeIntLE(dos, 0);  // Important colors: 4 bytes
    }

    public static boolean inPoly(int[][] vertices, int[] check) {
        boolean inside = false;
        int n = vertices.length;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            if ((vertices[i][1] > check[1]) != (vertices[j][1] > check[1]) &&
                (check[0] < vertices[i][0] + (vertices[j][0] - vertices[i][0]) * (check[1] - vertices[i][1]) / (vertices[j][1] - vertices[i][1]))) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static int[] projectToGround(int[] point, int[] light) {
        double lx = light[0], ly = light[1], lz = light[2];
        double px = point[0], py = point[1], pz = point[2];
        if (Math.abs(py - ly) < 1e-6) return null; // Parallel to ground plane
        double t = -ly / (py - ly);
        int x_proj = (int) Math.round(lx + t * (px - lx));
        int z_proj = (int) Math.round(lz + t * (pz - lz));
        return new int[]{x_proj, 0, z_proj};
    }

    private static boolean isGround(Polygon poly) {
        int count = 4;
        for (int i[] : poly.getVerts()) {
            if (i[1] == 0){
                count--;
            }
        }
        return count == 0; // Assuming ground polygons have y=0
    }

    private static boolean isInShadow(int[] check, ArrayList<Polygon2d> shadows) {
        for (Polygon2d s : shadows) {
            if (inPoly(s.getVerts(), check)) return true;
        }
        return false;
    }

    private static int findQuadCords(Polygon2d poly, int[] check) {
        Image tex = poly.getColours();
        int[][] verts = poly.getVerts();
        double[] depths = poly.getDepths();
        double[][] uv = poly.getUVs();

        int[][] tri1 = {verts[0], verts[1], verts[2]};
        int[][] tri2 = {verts[0], verts[2], verts[3]};
        int[] tri1Idx = {0, 1, 2};
        int[] tri2Idx = {0, 2, 3};

        double[] weights = barycentricWeights(check, tri1);
        if (weights != null && weights[0] >= -1e-8 && weights[1] >= -1e-8 && weights[2] >= -1e-8) {
            return sampleTriangle(tex, check, tri1Idx, weights, depths, uv);
        }

        weights = barycentricWeights(check, tri2);
        if (weights != null && weights[0] >= -1e-8 && weights[1] >= -1e-8 && weights[2] >= -1e-8) {
            return sampleTriangle(tex, check, tri2Idx, weights, depths, uv);
        }

        return sampleTriangle(tex, check, tri1Idx, barycentricWeights(check, tri1), depths, uv);
    }

    private static double[] barycentricWeights(int[] p, int[][] tri) {
        double x0 = tri[0][0], y0 = tri[0][1];
        double x1 = tri[1][0], y1 = tri[1][1];
        double x2 = tri[2][0], y2 = tri[2][1];
        double denom = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2);
        if (Math.abs(denom) < 1e-9) {
            return null;
        }
        double x = p[0], y = p[1];
        double w0 = ((y1 - y2) * (x - x2) + (x2 - x1) * (y - y2)) / denom;
        double w1 = ((y2 - y0) * (x - x2) + (x0 - x2) * (y - y2)) / denom;
        double w2 = 1.0 - w0 - w1;
        return new double[]{w0, w1, w2};
    }

    private static int sampleTriangle(Image tex, int[] check, int[] triIdx, double[] weights, double[] depths, double[][] uv) {
        if (weights == null) {
            return 0;
        }
        double z0 = Math.max(depths[triIdx[0]], 1e-6);
        double z1 = Math.max(depths[triIdx[1]], 1e-6);
        double z2 = Math.max(depths[triIdx[2]], 1e-6);

        double u0 = uv[triIdx[0]][0];
        double v0 = uv[triIdx[0]][1];
        double u1 = uv[triIdx[1]][0];
        double v1 = uv[triIdx[1]][1];
        double u2 = uv[triIdx[2]][0];
        double v2 = uv[triIdx[2]][1];

        double w0 = weights[0];
        double w1 = weights[1];
        double w2 = weights[2];

        double uOverZ = w0 * u0 / z0 + w1 * u1 / z1 + w2 * u2 / z2;
        double vOverZ = w0 * v0 / z0 + w1 * v1 / z1 + w2 * v2 / z2;
        double oneOverZ = w0 / z0 + w1 / z1 + w2 / z2;
        if (Math.abs(oneOverZ) < 1e-9) {
            return 0;
        }

        double u = uOverZ / oneOverZ;
        double v = vOverZ / oneOverZ;

        int texX = (int)Math.round(u * (tex.getwidth() - 1));
        int texY = (int)Math.round(v * (tex.getheight() - 1));
        texX = Math.max(0, Math.min(tex.getwidth() - 1, texX));
        texY = Math.max(0, Math.min(tex.getheight() - 1, texY));
        return tex.getColourData(texX, texY);
    }

    private static void getCubes(ArrayList<Cube> out) throws NumberFormatException, IOException{
        File CD  = new File("cubes.cdata");
        Scanner read = new Scanner(CD);
        while (true){
            String[] temp = read.nextLine().split(",");
            if (temp[0].charAt(0) == '/'){
                break;
            } else if (temp[0].charAt(0) == 'c'){
                camera = new int[] {Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3])};
            } else if (temp[0].charAt(0) == 'L'){
                LS = new int[] {Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3])};
            } else {
                out.add(new Cube(new int[] {Integer.parseInt(temp[0]), Integer.parseInt(temp[1]),
                Integer.parseInt(temp[2])}, Integer.parseInt(temp[3]), cache.getCache(temp[4]),
                new byte[]{Byte.parseByte(temp[5]),Byte.parseByte(temp[6]),Byte.parseByte(temp[7]),Byte.parseByte(temp[8]),
                Byte.parseByte(temp[9]),Byte.parseByte(temp[10])}));
            }
            
        }
        read.close();
    }
}
