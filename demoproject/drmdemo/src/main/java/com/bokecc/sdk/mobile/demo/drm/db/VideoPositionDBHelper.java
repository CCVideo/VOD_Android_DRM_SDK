package com.bokecc.sdk.mobile.demo.drm.db;

import com.bokecc.sdk.mobile.demo.drm.model.VideoPosition;
import com.bokecc.sdk.mobile.demo.drm.model.VideoPosition_;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataSubscription;

public class VideoPositionDBHelper {

    Box<VideoPosition> box;

    public VideoPositionDBHelper(BoxStore boxStore) {
        box = boxStore.boxFor(VideoPosition.class);
    }

    public void insertVideoPosition(String videoId, int position) {
        VideoPosition videoPosition = new VideoPosition(videoId, position);
        box.put(videoPosition);

    }

    public VideoPosition getVideoPosition(String videoId) {
        Query<VideoPosition> query = box.query().equal(VideoPosition_.videoId, videoId).build();
        VideoPosition videoPosition = query.findFirst();
        return videoPosition;
    }

    public void updateVideoPosition(VideoPosition videoPosition) {
        box.put(videoPosition);
    }
}
