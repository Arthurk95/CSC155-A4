#version 430

in vec3 varyingNormal, varyingLightDir, varyingVertPos, varyingHalfVec, varyingTangent;
in vec4 shadow_coord;
in vec2 tc;
in vec3 eyeSpacePos;
out vec4 fragColor;

struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
};

struct Material
{	vec4 ambient, diffuse, specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
uniform vec4 cameraPos;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D samp;
layout (binding=2) uniform sampler2D normMap;

float lookup(float x, float y){
	float t = textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
	y * 0.001 * shadow_coord.w, -0.01, 0.0));
	return t;
}

vec3 calcNewNormal(){
	vec3 normal = normalize(varyingNormal);
	vec3 tangent = normalize(varyingNormal);
	tangent = normalize(tangent - dot(tangent, normal) * normal);
	vec3 bitangent = cross(tangent, normal);
	mat3 tbn = mat3(tangent, bitangent, normal);
	vec3 retrievedNormal = texture(normMap, tc).xyz;
	retrievedNormal = retrievedNormal * 2.0 - 1.0;
	vec3 newNormal = tbn * retrievedNormal;
	newNormal = normalize(newNormal);
	return newNormal;
}

void main(void)
{	float shadowFactor=0.0;

	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-varyingVertPos);
	vec3 H = normalize(varyingHalfVec);

	float swidth = 2.5;
	vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
	shadowFactor += lookup(-1.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup(-1.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor = shadowFactor / 4.0;

	// hi res PCF
	/*	float width = 2.5;
        float endp = width * 3.0 + width/2.0;
        for (float m=-endp ; m<=endp ; m=m+width)
        {	for (float n=-endp ; n<=endp ; n=n+width)
            {	shadowFactor += lookup(m,n);
        }	}
        shadowFactor = shadowFactor / 64.0;
    */
	// this would produce normal hard shadows
	//	shadowFactor = lookup(0.0, 0.0);

	float notInShadow = textureProj(shadowTex, shadow_coord);

	vec4 tex = texture(samp, tc);

	vec4 shadowColor = tex * (globalAmbient * material.ambient
	+ light.ambient * material.ambient);

	vec4 lightedColor = (light.diffuse * material.diffuse * max(dot(L,N),0.0)
	+ light.specular * material.specular
	* pow(max(dot(H,N),0.0),material.shininess*3.0));


	vec4 fogColor = vec4(0.7, 0.7, 0.7, 1.0);
	float dist = length(eyeSpacePos);

	float fogFactor = dist * 0.05;

	if(fogFactor > 1.0){fogFactor = 1.0;}
	// not producing soft shadows for some reason, but weird shadow problem fixed
	fragColor = mix(vec4((shadowColor.xyz*shadowFactor + lightedColor.xyz*shadowFactor), 1.0), fogColor, fogFactor);

	if(notInShadow == 1.0){
		fragColor += (globalAmbient * material.ambient) * shadowFactor;
	}
}
