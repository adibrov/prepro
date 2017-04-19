package main;

import com.sun.javafx.image.impl.ByteBgra;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
//import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.jocl.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;

import static org.jocl.CL.*;
import com.sun.prism.PixelFormat;

/**
 * Created by dibrov on 12/04/17.
 */
public class AnisotropicDiffusionNoGrad {

    private BufferedImage image;
    private int sizeX;
    private int sizeY;
    private float sigmaX;
    private float sigmaY;
    private int delX;
    private int delY;
    private String kernelSourceDiff;
    private String kernelSourceGrad;
    private cl_context context;
    private cl_command_queue commandQueue;
    private cl_kernel kernelDiff;
    private cl_kernel kernelGrad;
    private cl_mem pixelMem;
    private cl_mem colorMapMem;
    private cl_mem gradient;
    private byte[] imageBuffer;
    private BufferedImage outImg;
    private  BufferedImage intermImg;
    float K;
    float D0;
    int steps =  0; ;
    WritableImage wi;


    public void setSteps(int steps) {
        this.steps = steps;
    }

    private cl_mem outputImgCL;
    ;
    //private cl_mem pY;


    public AnisotropicDiffusionNoGrad(BufferedImage inputImg, float D0, float K) {
        this.K = K;
        this.sizeY = inputImg.getHeight();
        this.steps = steps;
        this.sizeX = inputImg.getWidth();
        this.image = inputImg;
        this.imageBuffer =((DataBufferByte)(inputImg.getRaster().getDataBuffer())).getData();
        this.D0 = D0;




//        Constructor<WritablePixelFormat> constr = null;
//
//        try {
//             constr = WritablePixelFormat.class.getConstructor();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }

//        constr.setAccessible(true)


        outImg = new BufferedImage (sizeX, sizeY, BufferedImage.TYPE_BYTE_GRAY);
        intermImg = new BufferedImage (sizeX, sizeY, BufferedImage.TYPE_BYTE_GRAY);

        System.out.println("");

        this.sigmaX = sigmaX;
        this.sigmaY = sigmaY;
        this.delX = delX;
        this.delY = delY;
        this.kernelSourceDiff = readFile("resources/kernels/AnisotropicDiffusionNoGrad.cl");
        this.kernelSourceGrad = readFile("resources/kernels/Gradient.cl");



        initCL();
    }

    public void setSigma(float value){
        System.out.println("setting sigma to: " + value);
        if (value == 0) {
            sigmaX = 0.01f;
            sigmaY = 0.01f;
        }
        else {
            sigmaX = value;
            sigmaY = value;
        }

    }

