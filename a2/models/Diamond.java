package a2.models;

public class Diamond {
    float[] diamondPositions =
            {    -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.0f, 0.5f, 0.0f, //front
                    0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.0f, 0.5f, 0.0f, //right
                    0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.0f, 0.5f, 0.0f, //back
                    -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.0f, 0.5f, 0.0f, //left
                    0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f,0.0f, -1.5f, 0.0f, //front t2
                    0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.0f, -1.5f, 0.0f, //right t2
                    -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.0f, -1.5f, 0.0f, //back t2
                    -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.0f, -1.5f, 0.0f //left t2
            };

    float[] textureCoords =
            {       0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                    0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                    0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                    0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                    0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                    0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                    0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                    0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f
            };
    public float[] getPositions(){
        return diamondPositions;
    }

    public float[] getTextureCoords(){ return textureCoords;}
}
