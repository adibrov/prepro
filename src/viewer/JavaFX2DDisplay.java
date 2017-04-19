package viewer;

import com.jogamp.common.nio.ByteBufferInputStream;
import com.sun.javafx.tk.PlatformImage;
import com.sun.prism.PixelFormat;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.Gaussian;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by dibrov on 12/04/17.
 */
public class JavaFX2DDisplay extends Application {
    private ImageView imageView_Source, imageView_Target;
    private Image imgIn, imgOut;
    private Gaussian gaussian;
    private Slider sliderContrast, sliderHue, sliderBrightness, sliderSaturation;
    private Buffer buf;

    public void setBuf(ByteBuffer buf) {
        try {
            Method m = imgIn.getClass().getSuperclass().getDeclaredMethod("getWritablePlatformImage", null);
            m.setAccessible(true);
            com.sun.prism.Image im = (com.sun.prism.Image)m.invoke(imgIn,null);
            if (im == null) {
                System.out.println("null img");
            }
            Field bufF = im.getClass().getDeclaredField("pixelBuffer");
            bufF.setAccessible(true);
            System.out.println("setting the buffer: " + (buf ==null));
            buf.rewind();
            ByteBuffer ownbuffer = (ByteBuffer)bufF.get(im);

            System.out.println("own: " + ownbuffer.array().length + "; toputbuf: " + buf.array().length);
            ownbuffer.put(buf);
            ownbuffer.rewind();

//            Field pf = im.getClass().getDeclaredField("pixelFormat");
//            pf.setAccessible(true);
//            pf.set(im, PixelFormat.BYTE_GRAY);

            Field serial = im.getClass().getDeclaredField("serial");
            serial.setAccessible(true);
//            System.out.println("setting the buffer: " + (buf ==null));
            int[] h = (int[])serial.get(im);
            h[0]++;
            serial.set(im,h);

            Method mm = imgIn.getClass().getSuperclass().getDeclaredMethod("pixelsDirty", null);
            mm.setAccessible(true);
            mm.invoke(imgIn,null);

            System.out.println("buffer shdvbeen updated");

        } catch (Exception e) {
            System.out.println("boom");
            e.printStackTrace();
        }
    }



    public JavaFX2DDisplay() {

//        this.imgOut = new Image("/img/wing8bit_crop.png");
//        this.gaussian = new Gaussian(imgIn, (int)imgIn.getHeight(), (int)imgIn.getWidth(),
//                "resources/kernels/Gaussian" +
//                        ".cl", 3,3, 1.5f, 1.5f);
    }

//    public void setImgOut(Image imgOut) {
//        this.imgOut = imgOut;
//    }

    @Override
    public void start(Stage primaryStage) {

        this.imgIn = new WritableImage(532,508);
        try {
            Method init = imgIn.getClass().getSuperclass().getDeclaredMethod("initialize", Object.class);
            init.setAccessible(true);

            ByteBuffer bb = ByteBuffer.allocate(532*508);
            com.sun.prism.Image im = com.sun.prism.Image.fromByteGrayData(bb,532,508);
            init.invoke(imgIn,im);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Method m = imgIn.getClass().getSuperclass().getDeclaredMethod("getWritablePlatformImage", null);
            m.setAccessible(true);
            com.sun.prism.Image im = (com.sun.prism.Image)m.invoke(imgIn,null);
            if (im == null) {
                System.out.println("null img");
            }

            imageView_Source = new ImageView();
            imageView_Source.setImage(imgIn);
//            Field pf = im.getClass().getDeclaredField("pixelFormat");
//            pf.setAccessible(true);
//            pf.set(im, PixelFormat.BYTE_GRAY);
        } catch (Exception e) {
            e.printStackTrace();


        }

        byte[] arr = new byte[(int)(imgIn.getHeight()*imgIn.getWidth()*4)];
        this.imgOut = new Image(new ByteBufferInputStream(ByteBuffer.wrap(arr)));
        imageView_Target = new ImageView();
        imageView_Target.setImage(imgOut);
        System.out.println("w h: "  + imgIn.getWidth() + " " + imgIn.getHeight());



        HBox hBoxImage = new HBox();
        hBoxImage.getChildren().addAll(imageView_Source, imageView_Target);


        Button btnProcess = new Button("Blur");
        btnProcess.setOnAction(btnProcessEventListener);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBoxImage,

                btnProcess);

        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root, 1400, 1000);
        primaryStage.setTitle("java-buddy.blogspot.com");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateEffect();

    }


    EventHandler<ActionEvent> btnProcessEventListener
            = new EventHandler<ActionEvent>(){
        public void handle(ActionEvent t) {
            Runnable r = new Runnable() {
                public void run() {
//                    setImgOut(gaussian.convolve());
                    updateEffect();
                }
            };
            Thread th = new Thread(r);
            th.start();
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    };

    private void updateEffect(){
        imageView_Target.setImage(imgOut);
    }

    public void run(String[] args) {
        launch(args);
    }
}
