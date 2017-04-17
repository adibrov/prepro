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





        new JFXPanel();
        Platform.runLater(new Runnable() {
            public void run() {
                JavaFX2DDisplay d = new JavaFX2DDisplay();
                Stage s = new Stage();
                try {
                    d.start(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("sth");
    }
}
