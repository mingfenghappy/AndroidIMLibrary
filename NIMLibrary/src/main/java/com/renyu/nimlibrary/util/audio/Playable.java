package com.renyu.nimlibrary.util.audio;

public interface Playable {
    long getDuration();

    String getPath();

    boolean isAudioEqual(Playable audio);
}