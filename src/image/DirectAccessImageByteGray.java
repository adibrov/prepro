package image;

import javafx.beans.NamedArg;
import javafx.scene.image.WritableImage;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Created by dibrov on 19/04/17.
 */
public class DirectAccessImageByteGray extends WritableImage{

    private ByteBuffer buffer;

    public DirectAccessImageByteGray(@NamedArg("width") int width, @NamedArg("height") int height) {
        super(width, height);

        try {
            Method init = this.getClass().getSuperclass().getSuperclass().getDeclaredMethod("initialize", Object.class);
            init.setAccessible(true);
            buffer = ByteBuffer.allocate(width*height);
            com.sun.prism.Image im = com.sun.prism.Image.fromByteGrayData(buffer,532,508);
            init.invoke(this,im);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void copyBuffer(ByteBuffer extBuffer) throws Exception {
        if (buffer.array().length != extBuffer.array().length) {
            throw new Exception(String.format("Buffer sizes don't match. Native buffer size: %d, provided buffer size: %d",
                    buffer.array().length, extBuffer.array().length));
        }

        buffer.rewind();
        extBuffer.rewind();
        buffer.put(extBuffer);
    }

    public static void main(String[] args) {
        int w = 500;
        int h = 500;
        DirectAccessImageByteGray testImg = new DirectAccessImageByteGray(w,h);

        // let's see what's in the native buffer...
        ByteBuffer nativeBuffer = testImg.getBuffer();
        System.out.println("Old buffer content:");
        for (int i = 0; i< 10; i++) {
            System.out.println(String.format("nativeBuff[%d] = %d",i,nativeBuffer.array()[i]));
        }

        System.out.println();

        // let's change the buffer...
        ByteBuffer b = ByteBuffer.allocate(w*h);
        for (int i = 0; i<b.array().length; i++) {
            b.put((byte)10);
        }
        try {
            testImg.copyBuffer(b);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("New buffer content:");
        // let's see what's in the new buffer
        for (int i = 0; i< 10; i++) {
            System.out.println(String.format("nativeBuff[%d] = %d",i,nativeBuffer.array()[i]));
        }
    }
}
