package a3.sceneobjects;

import a3.ShaderTools;

public abstract class SceneObject {
    float[] matAmb = ShaderTools.goldAmbient();
    float[] matDif = ShaderTools.goldDiffuse();
    float[] matSpe = ShaderTools.goldSpecular();
    float matShi = ShaderTools.goldShininess();

    public float[] getAmb(){return matAmb;}
    public float[] getDif(){return matDif;}
    public float[] getSpe(){return matSpe;}

}
