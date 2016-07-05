package com.software.crucifix.hismastersvoice;

import com.software.crucifix.hismastersvoice.word.processing.Utility;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Created by MUTTLEY on 28/06/2016.
 */
public class TooonageVO implements Comparable<TooonageVO> {

    private static final Utility mUtility = new Utility();
    private static final String SPACE = " ";

    private final String artist;
    private final String albumn;
    private final String song;

    private TooonageVO(final String artist, final String albumn, final String song) {
        this.artist = artist;
        this.albumn = albumn;
        this.song = song;
    }

    private TooonageVO(final Builder builder) {
        this.artist = builder.artist;
        this.albumn = builder.albumn;
        this.song = builder.song;
    }

    public String getArtist() {
        return WordUtils.capitalizeFully(mUtility.cleanseText(artist, SPACE));
    }

    public String getAlbumn() {
        return WordUtils.capitalizeFully(mUtility.cleanseText(albumn, SPACE));
    }

    public String getSong() {
        return WordUtils.capitalizeFully(mUtility.cleanseText(song, SPACE));
    }


    /**
     * B U I L D E R
     */

    public static class Builder {

        private String artist;
        private String albumn;
        private String song;

        public Builder(final String artist, final String albumn, final String song) {
            this.artist = artist;
            this.albumn = albumn;
            this.song = song;
        }

        public Builder() {
        }

        public Builder artist(final String artist) {
            this.artist = artist;
            return this;
        }

        public Builder albumn(final String albumn) {
            this.albumn = albumn;
            return this;
        }

        public Builder song(final String song) {
            this.song = song;
            return this;
        }

        public TooonageVO build() {
            return new TooonageVO(this);
        }

    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TooonageVO that = (TooonageVO) o;

        if (!getArtist().equals(that.getArtist())) return false;
        if (!getAlbumn().equals(that.getAlbumn())) return false;
        return getSong().equals(that.getSong());

    }

    @Override
    public int hashCode() {
        int result = getArtist().hashCode();
        result = 31 * result + getAlbumn().hashCode();
        result = 31 * result + getSong().hashCode();
        return result;
    }

    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(final TooonageVO another) {

        final String thisOne = this.artist.trim() + this.albumn.trim() + this.song.trim();
        final String thatOne = another.artist.trim() + another.albumn.trim() + another.song.trim();

        return thisOne.compareTo(thatOne);
    }
}
