package com.software.crucifix.hismastersvoice.listeners;

import android.view.View;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;

/**
 * Created by MUTTLEY on 17/06/2016.
 */
public class BaseOnClickListener implements View.OnClickListener {

    private final FloatingActionButton mFloatingActionButton;
    private final static boolean manualToggle = true;


    public BaseOnClickListener(final FloatingActionButton floatingActionButton) {
        this.mFloatingActionButton = floatingActionButton;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(final View view) {

        if (manualToggle) {
        } else {
            mFloatingActionButton.performClick();
        }
    }
}
