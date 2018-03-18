//================================================================================================================================
//
//  Copyright (c) 2015-2018 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarvideo;

import java.util.ArrayList;

import android.opengl.GLES20;
import android.util.Log;

import cn.easyar.CameraCalibration;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraFrameStreamer;
import cn.easyar.Frame;
import cn.easyar.FunctorOfVoidFromPointerOfTargetAndBool;
import cn.easyar.Image;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.Renderer;
import cn.easyar.StorageType;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.Vec2I;
import cn.easyar.Vec4I;

public class HelloAR {
    private CameraDevice camera;
    private CameraFrameStreamer streamer;
    private ArrayList<ImageTracker> trackers;
    private Renderer videobg_renderer;
    //五个东西
    private ArrayList<VideoRenderer> video_renderers;//集合
    private VideoRenderer current_video_renderer;//对象
    private int tracked_target = 0;//两个target
    private int active_target = 0;
    private ARVideo video = null;//ARVideo对象

    private boolean viewport_changed = false;  //窗口是否变化了
    private Vec2I view_size = new Vec2I(0, 0);
    private int rotation = 0;
    private Vec4I viewport = new Vec4I(0, 0, 1280, 720);//用于glviewport

    public HelloAR() {
        trackers = new ArrayList<ImageTracker>();
    }

