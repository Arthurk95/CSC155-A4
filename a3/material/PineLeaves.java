package a3.material;


public class PineLeaves extends Material{
    private float[] matAmb = {0.24f, 0.32f, 0.22f, 1};
    private float[] matDif = {0.32f, 0.55f, 0.16f, 1};
    private float[] matSpe = {0.32f, 0.55f, 0.16f, 1};
    private float matShi = 50.0f;

    public float[] getAmbient(){return matAmb;}
    public float[] getDiffuse(){return matDif;}
    public float[] getSpecular(){return matSpe;}
    public float getShine(){return matShi;}
}
