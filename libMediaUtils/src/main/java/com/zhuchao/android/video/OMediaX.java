package com.zhuchao.android.video;

import com.zhuchao.android.player.OPlayer;
import com.zhuchao.android.player.PlayerManager;

public class OMediaX extends OMedia {
    public OMediaX(String url) {
        super(url);
    }

    @Override
    public OPlayer getPlayer() {
        if (FPlayer == null)
            FPlayer = PlayerManager.getMultiOPlayer(context, options, this);
        return (OPlayer) FPlayer;
    }
}
