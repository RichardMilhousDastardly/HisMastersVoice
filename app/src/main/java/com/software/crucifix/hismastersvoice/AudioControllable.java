package com.software.crucifix.hismastersvoice;

import java.util.Map;

/**
 * Created by MUTTLEY on 17/06/2016.
 */
public interface AudioControllable {

    public void play();

    public void pause();

    public void stop();

    public void next();

    public void previous();

    public void repeat();

    public void repeatOnce();

    public void shuffle();

    public boolean manageAudioFocus();

    public boolean manageAbandonAudioFocus();

    public void setSongList(final Map<TooonageVO, String> songList);

}
