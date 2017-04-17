package main;

import com.jogamp.common.nio.ByteBufferInputStream;
import com.jogamp.nativewindow.util.PixelFormatUtil;
import com.sun.javafx.image.impl.ByteBgra;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import org.jocl.*;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.jocl.CL.*;

/**
 * Created by dibrov on 12/04/17.
 */
public class Gaussian {

    private BufferedImage image;
    private int sizeX;
    private int sizeY;
    private float sigmaX;
    private float sigmaY;
    private int delX;
    private int delY;
    private String kernelSource;
    private cl_context context;
    private cl_command_queue commandQueue;
    private cl_kernel kernel;
    private cl_mem pixelMem;
    private cl_mem colorMapMem;
    private byte[] imageBuffer;
    private BufferedImage outImg;

    private cl_mem outputImgCL;
    ;
    //private cl_mem pY;


    public Gaussian(BufferedImage inputImg, int delX, int delY, float sigmaX,
                    float
            sigmaY) {
        this.sizeY = inputImg.getHeight();
        this.sizeX = inputImg.getWidth();
        this.image = inputImg;
       // this.imageBuffer = new byte[sizeX*sizeY*4];
        this.imageBuffer =((DataBufferByte)(inputImg.getRaster().getDataBuffer())).getData();

//        wi.getPixelWriter().setPixels(1,1,1,1, PixelFormat.createByteIndexedInstance(colors), );


//        Constructor<WritablePixelFormat> constr = null;
//
//        try {
//             constr = WritablePixelFormat.class.getConstructor();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }

//        constr.setAccessible(true)


        outImg = new BufferedImage (sizeX, sizeY, BufferedImage.TYPE_BYTE_GRAY);

        System.out.println("");

        this.sigmaX = sigmaX;
        this.sigmaY = sigmaY;
        this.delX = delX;
        this.delY = delY;
        this.kernelSource = readFile("resources/kernels/Gaussian.cl");


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



        clEnqueueWriteBuffer(commandQueue, pixelMem, true, 0,
                Sizeof.cl_char * sizeY * sizeX, Pointer.to(this.imageBuffer), 0, null, null);


        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(pixelMem));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(outputImgCL));
        clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{sizeX}));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{sizeY}));
        clSetKernelArg(kernel, 4, Sizeof.cl_int, Pointer.to(new int[]{delX}));
        clSetKernelArg(kernel, 5, Sizeof.cl_int, Pointer.to(new int[]{delY}));
        clSetKernelArg(kernel, 6, Sizeof.cl_float, Pointer.to(new float[]{sigmaX}));
        clSetKernelArg(kernel, 7, Sizeof.cl_float, Pointer.to(new float[]{sigmaY}));

        clEnqueueNDRangeKernel(commandQueue, kernel, 2, null,
                globalWorkSize, null, 0, null, null);
        byte[] arr = new byte[sizeX*sizeY];

        clEnqueueReadBuffer(commandQueue, outputImgCL, CL_TRUE, 0,
                Sizeof.cl_char * sizeY * sizeX, Pointer.to(arr), 0, null, null);


//
//
//        for (int i = 0; i < 25; i++) {
//            for (int j = 0; j <25; j++){
//                System.out.println(arr[i*sizeX + j]);
//            }
//        }

        ByteArrayInputStream bbis =new ByteArrayInputStream(arr);
        System.out.println("bytebufferinputstream " + bbis.available());

//        this.outImg =  new Image(bbis);
        if (outImg ==null) {
            System.out.println("null");
        }

        final byte[] h = ((DataBufferByte)outImg.getRaster().getDataBuffer()).getData();
        System.arraycopy(arr, 0, h, 0, arr.length);
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
        cl_program cpProgram = clCreateProgramWithSource(context, 1,
                new String[]{kernelSource}, null, null);

        // Build the program
        clBuildProgram(cpProgram, 0, null, "-cl-mad-enable", null, null);

        // Create the kernel
        kernel = clCreateKernel(cpProgram, "Gaussian", null);

        // Create the memory object which will be filled with the
        // pixel data
        pixelMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
                sizeX * sizeY * Sizeof.cl_char, null, null);

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