    private void loadFromImage(ImageTracker tracker, String path) {
        ImageTarget target = new ImageTarget();
        String jstr = "{\n"
                + "  \"images\" :\n"
                + "  [\n"
                + "    {\n"
                + "      \"image\" : \"" + path + "\",\n"
                + "      \"name\" : \"" + path.substring(0, path.indexOf(".")) + "\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        target.setup(jstr, StorageType.Assets | StorageType.Json, "");
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadAllFromJsonFile(ImageTracker tracker, String path) {
        for (ImageTarget target : ImageTarget.setupAll(path, StorageType.Assets)) {
            tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
                @Override
                public void invoke(Target target, boolean status) {
                    try {
                        Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
                    } catch (Throwable ex) {
                    }
                }
            });
        }
    }

    public boolean initialize() {
        camera = new CameraDevice();
        streamer = new CameraFrameStreamer();
        streamer.attachCamera(camera);

        boolean status = true;
        status &= camera.open(CameraDeviceType.Default);
        camera.setSize(new Vec2I(1280, 720));

        if (!status) {
            return status;
        }
        ImageTracker tracker = new ImageTracker();
        tracker.attachStreamer(streamer);
        loadAllFromJsonFile(tracker, "targets.json");
        loadFromImage(tracker, "namecard.jpg");
        trackers.add(tracker);

        return status;
    }

    public void dispose()//只调用一次
    {
        if (video != null) {
            video.dispose();//player.close 关闭
            video = null;
        }
        tracked_target = 0;
        active_target = 0;

        for (ImageTracker tracker : trackers) {
            tracker.dispose();
        }
        trackers.clear();
        video_renderers.clear();
        current_video_renderer = null;
        if (videobg_renderer != null) {
            videobg_renderer.dispose();
            videobg_renderer = null;
        }
        if (streamer != null) {
            streamer.dispose();
            streamer = null;
        }
        if (camera != null) {
            camera.dispose();
            camera = null;
        }
    }

    public boolean start() {
        boolean status = true;
        status &= (camera != null) && camera.start();
        status &= (streamer != null) && streamer.start();
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        for (ImageTracker tracker : trackers) {
            status &= tracker.start();
        }
        return status;
    }

    public boolean stop()//值调用一次
    {
        boolean status = true;
        for (ImageTracker tracker : trackers) {
            status &= tracker.stop();
        }
        status &= (streamer != null) && streamer.stop();
        status &= (camera != null) && camera.stop();
        return status;
    }

    public void initGL() {
        if (active_target != 0) {
            video.onLost();//初始时onLost方法被调用，found=false
            video.dispose();//初始时player.close  关闭
            video = null;  //ARVideo对象设置为null
            tracked_target = 0;
            active_target = 0;
        }
        if (videobg_renderer != null) {//有的
            videobg_renderer.dispose();//有的
        }//有的
        videobg_renderer = new Renderer();//有的
        video_renderers = new ArrayList<VideoRenderer>();
        for (int k = 0; k < 3; k += 1) {
            VideoRenderer video_renderer = new VideoRenderer();//有的
            video_renderer.init();//有的
            video_renderers.add(video_renderer);//List集合中添加元素   0 1 2 中分别添加三种视频文件
        }
        current_video_renderer = null;
    }

    public void resizeGL(int width, int height) {
        view_size = new Vec2I(width, height);//调节尺寸
        viewport_changed = true;//窗口变化了
    }

    private void updateViewport()//只调用一次
    {
        CameraCalibration calib = camera != null ? camera.cameraCalibration() : null;
        int rotation = calib != null ? calib.rotation() : 0;
        if (rotation != this.rotation) {
            this.rotation = rotation;//旋转角度变化了
            viewport_changed = true;//窗口变化了
        }
        if (viewport_changed) {//窗口变化了
            Vec2I size = new Vec2I(1, 1);//新建一个2*1向量
            if ((camera != null) && camera.isOpened()) {
                size = camera.size();//camera尺寸重新设置size
            }
            if (rotation == 90 || rotation == 270) {
                size = new Vec2I(size.data[1], size.data[0]);//size
            }
            float scaleRatio = Math.max((float) view_size.data[0] / (float) size.data[0], (float) view_size.data[1] / (float) size.data[1]);
            Vec2I viewport_size = new Vec2I(Math.round(size.data[0] * scaleRatio), Math.round(size.data[1] * scaleRatio));//四舍五入
            //
            viewport = new Vec4I((view_size.data[0] - viewport_size.data[0]) / 2, (view_size.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1]);

            if ((camera != null) && camera.isOpened())
                viewport_changed = false;
        }
    }

    public void render() {
        GLES20.glClearColor(1.f, 1.f, 1.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (videobg_renderer != null) {
            Vec4I default_viewport = new Vec4I(0, 0, view_size.data[0], view_size.data[1]);//4*1向量
            //设置串口
            GLES20.glViewport(default_viewport.data[0], default_viewport.data[1], default_viewport.data[2], default_viewport.data[3]);
            if (videobg_renderer.renderErrorMessage(default_viewport)) {//渲染错误信息到绑定的帧缓冲对象
                return;
            }
        }

        if (streamer == null) {
            return;
        }
        Frame frame = streamer.peek();
        try {
            updateViewport();//唯一调用
            //打开窗口
            GLES20.glViewport(viewport.data[0], viewport.data[1], viewport.data[2], viewport.data[3]);

            if (videobg_renderer != null) {
                videobg_renderer.render(frame, viewport);
            }
//以上不变
            ArrayList<TargetInstance> targetInstances = frame.targetInstances();//加的
            if (targetInstances.size() > 0) {//加的
                TargetInstance targetInstance = targetInstances.get(0);//instance到target 再到status
                Target target = targetInstance.target();
                int status = targetInstance.status();//不变
                if (status == TargetStatus.Tracked) {//不变
                    int id = target.runtimeID();
                    if (active_target != 0 && active_target != id) {
                        video.onLost();//onLost第二次被调用  found=false
                        video.dispose();//player.close  关闭
                        video = null;//ARVideo设置为null
                        tracked_target = 0;  //这两个int设置为0
                        active_target = 0;
                    }
                    if (tracked_target == 0) {
                        if (video == null && video_renderers.size() > 0) {
                            //不同的图片播放不同的视频  0 表示普通视频文件  1 表示透明视频文件 2表示流媒体视频文件
                            String target_name = target.name();            //texId()不等于0
                            if (target_name.equals("argame") && video_renderers.get(0).texId() != 0) {//不带后缀名
                                video = new ARVideo();//重新初始化
                                video.openVideoFile("video.mp4", video_renderers.get(0).texId());//普通视频文件
                                current_video_renderer = video_renderers.get(0);
                            }else if (target_name.equals("a001")&&video_renderers.get(0).texId()!=0){
                                video = new ARVideo();
                                video.openVideoFile("a001.mp4",video_renderers.get(0).texId());
                                current_video_renderer=video_renderers.get(0);
                            }
                            else if (target_name.equals("namecard") && video_renderers.get(1).texId() != 0) {//不带后缀名
                                video = new ARVideo();//重新初始化
                                video.openTransparentVideoFile("transparentvideo.mp4", video_renderers.get(1).texId());//透明视频文件
                                current_video_renderer = video_renderers.get(1);
                            } else if (target_name.equals("idback") && video_renderers.get(2).texId() != 0) {//不带后缀名
                                video = new ARVideo();//重新初始化
                                //流媒体视频文件
                                video.openStreamingVideo("https://sightpvideo-cdn.sightp.com/sdkvideo/EasyARSDKShow201520.mp4", video_renderers.get(2).texId());
                                current_video_renderer = video_renderers.get(2);
                            }
                        }
                        if (video != null) {
                            video.onFound();//onFound()方法唯一调用  也是found唯一被设置为true;
                            tracked_target = id;
                            active_target = id;
                        }
                    }
                    ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;//不变
                    if (imagetarget != null) {//不变
                        if (current_video_renderer != null) {//不变
                            video.update();//唯一调用  两个方法连着用
                            if (video.isRenderTextureAvailable()) {//唯一调用
                                //投影矩阵
                                current_video_renderer.render(camera.projectionGL(0.2f, 500.f), targetInstance.poseGL(), imagetarget.size());//不变
                            }
                        }
                    }
                }
            } else {
                if (tracked_target != 0) {
                    video.onLost();//onLost第三次被调用  found=false
                    tracked_target = 0;//设置为0
                }
            }
        } finally {
            frame.dispose();
        }
    }
}
