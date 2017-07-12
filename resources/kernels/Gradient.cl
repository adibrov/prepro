/*
 * AD kernel
 */


__kernel void Gradient(
    __global char *input,
    __global float *output,
    int sizeX, int sizeY)
{
        unsigned int ix = get_global_id(0);
        unsigned int iy = get_global_id(1);

      //  float intGradX = ((input[iy*sizeX + ix+2] - input[iy*sizeX + ix-2]) + 8.0f*(input[iy*sizeX + ix+1] -
      //  input[iy*sizeX + ix-1]))/12.0f;
      //  float intGradY = ((input[(iy+2)*sizeX + ix] - input[(iy-2)*sizeX + ix]) + 8.0f*(input[(iy+1)*sizeX + ix] -
      //  input[(iy-1)*sizeX + ix]))/12.0f;

      float intGradX = (input[sizeX*iy+ix+1] - input[sizeX*iy+ix-1] + input[sizeX*(iy+1)+ix+1] - input[sizeX*(iy+1)
      +ix-1] + input[sizeX*(iy-1) + ix+1] - input[sizeX*(iy-1) + ix-1])/6.0f;
      float intGradY = (input[sizeX*(iy+1)+ix] - input[sizeX*(iy-1)+ix] + input[sizeX*(iy+1) + ix +1] -
      input[sizeX*(iy-1) + ix+1] + input[sizeX*(iy+1) + ix -1 ] - input[sizeX*(iy-1)+ix-1])/6.0f;


      output[iy*sizeX+ix] = (intGradX*intGradX + intGradY*intGradY);

}