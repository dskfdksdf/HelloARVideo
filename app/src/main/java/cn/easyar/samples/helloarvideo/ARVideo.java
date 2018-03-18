//================================================================================================================================
//
//  Copyright (c) 2015-2018 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarvideo;

import android.util.Log;

import cn.easyar.FunctorOfVoidFromVideoStatus;
import cn.easyar.StorageType;
import cn.easyar.VideoPlayer;
import cn.easyar.VideoStatus;
import cn.easyar.VideoType;

public class ARVideo
{
    private VideoPlayer player;//close pause play 三个方法都调用了
    private boolean prepared;//是否准备好，可以开始播放  初始化为false 打开 status == VideoStatus.Ready 时设置为true
    private boolean found;//是否    初始化为false onFound()设置为true onLost()中设置为false
    private String path;

    public ARVideo()//构造函数中初始化视频播放类对象，将两个boolean设置为false
    {
        player = new VideoPlayer();
        prepared = false;
        found = false;
    }
    public void dispose()//HelloAR.java 三个地方被调用
    {
        player.close();
    }//player关闭

    public void openVideoFile(String path, int texid)//打开视频文件
    {
        this.path = path;//path赋值
        player.setRenderTexture(texid);//传入用来显示视频的texture到播放器
        player.setVideoType(VideoType.Normal);//设置视频类型
        //从path中打开视频  先打开再播放
        player.open(path, StorageType.Assets, new FunctorOfVoidFromVideoStatus() {
            @Override
            public void invoke(int status) {
                setVideoStatus(status);
            }
        });
    }
    public void openTransparentVideoFile(String path, int texid)//打开透明的视频文件
    {
        this.path = path;
        player.setRenderTexture(texid);
        player.setVideoType(VideoType.TransparentSideBySide);//视频类型
        player.open(path, StorageType.Assets, new FunctorOfVoidFromVideoStatus() {
            @Override
            public void invoke(int status) {
                setVideoStatus(status);
            }
        });
    }
    public void openStreamingVideo(String url, int texid)//打开流媒体视频文件  是url 就是StorageType.Absolute
    {
        this.path = url;
        player.setRenderTexture(texid);
        player.setVideoType(VideoType.Normal);//视频类型
        player.open(url, StorageType.Absolute, new FunctorOfVoidFromVideoStatus() {
            @Override
            public void invoke(int status) {
                setVideoStatus(status);
            }
        });
    }

    public void setVideoStatus(int status)
    {
        Log.i("HelloAR", "video: " + path + " (" + Integer.toString(status) + ")");
        //只有状态为Ready和Completed时播放   Error不播放
        if (status == VideoStatus.Ready) {
            prepared = true;//准备好了
            if (found) {
                player.play();
            }
        } else if (status == VideoStatus.Completed) {
            if (found) {
                player.play();
            }
        }
    }

    public void onFound()//只调用一次
    {
        found = true;
        if (prepared) {
            player.play();
        }
    }
    public void onLost()
    {
        found = false;
        if (prepared) {
            player.pause();
        }
    }
    //这两个方法连着用
    public boolean isRenderTextureAvailable()
    {
        return player.isRenderTextureAvailable();
    }
    public void update()
    {
        player.updateFrame();
    }
}
