package com.zhuchao.android.video;

import com.zhuchao.android.playerutil.OPlayer;
import com.zhuchao.android.playerutil.PlayerManager;

public class OMediaX extends OMedia{
    public OMediaX(String url) {
        super(url);
    }

    @Override
    public OPlayer getOPlayer() {
        if (FPlayer == null)
            FPlayer = PlayerManager.getMultiOPlayer(context, options, this);
        return (OPlayer) FPlayer;
    }
}