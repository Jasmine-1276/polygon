public class Polygon2d {
    private int[][] verts;
    private Image tex;
    private byte shift;
    private double[] depth;
    private double[][] uv;

    public Polygon2d(int[][] verts, Image texture, byte s, double[] depth, double[][] uv) {
        this.verts = verts;
        this.tex = texture;
        this.shift = s;
        this.depth = depth;
        this.uv = uv;
    }

    public int[][] getVerts() {
        return verts;
    }

    public Image getColours() {
        return tex;
    }

    public byte getShift(){
        return shift;
    }

    public double[] getDepths() {
        return depth;
    }

    public double[][] getUVs() {
        return uv;
    }
}
