package a4.sceneobject;

import a4.ShaderTools;
import a4.models.Sphere;

public class GoldSphere extends SceneObject{
    Sphere sphere;
    private float[] matAmb = ShaderTools.goldAmbient();
    private float[] matDif = ShaderTools.goldDiffuse();
    private float[] matSpe = ShaderTools.goldSpecular();
    private float matShi = ShaderTools.goldShininess();


    public GoldSphere(){
        sphere = new Sphere();
    }


}
