import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javafx.application.Application;
import javafx.beans.binding.StringBinding;
import javafx.scene.Scene;
import javafx.scene.control.*;
//import javafx.scene.control.Button;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.FileChooser;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main extends Application {
    MyData data;
    BorderPane root;
//    Pane sankeyGraphArea;
    SankeyPane sankeyGraphPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        root = new BorderPane();
        Scene scene = new Scene(root, 700, 420);

        // # menu and menu items
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        MenuItem menuFileItemOpen = new MenuItem("Open");
        menuFileItemOpen.setOnAction(actionEvent -> {
            open_file(stage);
        });

        menuFile.getItems().add(menuFileItemOpen);

        menuBar.getMenus().addAll(menuFile);

        root.setTop(menuBar);
        // ~ menu and menu items

        // # sankey graph pane
        sankeyGraphPane = new SankeyPane();
        root.setCenter(sankeyGraphPane);

        // ~ sankey graph pane

        stage.setScene(scene);
        stage.setTitle("Sankey");

        stage.show();
    }

    private void open_file(final Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File("F:/CODE/PTJ/1219_3_java_gui"));
        File file = fileChooser.showOpenDialog(primaryStage);
        data = new MyData(file);
        sankeyGraphPane.display_data(data);
    }
}


class MyData {
    private HashMap<String, Double> hashMap;
    private String title, srcLabel;
    private double total = 0.0;
    private int streamCount = 0;
    private boolean isValid = true;

    public double getTotal() {
        return total;
    }

    public int getStreamCount() {
        return streamCount;
    }

    public String getTitle() {
        return title;
    }

    public String getSrcLabel() {
        return srcLabel;
    }

    public boolean is_valid() {
        return isValid;
    }

    public Iterator<Map.Entry<String, Double>> streams() {
        return hashMap.entrySet().iterator();
    }

    public MyData(File file) {
        hashMap = new HashMap<String, Double>();
        String line;
        int lineCount = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            while ((line = reader.readLine()) != null) {
                if (lineCount == 0) {
                    title = line;
                } else if (lineCount == 1) {
                    srcLabel = line;
                } else {
                    String[] kv = line.split(" ");
                    if (kv.length > 2) {
                        for (int i = 1; i < kv.length - 1; i++) {
                            kv[0] += " ";
                            kv[0] += kv[i];
                        }
                    }
                    hashMap.put(kv[0], Double.parseDouble(kv[kv.length - 1]));
                    total += Double.parseDouble(kv[kv.length - 1]);
                    streamCount++;
                }
                lineCount++;
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            isValid = false;
        }
    }
}

class SankeyPane extends Pane{
    private String colors[]= {
            "8ECFC9",
            "FFBE7A",
            "FA7F6F",
            "82B0D2",
            "BEB8DC",
            "E7DAD2",
            "999999",
            "C497B2",
            "2F7FC1",
            "EF7A6D",
            "A1A9D0"
    };

