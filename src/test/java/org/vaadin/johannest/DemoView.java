package org.vaadin.johannest;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import javafx.util.Pair;
import org.vaadin.johannest.tf.Base64Consumer;
import org.vaadin.johannest.tf.ImageCapture;
import org.vaadin.johannest.tf.TensorFlowPredictor;

import java.util.ArrayList;
import java.util.List;

@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@StyleSheet("context://css/main.css")
@Route("")
public class DemoView extends Div implements PageConfigurator {
    private Grid<ResultSetOfThree> grid = null;
    private List<ResultSetOfThree> resultsList = new ArrayList<>();

    public DemoView() {
        H2 title = new H2("Vaadin Platform Tensorflow Demo");
        title.addClassName("main-layout__title");

        add(title);

        addClassName("main-layout");

        ImageCapture imageCapture = new ImageCapture(new Base64Consumer());
        TensorFlowPredictor predictor = new TensorFlowPredictor();
        imageCapture.addOnImageListener(imageBytes -> {
            List<Pair<String, Float>> results = predictor.predictClass(imageBytes,3);
            Notification.show("Predicted class: "+results.get(0).getKey()+" prob: "+results.get(0).getValue());
            resultsList.add(new ResultSetOfThree(results));
            grid.setItems(resultsList);
        });

        add(imageCapture);
        add(new Button("Capture now", e -> imageCapture.capture()));
        add(grid = createResultGrid());
    }

    private Grid createResultGrid() {
        Grid<ResultSetOfThree> grid = new Grid<>();
        grid.addColumn(ResultSetOfThree::getClass1).setHeader("Predicted class 1");
        grid.addColumn(ResultSetOfThree::getP1).setHeader("p1");
        grid.addColumn(ResultSetOfThree::getClass2).setHeader("Predicted class 2");
        grid.addColumn(ResultSetOfThree::getP2).setHeader("p2");
        grid.addColumn(ResultSetOfThree::getClass3).setHeader("Predicted class 3");
        grid.addColumn(ResultSetOfThree::getP3).setHeader("p3");
        grid.setItems(resultsList);
        return grid;
    }

    @Override
    public void configurePage(InitialPageSettings settings) {
        settings.addMetaTag("apple-mobile-web-app-capable", "yes");
        settings.addMetaTag("apple-mobile-web-app-status-bar-style", "black");
    }

    class ResultSetOfThree {

        private String class1;
        private Float p1;
        private String class2;
        private Float p2;
        private String class3;
        private Float p3;

        public ResultSetOfThree(List<Pair<String,Float>> results) {
            class1 = results.get(0).getKey();
            p1 = results.get(0).getValue();
            class2 = results.get(1).getKey();
            p2 = results.get(1).getValue();
            class3 = results.get(2).getKey();
            p3 = results.get(2).getValue();
        }

        public String getClass1() {
            return class1;
        }

        public void setClass1(String class1) {
            this.class1 = class1;
        }

        public Float getP1() {
            return p1;
        }

        public void setP1(Float p1) {
            this.p1 = p1;
        }

        public String getClass2() {
            return class2;
        }

        public void setClass2(String class2) {
            this.class2 = class2;
        }

        public Float getP2() {
            return p2;
        }

        public void setP2(Float p2) {
            this.p2 = p2;
        }

        public String getClass3() {
            return class3;
        }

        public void setClass3(String class3) {
            this.class3 = class3;
        }

        public Float getP3() {
            return p3;
        }

        public void setP3(Float p3) {
            this.p3 = p3;
        }
    }
}

