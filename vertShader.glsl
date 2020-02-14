#version 430

uniform float increment;
uniform float theta;
uniform float color;
uniform float scale;

float x, y;

out vec4 vertColor;

void calcNextPosCircle();
void calcNextPosLine();

void main(void){
    if (theta == 0)
        calcNextPosLine();
    else {
        calcNextPosCircle();
    }

    if (gl_VertexID == 0){ // bottom right corner
        gl_Position = vec4(((scale*0.25) + x), ((scale*-0.25) + y), 0.0, 1.0);

        // either a solid color or a gradient
        if(color == 0.0)
            vertColor = vec4(0.0, 0.0, 1.0, 0.0);
        else vertColor = vec4(1.0, 0.0, 0.0, 0.0);
    }
    else if (gl_VertexID == 1){ // bottom left corner
        gl_Position = vec4(((scale*-0.25) + x), ((scale*-0.25) + y), 0.0, 1.0);
        if(color == 0.0)
            vertColor = vec4(0.0, 0.0, 1.0, 0.0);
        else vertColor = vec4(0.0, 1.0, 0.0, 0.0);
    }
    else { // top
        gl_Position = vec4((0 + x), ((scale*0.25) + y), 0.0, 1.0);
        if(color == 0.0)
            vertColor = vec4(0.0, 0.0, 1.0, 0.0);
        vertColor = vec4(0.0, 0.0, 1.0, 0.0);
    }
}

void calcNextPosCircle(){
    x = (0.5*cos(radians(90 + theta)));
    y = (0.5*sin(radians(90 + theta)));
}

void calcNextPosLine(){
  x = 0;
  y = increment;
}