#version 430

in vec2 tc;
out vec4 tex;


uniform mat4 mv_matrix;
uniform mat4 proj_matrix;


layout (binding=0) uniform sampler2D s;
void main(void)
{
	tex = texture(s, tc);
}