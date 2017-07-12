/*
 * AD kernel
 */


__kernel void AnisotropicDiffusion(
    __global char *input,
    __global float *gradient,
    __global char *output,
    int sizeX, int sizeY, float k, float D0)
{
        unsigned int ix = get_global_id(0);
        unsigned int iy = get_global_id(1);

       // float DGradX = D0*(8.0f*(exp(-gradient[sizeX*iy + ix+1]/k) - exp(-gradient[sizeX*iy + ix-1]/k)) + exp
       // (-gradient[sizeX*iy+ ix+2]/k) - exp(-gradient[sizeX*iy + ix-2]/k))/12.0f;
       // float DGradY = D0*((exp(-gradient[sizeX*(iy+1) + ix]/k) - exp(-gradient[sizeX*(iy-1) + ix]/k))*8.0f + exp
       // (-gradient[sizeX*(iy+2)+ ix]/k)- exp(-gradient[sizeX*(iy-2) + ix]/k))/12.0f;

       float DGradX = D0*(exp(-gradient[sizeX*iy + ix+1]/k) - exp(-gradient[sizeX*iy + ix-1]/k) + exp
       (-gradient[sizeX*(iy+1)+ ix+1]/k) - exp(-gradient[sizeX*(iy+1) + ix-1]/k) + exp(-gradient[sizeX*(iy-1)+
       ix+1]/k) -exp(-gradient[sizeX*(iy-1) + ix-1]/k))/6.0f;
       float DGradY = D0*((exp(-gradient[sizeX*(iy+1) + ix]/k) - exp(-gradient[sizeX*(iy-1) + ix]/k)) + exp
       (-gradient[sizeX*(iy+1)+ ix+1]/k)- exp(-gradient[sizeX*(iy-1) + ix+1]/k) + exp(-gradient[sizeX*(iy+1) +
       ix-1]/k) - exp(-gradient[sizeX*(iy-1) + ix-1]/k))/6.0f;

       // float intGradX = ((input[iy*sizeX + ix+2] - input[iy*sizeX + ix-2]) + 8.0f*(input[iy*sizeX + ix+1] -
        //input[iy*sizeX + ix-1]))/12.0f;
        ///float intGradY = ((input[(iy+2)*sizeX + ix] - input[(iy-2)*sizeX + ix]) + 8.0f*(input[(iy+1)*sizeX + ix] -
        //input[(iy-1)*sizeX + ix]))/12.0f;

        float intGradX = ((input[iy*sizeX + ix+1] - input[iy*sizeX + ix-1]) + (input[(iy+1)*sizeX + ix+1] -
      input[(iy+1)*sizeX +ix-1]) + (input[(iy-1)*sizeX + ix+1] - input[(iy-1)*sizeX + ix-1]) )/6.0f;
      float intGradY = ((input[(iy+1)*sizeX + ix] - input[(iy-1)*sizeX + ix]) + (input[(iy+1)*sizeX + ix+1] - input[
      (iy-1)*sizeX + ix+1]) + (input[(iy+1)*sizeX + ix-1] - input[(iy-1)*sizeX + ix-1]))/6.0f;


        float laplace =(0.5f*input[iy*sizeX + ix+1] + 0.5f*input[(iy+1)*sizeX + ix] + 0.5f*input[iy*sizeX + ix-1] +
        0.5f*input[(iy-1)*sizeX+ ix] - 3.0f*input[sizeX*iy + ix] + 0.25f*input[(iy+1)*sizeX + ix+1] + 0.25f*input[
        (iy-1)*sizeX + ix-1] + 0.25f*input[(iy+1)*sizeX + ix-1] + 0.25f*input[(iy-1)*sizeX + ix+1]);

        float D = D0*exp(-gradient[sizeX*iy+ix]/k);
        char res = (char)(DGradX*intGradX + DGradY*intGradY + D * laplace);


        //output[iy*sizeX+ix] = ((res<<16) | (res<<8) | res);
        output[iy*sizeX+ix] = (char)(input[iy*sizeX+ix] + res);

}