package viewer;

import com.jogamp.common.nio.ByteBufferInputStream;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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
public class JavaFX2DSimpleDisplay extends Application {
    ImageView imageView_Source;
    Image imgIn;


    public JavaFX2DSimpleDisplay(Image img) {
        this.imgIn = img;
    }
    @Override
    public void start(Stage primaryStage) {


        imageView_Source = new ImageView();
        imageView_Source.setImage(imgIn);

        HBox hBoxImage = new HBox();
        hBoxImage.getChildren().addAll(imageView_Source);



        StackPane root = new StackPane();
        root.getChildren().add(hBoxImage);
        Scene scene = new Scene(root, 1400, 1000);
        primaryStage.setTitle("java-buddy.blogspot.com");
        primaryStage.setScene(scene);
        primaryStage.showAndWait();



    }





    public void run(String[] args) {
        launch(args);
    }
}
