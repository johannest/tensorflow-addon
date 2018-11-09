/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.johannest.tf;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;

import java.util.ArrayList;
import java.util.List;

/**
 * The main layout contains the header with the navigation buttons, and the
 * child views below that.
 */
@JavaScript("context://tf/js/camSupport.js")
public class ImageCapture extends Div {

    private Base64Consumer imageConsumer;

    private List<OnImageListener> onImageListeners = new ArrayList<>(1);

    public ImageCapture() {
        add(new Html("<div id='jsQRCamLoadingMessage'></div>"));
        add(new Html("<canvas id='jsQRCamCanvas' hidden style='width:100%'></canvas>"));
    }

    public ImageCapture(Base64Consumer imageConsumer) {
        this();
        this.imageConsumer = imageConsumer;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        attachEvent.getUI().getPage().executeJavaScript("jsQRCam.init()");
    }

    public void reset() {
        getUI().ifPresent(ui -> ui.getPage().executeJavaScript("jsQRCam.reset()"));
    }

    public void capture() {
        getUI().ifPresent(ui -> ui.getPage().executeJavaScript("jsQRCam.capture()"));
    }

    @ClientCallable
    public void onImage(String base64Image) {
        if (imageConsumer != null) {
            imageConsumer.extractBytes(base64Image);
            onImageListeners.forEach(l -> l.onImage(imageConsumer.getBytes()));
        }
    }

    public void addOnImageListener(OnImageListener onImageListener) {
        onImageListeners.add(onImageListener);
    }

    public void removeOnImageListener(OnImageListener onImageListener) {
        onImageListeners.remove(onImageListener);
    }
}
