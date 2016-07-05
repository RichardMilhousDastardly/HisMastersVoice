package com.software.crucifix.hismastersvoice;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;

/**
 * Created by MUTTLEY on 21/06/2016.
 */
public class HMVFloatingActionButton extends FloatingActionButton {

    public static final int POSITION_CENTER_CENTER = 9;

    public HMVFloatingActionButton(final Activity activity, final LayoutParams layoutParams, final int theme, final Drawable backgroundDrawable, final int position, final View contentView, final FrameLayout.LayoutParams contentParams) {
        super(activity, layoutParams, theme, backgroundDrawable, position, contentView, contentParams);
    }


    @Override
    public void setPosition(final int position, final FrameLayout.LayoutParams layoutParams) {

        byte gravity;
        switch (position) {
            case 1:
                gravity = 49;
                break;
            case 2:
                gravity = 53;
                break;
            case 3:
                gravity = 21;
                break;
            case 4:
            default:
                gravity = 85;
                break;
            case 5:
                gravity = 81;
                break;
            case 6:
                gravity = 83;
                break;
            case 7:
                gravity = 19;
                break;
            case 8:
                gravity = 51;
                break;
            case 9:
                gravity = Gravity.CENTER;
        }

        layoutParams.gravity = gravity;
        this.setLayoutParams(layoutParams);
    }

    public static class Builder {
        private Activity activity;
        private FloatingActionButton.LayoutParams layoutParams;
        private int theme;
        private Drawable backgroundDrawable;
        private int position;
        private View contentView;
        private FloatingActionButton.LayoutParams contentParams;

        public Builder(Activity activity) {
            this.activity = activity;
            int size = activity.getResources().getDimensionPixelSize(com.oguzdev.circularfloatingactionmenu.library.R.dimen.action_button_size);
            int margin = activity.getResources().getDimensionPixelSize(com.oguzdev.circularfloatingactionmenu.library.R.dimen.action_button_margin);
            FloatingActionButton.LayoutParams layoutParams = new FloatingActionButton.LayoutParams(size, size, 85);
            layoutParams.setMargins(margin, margin, margin, margin);
            this.setLayoutParams(layoutParams);
            this.setTheme(0);
            this.setPosition(4);
        }

        public HMVFloatingActionButton.Builder setLayoutParams(FloatingActionButton.LayoutParams params) {
            this.layoutParams = params;
            return this;
        }

        public HMVFloatingActionButton.Builder setTheme(int theme) {
            this.theme = theme;
            return this;
        }

        public HMVFloatingActionButton.Builder setBackgroundDrawable(Drawable backgroundDrawable) {
            this.backgroundDrawable = backgroundDrawable;
            return this;
        }

        public HMVFloatingActionButton.Builder setBackgroundDrawable(int drawableId) {
            return this.setBackgroundDrawable(this.activity.getResources().getDrawable(drawableId));
        }

        public HMVFloatingActionButton.Builder setPosition(int position) {
            this.position = position;
            return this;
        }

        public HMVFloatingActionButton.Builder setContentView(View contentView) {
            return this.setContentView(contentView, (HMVFloatingActionButton.LayoutParams) null);
        }

        public HMVFloatingActionButton.Builder setContentView(View contentView, HMVFloatingActionButton.LayoutParams contentParams) {
            this.contentView = contentView;
            this.contentParams = contentParams;
            return this;
        }

        public HMVFloatingActionButton build() {
            return new HMVFloatingActionButton(this.activity, this.layoutParams, this.theme, this.backgroundDrawable, this.position, this.contentView, this.contentParams);
        }
    }

}
