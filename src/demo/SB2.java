package demo;

import com.jogamp.common.nio.ByteBufferInputStream;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import viewer.JavaFX2DSimpleDisplay;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dibrov on 14/04/17.
 */
public class SB2 {

    public static void main(String[] args) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("resources/img/wing8bit_crop.png"));
            byte[] arr = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
            System.out.println("im size: " + img.getHeight() + " " + img.getWidth());
            System.out.println("arr size: " + arr.length);
            System.out.println("cm: " + img.getColorModel());


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)img, "png", baos);
            baos.flush();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            System.out.println("bbis: " + bais.available());

            Image imfx = new Image(bais);
            System.out.println("error?: " + imfx.errorProperty());
            System.out.println("progress?: " + imfx.progressProperty());


            System.out.println("imfx size: " + imfx.getHeight() + " " + imfx.getWidth());

            new JFXPanel();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Stage s = new Stage();
                    new JavaFX2DSimpleDisplay(imfx).start(s);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
