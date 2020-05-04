package a4.material;

public class DarkGrass extends Material{
    private float[] matAmb = {0.1f, 0.2f, 0.05f, 1};
    private float[] matDif = {0.15f, 0.3f, 0.1f, 1};
    private float[] matSpe = {0.15f, 0.3f, 0.1f, 1};
    private float matShi = 30.0f;

    public float[] getAmbient(){return matAmb;}
    public float[] getDiffuse(){return matDif;}
    public float[] getSpecular(){return matSpe;}
    public float getShine(){return matShi;}
}
