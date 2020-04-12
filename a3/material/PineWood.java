package a3.material;

public class PineWood extends Material {
    private float[] matAmb = {0.30f, 0.2f, 0.1f, 1};
    private float[] matDif = {0.40f, 0.22f, 0.12f, 1};
    private float[] matSpe = {0.45f, 0.22f, 0.12f, 1};
    private float matShi = 10.0f;

    public float[] getAmbient(){return matAmb;}
    public float[] getDiffuse(){return matDif;}
    public float[] getSpecular(){return matSpe;}
    public float getShine(){return matShi;}
}
