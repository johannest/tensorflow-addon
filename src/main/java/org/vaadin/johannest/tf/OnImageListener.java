package org.vaadin.johannest.tf;

import java.util.EventListener;

@FunctionalInterface
public interface OnImageListener extends EventListener {

    void onImage(byte[] imageButes);

}
