package com.rockchip.car.recorder.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;

/**
 * Created by Administrator on 2016/8/12.
 */
public class PathView extends View {
    private Path p=new Path();

    Region re=new Region();

    public PathView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        //起始顶点，绘制的方向为左下右上。
        p.moveTo(100, 100);
        p.lineTo(100, 150);
        //绘制一个贝塞尔曲线，第一个顶点是来控制曲线的弧度和方向，第二个顶点是弧线的结束顶点。
        p.quadTo(230, 150, 300, 200);
        p.quadTo(200, 150, 300, 200);
        p.quadTo(200, 120, 150, 100);
        p.close();
        //构造一个区域对象，左闭右开的。
        RectF r=new RectF();
        //计算控制点的边界
        p.computeBounds(r, true);
        //设置区域路径和剪辑描述的区域
        re.setPath(p, new Region((int)r.left,(int)r.top,(int)r.right,(int)r.bottom));
        Paint paint=new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        //设置抗锯齿。
        paint.setAntiAlias(true);
        //paint.setFilterBitmap(true);
        canvas.drawPath(p, paint);
    }
}
