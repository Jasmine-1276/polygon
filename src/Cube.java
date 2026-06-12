
import java.io.IOException;
import java.util.ArrayList;

public class Cube {
    private int[][] verts;
    private int scale;
    private Image tex;
    private byte[] shift;
    public Cube(int[] position, int scale, Image texture, byte[] s) {
        this.scale = scale;
        this.verts = new int[][]{
        {position[0], position[1], position[2]},
        {position[0]+scale, position[1], position[2]},
        {position[0], position[1], position[2]-scale},
        {position[0]+scale, position[1], position[2]-scale},
        {position[0], position[1]-scale, position[2]},
        {position[0]+scale, position[1]-scale, position[2]},
        {position[0], position[1]-scale, position[2]-scale},
        {position[0]+scale, position[1]-scale, position[2]-scale},};
        this.tex = texture;
        this.shift = s;
    }

    public ArrayList<Polygon> toPolyArray() throws IOException{
        ArrayList<Polygon> out = new ArrayList<Polygon>();
        out.add(new Polygon(new int[][] {verts[0],verts[2],verts[3],verts[1]}, tex, shift[0], (byte)1));
        out.add(new Polygon(new int[][] {verts[4],verts[5],verts[1],verts[0]}, tex, shift[1], (byte)2));
        out.add(new Polygon(new int[][] {verts[5],verts[7],verts[3],verts[1]}, tex, shift[2], (byte)3));
        out.add(new Polygon(new int[][] {verts[7],verts[6],verts[2],verts[3]}, tex, shift[3], (byte)4));
        out.add(new Polygon(new int[][] {verts[6],verts[4],verts[0],verts[2]}, tex, shift[4], (byte)5));
        out.add(new Polygon(new int[][] {verts[6],verts[4],verts[5],verts[7]}, tex, shift[5], (byte)6));
        return out;
    }

    public ArrayList<Polygon> toPolyArray(int[] camera) throws IOException{
        ArrayList<Polygon> out = toPolyArray();
        sortPolygonsBackToFront(out, camera);
        return out;
    }

    private static void sortPolygonsBackToFront(ArrayList<Polygon> polys, int[] camera) {
        for (int i = 0; i < polys.size() - 1; i++) {
            int closestIndex = i;
            double closestDepth = polygonDepth(polys.get(i), camera);
            for (int j = i + 1; j < polys.size(); j++) {
                double depth = polygonDepth(polys.get(j), camera);
                if (depth < closestDepth) {
                    closestDepth = depth;
                    closestIndex = j;
                }
            }
            if (closestIndex != i) {
                Polygon temp = polys.get(i);
                polys.set(i,polys.get(closestIndex));
                polys.set(closestIndex, temp);
            }
        }
    }

    private static double polygonDepth(Polygon poly, int[] camera) {
        int[][] verts = poly.getVerts();
        double sumX = 0.0, sumY = 0.0, sumZ = 0.0;
        for (int[] vert : verts) {
            sumX += vert[0];
            sumY += vert[1];
            sumZ += vert[2];
        }
        double centerX = sumX / verts.length;
        double centerY = sumY / verts.length;
        double centerZ = sumZ / verts.length;

        double dx = centerX - camera[0];
        double dy = centerY - camera[1];
        double dz = centerZ - camera[2];
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
}

