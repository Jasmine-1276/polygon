
public class Polygon {
    private int[][] verticies;
    private byte shift;
    private byte id;
    private Image tex;
    private int[] vertAvg;

    public Polygon(int[][] verticies, Image texture, byte s, byte id){
        this.verticies = verticies;
        this.tex = texture;
        this.shift = s;
        this.id = id;
        this.vertAvg = new int[3];
        this.vertAvg[0] = this.verticies[0][0] + this.verticies[1][0] + this.verticies[2][0] + 
                          this.verticies[3][0];
        this.vertAvg[0] /= 4;
        this.vertAvg[1] = this.verticies[0][1] + this.verticies[1][1] + this.verticies[2][1] + 
                          this.verticies[3][1];
        this.vertAvg[1] /= 4;
        this.vertAvg[2] = this.verticies[0][2] + this.verticies[1][2] + this.verticies[2][2] + 
                          this.verticies[3][2];
        this.vertAvg[2] /= 4;
    }

    public Polygon2d castTo2d(int[] camera, int res, int FOV){
        double f = (res / 2.0) / Math.tan(Math.toRadians(FOV / 2.0));
        int[][] vertOut = new int[4][2];
        double[] depth = new double[4];
        double[][] uv = new double[4][2];
        double[][] baseUV = {{0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}};
        for (int i = 0; i < 4; i++) {
            double dx = verticies[i][0] - camera[0];
            double dy = verticies[i][1] - camera[1];
            double dz = verticies[i][2] - camera[2];
            vertOut[i][0] = (int)Math.round(res / 2.0 + dx * f / dz);
            vertOut[i][1] = (int)Math.round(res / 2.0 - dy * f / dz);
            depth[i] = Math.abs(dz);
            int uvIndex = Math.floorMod(i - this.shift, 4);
            uv[i][0] = baseUV[uvIndex][0];
            uv[i][1] = baseUV[uvIndex][1];
        }
        return new Polygon2d(vertOut, this.tex, this.shift, depth, uv);
    }

    public int[][] getVerts() {
        return this.verticies;
    }

    public void setVerts(int[][] in) {
        this.verticies = in;
    }

    public Image getColours() {
        return this.tex;
    }

    public void setColours(Image in) {
        this.tex = in;
    }

    // Compute the normal vector of the polygon plane
    private double[] getNormal() {
        int[] v0 = verticies[0];
        int[] v1 = verticies[1];
        int[] v2 = verticies[2];
        double[] edge1 = {v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2]};
        double[] edge2 = {v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2]};
        double[] normal = {
            edge1[1] * edge2[2] - edge1[2] * edge2[1],
            edge1[2] * edge2[0] - edge1[0] * edge2[2],
            edge1[0] * edge2[1] - edge1[1] * edge2[0]
        };
        return normal;
    }

    // Check if the line segment from p1 to p2 intersects this polygon
    // Returns true if obscured (segment crosses the polygon interior)
    public boolean isObscured(int[] p1, int[] p2) {
        double[] normal = getNormal();
        double d = - (normal[0] * verticies[0][0] + normal[1] * verticies[0][1] + normal[2] * verticies[0][2]);
        
        double[] dir = {p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]};
        double denom = normal[0] * dir[0] + normal[1] * dir[1] + normal[2] * dir[2];
        
        if (Math.abs(denom) < 1e-6) return false; // Segment parallel to plane
        
        double t = - (normal[0] * p1[0] + normal[1] * p1[1] + normal[2] * p1[2] + d) / denom;
        
        if (t <= 0 || t >= 1) return false; // Intersection outside segment
        
        // Compute intersection point
        double[] inter = {
            p1[0] + t * dir[0],
            p1[1] + t * dir[1],
            p1[2] + t * dir[2]
        };
        
        // Check if intersection point is inside the polygon
        return isPointInsidePolygon(inter);
    }
    
    // Check if a 3D point is inside the planar polygon
    private boolean isPointInsidePolygon(double[] point) {
        double[] planeNormal = getNormal();
        for (int i = 0; i < verticies.length; i++) {
            int[] v1 = verticies[i];
            int[] v2 = verticies[(i + 1) % verticies.length];
            double[] edge = {v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]};
            double[] edgeNormal = {
                edge[1] * planeNormal[2] - edge[2] * planeNormal[1],
                edge[2] * planeNormal[0] - edge[0] * planeNormal[2],
                edge[0] * planeNormal[1] - edge[1] * planeNormal[0]
            };
            double[] vec = {point[0] - v1[0], point[1] - v1[1], point[2] - v1[2]};
            double dot = vec[0] * edgeNormal[0] + vec[1] * edgeNormal[1] + vec[2] * edgeNormal[2];
            if (dot < 0) return false; // Outside
        }
        return true; // Inside
    }

    public byte getID(){
        return id;
    }

    public boolean getLit(int[] LS){
        switch (id){
            case 1: {
                if (vertAvg[1] <= LS[1]){
                    return true;
                }
                break;
            }
            case 2: {
                if (vertAvg[2] <= LS[2]){
                    return true;
                }
                break;
            }
            case 3: {
                if (vertAvg[0] <= LS[0]){
                    return true;
                }
                break;
            }
            case 4: {
                if (vertAvg[2] >= LS[2]){
                    return true;
                }
                break;
            }
            case 5: {
                if (vertAvg[0] >= LS[0]){
                    return true;
                }
                break;
            }
            default: {
                if (vertAvg[1] >= LS[1]){
                    return true;
                }
                break;
            }
        }
        return false;
    }
}