/*
 * Gaussian kernel
 */


__kernel void Gaussian(
    __global char *input,
    __global char *output,
    int sizeX, int sizeY,
    int delX, int delY,
    float sigmaX, float sigmaY
    )
{
        unsigned int ix = get_global_id(0);
        unsigned int iy = get_global_id(1);
        int res = 0;
        float resF = 0.0f;
        float norm = 0.0f;
        float ga = 0.0f;

        for (float i = ix - delX; i <= ix + delX; i+=1.0f) {
            for (float j = iy - delY; j <= iy + delY; j+=1.0f) {
                if (i>=0 & i<sizeX & j>=0 & j<sizeY)
                {
                    float arg = -((float)ix-i)*((float)ix-i)/(2.0f*sigmaX*sigmaX) - ((float)iy-j)*((float)iy-j)/
                    (2.0f*sigmaY*sigmaY);
                    ga = exp(arg);

                    norm += ga;
                    resF += ((float)input[((int)j)*sizeX + (int)i])*ga;
                  //  printf("%f\n", resF);
                }
            }
        }


        res = (int)(resF/norm);

        //output[iy*sizeX+ix] = ((res<<16) | (res<<8) | res);
        output[iy*sizeX+ix] = res;

}