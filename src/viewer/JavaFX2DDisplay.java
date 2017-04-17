package viewer;

import com.jogamp.common.nio.ByteBufferInputStream;
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

import java.nio.ByteBuffer;

/**
 * Created by dibrov on 12/04/17.
 */
public class JavaFX2DDisplay extends Application {
    ImageView imageView_Source, imageView_Target;
    Image imgIn, imgOut;
    Gaussian gaussian;
    Slider sliderContrast, sliderHue, sliderBrightness, sliderSaturation;

    public JavaFX2DDisplay() {
        this.imgIn = new Image("/img/wing8bit_crop.png");
        byte[] arr = new byte[(int)(imgIn.getHeight()*imgIn.getWidth()*4)];
        this.imgOut = new Image(new ByteBufferInputStream(ByteBuffer.wrap(arr)));
        System.out.println("w h: "  + imgIn.getWidth() + " " + imgIn.getHeight());
//        this.imgOut = new Image("/img/wing8bit_crop.png");
//        this.gaussian = new Gaussian(imgIn, (int)imgIn.getHeight(), (int)imgIn.getWidth(),
//                "resources/kernels/Gaussian" +
//                        ".cl", 3,3, 1.5f, 1.5f);
    }

    public void setImgOut(Image imgOut) {
        this.imgOut = imgOut;
    }

    @Override
    public void start(Stage primaryStage) {


        imageView_Source = new ImageView();
        imageView_Source.setImage(imgIn);
        imgIn.getPixelReader().getPixelFormat();
        WritableImage wi = new WritableImage(2,2);
        int x = (int) imgIn.getHeight();
        int y = (int) imgIn.getWidth();
      //  wi.getPixelWriter().setPixels(0,0,x,y,imgIn.getPixelReader().getPixelFormat(), new int[5],0,0);


        imageView_Target = new ImageView();
        imageView_Target.setImage(imgOut);

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
        primaryStage.showAndWait();

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
