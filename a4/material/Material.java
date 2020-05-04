package a4.material;

import a4.ShaderTools;

public class Material {

    private float[] matAmb = ShaderTools.silverAmbient();
    private float[] matDif = ShaderTools.silverDiffuse();
    private float[] matSpe = ShaderTools.silverSpecular();
    private float matShi = ShaderTools.silverShininess();

    public float[] getAmbient(){return matAmb;}
    public float[] getDiffuse(){return matDif;}
    public float[] getSpecular(){return matSpe;}
    public float getShine(){return matShi;}

}
