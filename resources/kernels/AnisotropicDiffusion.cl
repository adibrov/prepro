/*
 * AD kernel
 */


__kernel void AnisotropicDiffusion(
    __global char *input,
    __global char *output,
    int sizeX, int sizeY, float k, float D0)
{
        unsigned int ix = get_global_id(0);
        unsigned int iy = get_global_id(1);



        float intGradX = (input[iy*sizeX + ix+1] - input[iy*sizeX + ix-1])/2.0f;
        float intGradY = (input[(iy+1)*sizeX + ix] - input[(iy-1)*sizeX + ix])/2.0f;
        float intGradModSq = (intGradX*intGradX + intGradY*intGradY);

        float intGradXUP = (input[(iy+1)*sizeX + ix+1] - input[(iy+1)*sizeX + ix-1])/2.0f;
        float intGradYUP = (input[(iy+2)*sizeX + ix] - input[iy*sizeX + ix])/2.0f;
        float Dup = D0*exp(-(intGradXUP*intGradXUP + intGradYUP*intGradYUP)/k);

        float intGradXDOWN = (input[(iy-1)*sizeX + ix+1] - input[(iy-1)*sizeX + ix-1])/2.0f;
        float intGradYDOWN = (input[(iy)*sizeX + ix] - input[(iy-2)*sizeX + ix])/2.0f;
        float Ddown = D0*exp(-(intGradXDOWN*intGradXDOWN + intGradYDOWN*intGradYDOWN)/k);

        float intGradXLEFT = (input[(iy)*sizeX + (ix)] - input[(iy)*sizeX + (ix-2)])/2.0f;
        float intGradYLEFT = (input[(iy+1)*sizeX + ix-1] - input[(iy-1)*sizeX + ix-1])/2.0f;
        float Dleft = D0*exp(-(intGradXLEFT*intGradXLEFT + intGradYLEFT*intGradYLEFT)/k);

        float intGradXRIGHT = (input[(iy)*sizeX + ix+2] - input[(iy)*sizeX + ix])/2.0f;
        float intGradYRIGHT = (input[(iy+1)*sizeX + ix+1] - input[(iy-1)*sizeX + ix+1])/2.0f;
        float Dright = D0*exp(-(intGradXRIGHT*intGradXRIGHT + intGradYRIGHT*intGradYRIGHT)/k);

        float laplace =(input[iy*sizeX + ix+1] + input[(iy+1)*sizeX + ix] + input[iy*sizeX + ix-1] + input[(iy-1)*sizeX
        + ix] - 4.0f*input[sizeX*iy + ix]);

        float D = D0*exp(-(intGradModSq)/k);

        float DGradX = (Dright-Dleft)/2.0f;
        float DGradY = (Dup-Ddown)/2.0f;



        int res = (int)(DGradX*intGradX + DGradY*intGradY + D * laplace);

        //output[iy*sizeX+ix] = ((res<<16) | (res<<8) | res);
        output[iy*sizeX+ix] = input[iy*sizeX+ix] + res;

}