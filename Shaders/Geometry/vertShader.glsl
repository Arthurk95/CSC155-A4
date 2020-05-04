#version 430

layout (location=0) in vec4 vertPos;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec4 vertNormal;

out vec3 varyingOriginalNormal;
out vec2 tc;
struct PositionalLight
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};
struct Material
{	vec4 ambient;  
	vec4 diffuse;  
	vec4 specular;  
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform float alpha;
uniform float flipNormal;
layout (binding=1) uniform sampler2D samp;

void main(void)
{	varyingOriginalNormal = (norm_matrix * vertNormal).xyz;
	if (flipNormal < 0) varyingOriginalNormal = -varyingOriginalNormal;
	tc = texCoord;
	gl_Position = mv_matrix * vertPos;
}
