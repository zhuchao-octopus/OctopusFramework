package com.zhuchao.android.video;

import com.zhuchao.android.player.PlayerManager;

public class OMediaX extends OMedia {
    public OMediaX(String url) {
        super(url);
    }

    @Override
    public void getPlayer() {
        if (FPlayer == null)
            FPlayer = PlayerManager.getMultiOPlayer(context, options, this);
    }
}