    public void display_data(MyData data) {
        getChildren().clear();

        SVGPath srcRect = new SVGPath();
        srcRect.contentProperty().bind(svgOfRect(0.14, 0.15, 0.14, 0.70));
        srcRect.setFill(Color.rgb(88, 173, 218,1.0));

        Text srcText=new Text(data.getSrcLabel()+": "+Double.valueOf(data.getTotal()));
        srcText.setFont(Font.font("Consolas"));
        srcText.setTextAlignment(TextAlignment.CENTER);
        srcText.xProperty().bind(widthProperty().multiply(0.15));
        srcText.yProperty().bind(heightProperty().multiply(0.5));

        getChildren().add(srcRect);
        getChildren().add(srcText);

        double gapFactor = 0.3 / Double.valueOf(data.getStreamCount() + 1);
        double biasLeft = 0.15, biasRight = gapFactor;

        int streamcount=0;
        Iterator<Map.Entry<String, Double>> streams = data.streams();
        while (streams.hasNext()) {
            Map.Entry<String, Double> kv = streams.next();
//            System.out.println("Key = " + kv.getKey() + ", Value = " + kv.getValue());

            final double hFactor = 0.7 * kv.getValue() / data.getTotal();

            SVGPath tempRect = new SVGPath();
            tempRect.contentProperty().bind(svgOfRect(0.72, biasRight, 0.14, hFactor));
            tempRect.setFill(Color.valueOf(colors[streamcount%11]));

            SVGPath bezierCurve = new SVGPath();
            bezierCurve.contentProperty().bind(svgOfBezierCurve(0.28, biasLeft + 0.5 * hFactor, 0.72, biasRight + 0.5 * hFactor, hFactor));
            bezierCurve.setFill(Color.valueOf(colors[streamcount%11]+"C4"));

            Text text=new Text(kv.getKey()+": "+kv.getValue().intValue());
            text.setFont(Font.font("Consolas"));
            text.setTextAlignment(TextAlignment.CENTER);
            text.xProperty().bind(widthProperty().multiply(0.73));
            text.yProperty().bind(heightProperty().multiply(biasRight+0.49*hFactor));

            getChildren().add(tempRect);
            getChildren().add(bezierCurve);
            getChildren().add(text);

            biasLeft += hFactor;
            biasRight += gapFactor;
            biasRight += hFactor;

            streamcount++;
        }
    }

    private StringBinding svgOfRect(double xFactor, double yFactor, double wFactor, double hFactor) {
        StringBinding strBnd = new StringBinding() {
            {
                super.bind(widthProperty(), heightProperty());
            }

            @Override
            protected String computeValue() {
                // "M x,y L x,y L x,y L x,y Z"

                String str = "M ";
                str += widthProperty().multiply(xFactor).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yFactor).asString().getValue();
                str += " L ";
                str += widthProperty().multiply(xFactor + wFactor).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yFactor).asString().getValue();
                str += " L ";
                str += widthProperty().multiply(xFactor + wFactor).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yFactor + hFactor).asString().getValue();
                str += " L ";
                str += widthProperty().multiply(xFactor).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yFactor + hFactor).asString().getValue();
                str += " Z";
                return str;
            }
        };

        return strBnd;
    }

    private StringBinding svgOfBezierCurve(double xStartFactor, double yStartFactor, double xEndFactor, double yEndFactor, double spanFactor) {
        StringBinding strBnd = new StringBinding() {
            {
                super.bind(widthProperty(), heightProperty());
            }

            @Override
            protected String computeValue() {
                // "
                // M x,y
                // C x1,y1,x2,y2,x,y
                // L x,y
                // C x1,y1,x2,y2,x,y
                // Z"

                String str = "M ";
                str += widthProperty().multiply(xStartFactor).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yStartFactor - 0.5 * spanFactor).asString().getValue();
                str += " C ";
                str += widthProperty().multiply(0.5 * (xStartFactor + xEndFactor)).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yStartFactor - 0.5 * spanFactor).asString().getValue();
                str += ",";
                str += widthProperty().multiply(0.5 * (xStartFactor + xEndFactor)).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yEndFactor - 0.5 * spanFactor).asString().getValue();
                str += ",";
                str += widthProperty().multiply(xEndFactor).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yEndFactor - 0.5 * spanFactor).asString().getValue();
                str += " L ";
                str += widthProperty().multiply(xEndFactor).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yEndFactor + 0.5 * spanFactor).asString().getValue();
                str += " C ";
                str += widthProperty().multiply(0.5 * (xStartFactor + xEndFactor)).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yEndFactor + 0.5 * spanFactor).asString().getValue();
                str += ",";
                str += widthProperty().multiply(0.5 * (xStartFactor + xEndFactor)).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yStartFactor + 0.5 * spanFactor).asString().getValue();
                str += ",";
                str += widthProperty().multiply(xStartFactor).asString().getValue();
                str += ",";
                str += heightProperty().multiply(yStartFactor + 0.5 * spanFactor).asString().getValue();
                str += " Z";
                return str;
            }
        };

        return strBnd;
    }

}


