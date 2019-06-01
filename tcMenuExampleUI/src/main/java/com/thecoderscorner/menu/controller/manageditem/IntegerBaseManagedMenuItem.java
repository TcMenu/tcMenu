/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

public abstract class IntegerBaseManagedMenuItem<I extends MenuItem> extends BaseLabelledManagedMenuItem<Integer, I> {
    private static final int REDUCE = -1;
    private static final int INCREASE = 1;
    private Button minusButton = new Button();
    private Button plusButton = new Button();
    private long lastRepeatStart = 0;

    enum RepeatTypes { REPEAT_NONE, REPEAT_UP, REPEAT_DOWN, REPEAT_UP_WAIT, REPEAT_DOWN_WAIT }
    private RepeatTypes repeating;


    public IntegerBaseManagedMenuItem(I item) {
        super(item);
    }

    /**
     * For integer items, we put controls on the form to increase and decrease the value using delta
     * value change messages back to the server. Notice we don't change the tree locally, rather we wait
     * for the menu to respond to the change.
     */
    @Override
    public Node createNodes(RemoteMenuController menuController) {
        itemLabel = new Label();
        itemLabel.setPadding(new Insets(3, 0, 3, 0));

        minusButton = new Button("<");
        plusButton = new Button(">");
        minusButton.setDisable(item.isReadOnly());
        plusButton.setDisable(item.isReadOnly());

        minusButton.setOnAction(e-> {
            if(waitingFor.isPresent()) return;
            waitingFor = Optional.of(menuController.sendDeltaUpdate(item, REDUCE));
        });

        minusButton.setOnMousePressed(e-> {
            repeating = RepeatTypes.REPEAT_DOWN_WAIT;
            lastRepeatStart = System.currentTimeMillis();
        });
        minusButton.setOnMouseReleased(e-> repeating = RepeatTypes.REPEAT_NONE);
        plusButton.setOnMousePressed(e-> {
            repeating = RepeatTypes.REPEAT_UP_WAIT;
            lastRepeatStart = System.currentTimeMillis();
        });
        plusButton.setOnMouseReleased(e-> repeating = RepeatTypes.REPEAT_NONE);
        plusButton.setOnAction(e-> {
            if(waitingFor.isPresent()) return;
            waitingFor = Optional.of(menuController.sendDeltaUpdate(item, INCREASE));
        });

        var border = new BorderPane();
        border.setLeft(minusButton);
        border.setRight(plusButton);
        border.setCenter(itemLabel);
        return border;
    }

    @Override
    public boolean isAnimating() {
        return super.isAnimating() || repeating != RepeatTypes.REPEAT_NONE;
    }

    @Override
    public void internalTick() {
        super.internalTick();

        if(repeating == RepeatTypes.REPEAT_DOWN_WAIT && (System.currentTimeMillis() - lastRepeatStart) > 500 ) {
            repeating = RepeatTypes.REPEAT_DOWN;
        }

        if(repeating == RepeatTypes.REPEAT_UP_WAIT && (System.currentTimeMillis() - lastRepeatStart) > 500 ) {
            repeating = RepeatTypes.REPEAT_UP;
        }

        if(repeating == RepeatTypes.REPEAT_UP) {
            Platform.runLater(()->plusButton.fire());
        }
        else if(repeating == RepeatTypes.REPEAT_DOWN) {
            Platform.runLater(()->minusButton.fire());
        }
    }
}
