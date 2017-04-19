package demo;

import com.jogamp.common.nio.ByteBufferInputStream;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.Gaussian;
import viewer.JavaFX2DDisplay;

import java.nio.ByteBuffer;

/**
 * Created by dibrov on 12/04/17.
 */
public class SB {
    public static void main(String[] args) {


        JavaFX2DDisplay d = new JavaFX2DDisplay();

        byte[] arr = new byte[532*508];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte)255;
        }
        ByteBuffer newbuf = ByteBuffer.wrap(arr);


        new JFXPanel();
        Platform.runLater(new Runnable() {
            public void run() {


                try {
                    Stage s = new Stage();
                    d.start(s);



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("sth");
        d.setBuf(newbuf);

    }
}
