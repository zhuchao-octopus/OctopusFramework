package com.zhuchao.android.session;

import android.os.Build;

import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.ObjectList;
import com.zhuchao.android.fbase.bean.AudioMetaFile;
import com.zhuchao.android.fbase.bean.MediaMetadata;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.util.HashMap;
import java.util.List;

public class TMediaMetadataManager {
    public static final String ALBUM_TAG = "media.album.tag.";
    public static final String ARTIST_TAG = "media.artist.tag.";

    public VideoList mVideoList = new VideoList("MediaMetadataManager");
    private final ObjectList mMediaMetaDatas = new ObjectList();

    public void clear() {
        mMediaMetaDatas.clear();
        mVideoList.clear();
    }

    public List<MediaMetadata> getAlbums() {
        return mMediaMetaDatas.toListLike(ALBUM_TAG);
    }

    public <T> List<T> getTAlbums() {
        return mMediaMetaDatas.toListLike(ALBUM_TAG);
    }

    public List<MediaMetadata> getArtist() {
        return mMediaMetaDatas.toListLike(ARTIST_TAG);
    }

    public <T> List<T> getTArtist() {
        return mMediaMetaDatas.toListLike(ARTIST_TAG);
    }

    public ObjectList getMediaMetaDatas() {
        return mMediaMetaDatas;
    }

    public void updateArtistAndAlbum(VideoList videoList) {
        OMedia oMedia = null;
        for (HashMap.Entry<String, Object> m : videoList.getMap().entrySet()) {
            oMedia = (OMedia) m.getValue();
            ///if (mVideoList.exist(oMedia)) continue;
            if (mVideoList.exist(oMedia.getPathName())) continue;
            mVideoList.addRow(oMedia);
            String aaName = null;///ALBUM_TAG + oMedia.getMovie().getAlbum();
            MediaMetadata mediaMetadata = null;///mediaMetadataList.getValue(aaName);
            String album = oMedia.getMovie().getAlbum();
            String artist = oMedia.getMovie().getArtist();

            if (FileUtils.EmptyString(album)) album = "Unknow";
            if (FileUtils.EmptyString(artist)) artist = "Unknow";

            {
                aaName = ALBUM_TAG + album;
                mediaMetadata = mMediaMetaDatas.getValue(aaName);
                if (mediaMetadata == null) mediaMetadata = new MediaMetadata();
                mediaMetadata.setAlbum(album);
                mediaMetadata.setArtist(artist);
                mediaMetadata.setId(oMedia.getMovie().getSourceId());
                mediaMetadata.addDescription(oMedia.getMovie().getStudio());
                mediaMetadata.addDescription(oMedia.getMovie().getDescription());
                mediaMetadata.addDescription(oMedia.getMovie().getActor());
                mediaMetadata.addCount();
                if ((mediaMetadata.getBitmap() == null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
                    mediaMetadata.setBitmap(AudioMetaFile.getAlbumArtPicture(oMedia.getPathName()));
                }
                mMediaMetaDatas.addObject(aaName, mediaMetadata);
            }

            {
                aaName = ARTIST_TAG + artist;
                mediaMetadata = mMediaMetaDatas.getValue(aaName);
                if (mediaMetadata == null) mediaMetadata = new MediaMetadata();
                mediaMetadata.setAlbum(album);
                mediaMetadata.setArtist(artist);
                mediaMetadata.setId(oMedia.getMovie().getSourceId());
                mediaMetadata.addDescription(oMedia.getMovie().getStudio());
                mediaMetadata.addDescription(oMedia.getMovie().getDescription());
                mediaMetadata.addDescription(oMedia.getMovie().getActor());
                mediaMetadata.addCount();
                mMediaMetaDatas.addObject(aaName, mediaMetadata);
            }
        }
    }

    public void free() {
        mMediaMetaDatas.clear();
        mVideoList.clear();
    }

}
