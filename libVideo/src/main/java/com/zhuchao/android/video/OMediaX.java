package com.zhuchao.android.video;

import com.zhuchao.android.playerutil.OPlayer;
import com.zhuchao.android.playerutil.Player;

public class OMediaX extends OMedia{
    public OMediaX(String url) {
        super(url);
    }

    @Override
    public OPlayer getOPlayer() {
        if (FOPlayer == null)
            FOPlayer = Player.getMultiOPlayer(context, options, this);
        return FOPlayer;
    }
}
