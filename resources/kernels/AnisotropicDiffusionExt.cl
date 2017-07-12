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



        float intGradX = (input[iy*sizeX + ix+2] - input[iy*sizeX + ix-2] + 8.0f*(input[iy*sizeX + ix+1] -
        input[iy*sizeX + ix-1]))/12.0f;
        float intGradY = (input[(iy+2)*sizeX + ix] - input[(iy-2)*sizeX + ix] + 8.0f*(input[(iy+1)*sizeX + ix] - input[
        (iy-1)*sizeX + ix]))/12.0f;
        float intGradModSq = (intGradX*intGradX + intGradY*intGradY);

        float intGradXUP = (input[(iy+1)*sizeX + ix+2] - input[(iy+1)*sizeX + ix-2] + 8.0f*(input[(iy+1)*sizeX +
        ix+1] - input[(iy+1)*sizeX + ix-1]))/12.0f;
        float intGradYUP = (input[(iy+3)*sizeX + ix] - input[(iy-1)*sizeX + ix] + 8.0f*(input[(iy+2)*sizeX + ix] -
        input[iy*sizeX + ix]))/12.0f;
        float Dup = D0*exp(-(intGradXUP*intGradXUP + intGradYUP*intGradYUP)/k);

        float intGradXDOWN = (input[(iy-1)*sizeX + ix+2] - input[(iy-1)*sizeX + ix-2] + 8.0f*(input[(iy-1)*sizeX +
        ix+1] - input[(iy-1)*sizeX + ix-1]))/12.0f;
        float intGradYDOWN = (input[(iy+1)*sizeX + ix] - input[(iy-3)*sizeX + ix] + 8.0f*(input[(iy)*sizeX + ix] -
        input[(iy-2)*sizeX + ix]))/12.0f;
        float Ddown = D0*exp(-(intGradXDOWN*intGradXDOWN + intGradYDOWN*intGradYDOWN)/k);

        float intGradXLEFT = (input[(iy)*sizeX + (ix+1)] - input[(iy)*sizeX + (ix-3)] + 8.0f*(input[(iy)*sizeX + (ix)
        ] - input[(iy)*sizeX + (ix-2)]))/12.0f;
        float intGradYLEFT = (input[(iy+2)*sizeX + ix-1] - input[(iy-2)*sizeX + ix-1] + 8.0f*(input[(iy+1)*sizeX +
        ix-1] - input[(iy-1)*sizeX + ix-1]))/12.0f;
        float Dleft = D0*exp(-(intGradXLEFT*intGradXLEFT + intGradYLEFT*intGradYLEFT)/k);

        float intGradXRIGHT = (input[(iy)*sizeX + ix+3] - input[(iy)*sizeX + ix-1] + 8.0f*(input[(iy)*sizeX + ix+2] -
        input[(iy)*sizeX + ix]))/12.0f;
        float intGradYRIGHT = (input[(iy+2)*sizeX + ix+1] - input[(iy-2)*sizeX + ix+1] + 8.0f*(input[(iy+1)*sizeX +
        ix+1] - input[(iy-1)*sizeX + ix+1]))/12.0f;
        float Dright = D0*exp(-(intGradXRIGHT*intGradXRIGHT + intGradYRIGHT*intGradYRIGHT)/k);

        float laplace =(0.5f*input[iy*sizeX + ix+1] + 0.5f*input[(iy+1)*sizeX + ix] + 0.5f*input[iy*sizeX + ix-1] +
        0.5f*input[(iy-1)*sizeX+ ix] - 3.0f*input[sizeX*iy + ix] + 0.25f*input[(iy+1)*sizeX + ix+1] + 0.25f*input[
        (iy-1)*sizeX + ix-1] + 0.25f*input[(iy+1)*sizeX + ix-1] + 0.25f*input[(iy-1)*sizeX + ix+1]);

        float D = D0*exp(-(intGradModSq)/k);

        float DGradX = (Dright-Dleft)/1.0f;
        float DGradY = (Dup-Ddown)/1.0f;



       // int res = (int)(DGradX*intGradX + DGradY*intGradY + D * laplace);
        int res = (int)( D0 * laplace);

        //output[iy*sizeX+ix] = ((res<<16) | (res<<8) | res);
        output[iy*sizeX+ix] = input[iy*sizeX+ix] + res;

}