    public static void saveToFile(Image image) {
        File outputFile = new File("./test.png");
        if (image == null) {
            System.out.println("wrong!");
        }
        BufferedImage bImage = new BufferedImage((int)image.getWidth(), (int)image.getHeight(), BufferedImage
                .TYPE_BYTE_GRAY);
        SwingFXUtils.fromFXImage(image, bImage);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public BufferedImage convolve() {
        long globalWorkSize[] = new long[2];
        globalWorkSize[0] = sizeX;
        globalWorkSize[1] = sizeY;


        image.copyData(intermImg.getRaster());
        byte[] buf =((DataBufferByte)(intermImg.getRaster().getDataBuffer())).getData();

        for (int i = 0; i < steps; i++) {

            clEnqueueWriteBuffer(commandQueue, pixelMem, true, 0,
                    Sizeof.cl_char * sizeY * sizeX, Pointer.to(buf), 0, null, null);


            clSetKernelArg(kernelGrad, 0, Sizeof.cl_mem, Pointer.to(pixelMem));
            clSetKernelArg(kernelGrad, 1, Sizeof.cl_mem, Pointer.to(gradient));
            clSetKernelArg(kernelGrad, 2, Sizeof.cl_mem, Pointer.to(new int[] {sizeX}));
            clSetKernelArg(kernelGrad, 3, Sizeof.cl_mem, Pointer.to(new int[] {sizeY}));

            clSetKernelArg(kernelDiff, 0, Sizeof.cl_mem, Pointer.to(pixelMem));
            clSetKernelArg(kernelDiff, 1, Sizeof.cl_mem, Pointer.to(gradient));
            clSetKernelArg(kernelDiff, 2, Sizeof.cl_mem, Pointer.to(outputImgCL));
            clSetKernelArg(kernelDiff, 3, Sizeof.cl_int, Pointer.to(new int[]{sizeX}));
            clSetKernelArg(kernelDiff, 4, Sizeof.cl_int, Pointer.to(new int[]{sizeY}));
            clSetKernelArg(kernelDiff, 5, Sizeof.cl_float, Pointer.to(new float[]{K}));
            clSetKernelArg(kernelDiff, 6, Sizeof.cl_float, Pointer.to(new float[]{D0}));






            clEnqueueNDRangeKernel(commandQueue, kernelGrad, 2, null,
                    globalWorkSize, null, 0, null, null);
            clEnqueueNDRangeKernel(commandQueue, kernelDiff, 2, null,
                    globalWorkSize, null, 0, null, null);

            clEnqueueReadBuffer(commandQueue, outputImgCL, CL_TRUE, 0,
                    Sizeof.cl_char * sizeY * sizeX, Pointer.to(buf), 0, null, null);
        }


       // byte[] arr = new byte[sizeX*sizeY];

//        clEnqueueReadBuffer(commandQueue, outputImgCL, CL_TRUE, 0,
//                Sizeof.cl_char * sizeY * sizeX, Pointer.to(arr), 0, null, null);


//
//
//        for (int i = 0; i < 25; i++) {
//            for (int j = 0; j <25; j++){
//                System.out.println(arr[i*sizeX + j]);
//            }
//        }

        ByteArrayInputStream bbis =new ByteArrayInputStream(buf);
        System.out.println("bytebufferinputstream " + bbis.available());

//        this.outImg =  new Image(bbis);
        if (outImg ==null) {
            System.out.println("null");
        }

        final byte[] h = ((DataBufferByte)outImg.getRaster().getDataBuffer()).getData();
        System.arraycopy(buf, 0, h, 0, buf.length);
        System.out.println(" sth: " + outImg.getHeight() + " " + outImg.getWidth());
       // saveToFile(outImg);
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j <25; j++){
                //outImg.getRaster().g;
            }
        }
        return outImg;


//        return new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_BYTE_GRAY);



    }


    private void initCL() {
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_GPU;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        commandQueue =
                clCreateCommandQueue(context, device, 0, null);

        // Program Setup


        // Create the program
        cl_program cpProgram1 = clCreateProgramWithSource(context, 1,
                new String[]{kernelSourceGrad}, null, null);

        // Build the program
        clBuildProgram(cpProgram1, 0, null, "-cl-mad-enable", null, null);

        // Create the kernelDiff
        kernelGrad = clCreateKernel(cpProgram1, "Gradient", null);

        // Create the program
        cl_program cpProgram = clCreateProgramWithSource(context, 1,
                new String[]{kernelSourceDiff}, null, null);

        // Build the program
        clBuildProgram(cpProgram, 0, null, "-cl-mad-enable", null, null);

        // Create the kernelDiff
        kernelDiff = clCreateKernel(cpProgram, "AnisotropicDiffusion", null);

        // Create the memory object which will be filled with the
        // pixel data
        pixelMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
                sizeX * sizeY * Sizeof.cl_char, null, null);
        gradient = clCreateBuffer(context, CL_MEM_READ_WRITE, sizeX*sizeY*Sizeof.cl_float, null, null);

        this.outputImgCL = clCreateBuffer(context, CL_MEM_READ_WRITE, sizeX * sizeY * Sizeof.cl_char, null, null);

    }

    /**
     * Helper function which reads the file with the given name and returns
     * the contents of this file as a String. Will exit the application
     * if the file can not be read.
     *
     * @param fileName The name of the file to read.
     * @return The contents of the file
     */
    private String readFile(String fileName) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileName)));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }


    private static class WPF extends ByteBgra {

    }
}
