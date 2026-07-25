package com.google.zxing.client.result;

public class SpotifyParsedResult extends ParsedResult {
    private String mArtist;
    private String mSong;

    protected SpotifyParsedResult(String artist, String song) {
        super(ParsedResultType.SPOTIFY);

        mArtist = artist;
        mSong = song;
    }

    @Override
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(50);
        maybeAppend(mArtist, result);
        maybeAppend(mSong, result);
        return result.toString();
    }

    public String getArtist() {
        return mArtist;
    }

    public String getSong() {
        return mSong;
    }

    @Override
    public String revertRawData() {
        return String.format("spotify:search:%s;%s", mArtist, mSong);
    }

    public static ParsedResult build(String artist, String song) {
        return new SpotifyParsedResult(artist, song);
    }
}


