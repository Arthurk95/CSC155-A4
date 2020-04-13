package a3.material;

public class DarkMaterial extends Material{
    private float[] matAmb = {0.01f, 0.01f, 0.01f, 1};
    private float[] matDif = {0.1f, 0.1f, 0.1f, 1};
    private float[] matSpe = {0.2f, 0.2f, 0.2f, 1};
    private float matShi = 20.0f;

    public float[] getAmbient(){return matAmb;}
    public float[] getDiffuse(){return matDif;}
    public float[] getSpecular(){return matSpe;}
    public float getShine(){return matShi;}
}
