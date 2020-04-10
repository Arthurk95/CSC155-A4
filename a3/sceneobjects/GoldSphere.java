package a3.sceneobjects;

import a3.ShaderTools;
import a3.models.Sphere;

public class GoldSphere extends SceneObject{
    Sphere sphere;
    float[] matAmb = ShaderTools.silverAmbient();
    float[] matDif = ShaderTools.silverAmbient();
    float[] matSpe = ShaderTools.silverAmbient();
    float matShi = ShaderTools.silverShininess();

    public GoldSphere(int i){
        sphere = new Sphere(i);
    }

    public GoldSphere(){
        sphere = new Sphere();
    }


}
