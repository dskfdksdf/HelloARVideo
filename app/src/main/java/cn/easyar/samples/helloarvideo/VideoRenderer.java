//================================================================================================================================
//
//  Copyright (c) 2015-2018 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarvideo;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import cn.easyar.Vec2F;
import cn.easyar.Matrix44F;

public class VideoRenderer
{
    private int program_box;
    private int pos_coord_box;//使用三次  实例化一次  enable一次  pointer一次
    private int pos_tex_box;//使用三次   实例化一次  enable一次  pointer一次
    private int pos_trans_box;//使用两次  实例化一次 uniform一次
    private int pos_proj_box;//使用两次   实例化一次  uniform一次
    private int vbo_coord_box;//使用四次  实例化一次  绑定三次
    private int vbo_tex_box;//使用三次   实例化一次  绑定两次
    private int vbo_faces_box;//使用三次 实例化一次  绑定两次
    private int texture_id;//使用四次  实例化一次 绑定两次

    private String box_vert="uniform mat4 trans;\n"
            + "uniform mat4 proj;\n"
            + "attribute vec4 coord;\n"
            + "attribute vec2 texcoord;\n"
            + "varying vec2 vtexcoord;\n"
            + "\n"
            + "void main(void)\n"
            + "{\n"
            + "    vtexcoord = texcoord;\n"
            + "    gl_Position = proj*trans*coord;\n"
            + "}\n"
            + "\n"
    ;

    private String box_frag="#ifdef GL_ES\n"
            + "precision highp float;\n"
            + "#endif\n"
            + "varying vec2 vtexcoord;\n"
            + "uniform sampler2D texture;\n"
            + "\n"
            + "void main(void)\n"
            + "{\n"
            + "    gl_FragColor = texture2D(texture, vtexcoord);\n"
            + "}\n"
            + "\n"
    ;

    private float[] flatten(float[][] a)
    {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        float[] l = new float[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }
    private int[] flatten(int[][] a)
    {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        int[] l = new int[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }
    //无用
    private short[] flatten(short[][] a)
    {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        short[] l = new short[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    //无用
    private byte[] flatten(byte[][] a)
    {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        byte[] l = new byte[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }
    private byte[] byteArrayFromIntArray(int[] a)
    {
        byte[] l = new byte[a.length];
        for (int k = 0; k < a.length; k += 1) {
            l[k] = (byte)(a[k] & 0xFF);
        }
        return l;
    }

    private int generateOneBuffer()
    {
        int[] buffer = {0};
        GLES20.glGenBuffers(1, buffer, 0);//新建缓冲区对象  将handle存于buffer之中   唯一
        return buffer[0];
    }

    private int generateOneTexture()
    {
        int[] buffer = {0};
        GLES20.glGenTextures(1, buffer, 0);//命名纹理对象  唯一
        return buffer[0];
    }
    public void init()
    {
        program_box = GLES20.glCreateProgram();
        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, box_vert);
        GLES20.glCompileShader(vertShader);

        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, box_frag);
        GLES20.glCompileShader(fragShader);

        GLES20.glAttachShader(program_box, vertShader);
        GLES20.glAttachShader(program_box, fragShader);
        GLES20.glLinkProgram(program_box);
        GLES20.glUseProgram(program_box);

        pos_coord_box = GLES20.glGetAttribLocation(program_box, "coord");//第一个 四个实例化
        pos_tex_box = GLES20.glGetAttribLocation(program_box, "texcoord");
        pos_trans_box = GLES20.glGetUniformLocation(program_box, "trans");
        pos_proj_box = GLES20.glGetUniformLocation(program_box, "proj");
//第二个 四个实例化开始
        vbo_coord_box = generateOneBuffer();//此方法自己定义
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box);//绑定   顶点数组  第一次绑定
        float cube_vertices[][] = {{1.0f / 2, 1.0f / 2, 0.f},{1.0f / 2, -1.0f / 2, 0.f},{-1.0f / 2, -1.0f / 2, 0.f},{-1.0f / 2, 1.0f / 2, 0.f}};
        FloatBuffer cube_vertices_buffer = FloatBuffer.wrap(flatten(cube_vertices));//将float数组包装到缓冲区中   将数据传递给当前绑定的顶点缓存对象
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_buffer.limit() * 4, cube_vertices_buffer, GLES20.GL_DYNAMIC_DRAW);

        vbo_tex_box = generateOneBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_tex_box);//顶点数组   第一次绑定
        int cube_vertex_colors[][] = {{0, 0},{0, 1},{1, 1},{1, 0}};
        ByteBuffer cube_vertex_colors_buffer = ByteBuffer.wrap(byteArrayFromIntArray(flatten(cube_vertex_colors)));//将byte数组包装到缓冲区中
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertex_colors_buffer.limit(), cube_vertex_colors_buffer, GLES20.GL_STATIC_DRAW);
//将数据传递给当前绑定的顶点缓存对象
        vbo_faces_box = generateOneBuffer();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box);//索引数组  第一次绑定
        short cube_faces[] = {3, 2, 1, 0};
        ShortBuffer cube_faces_buffer = ShortBuffer.wrap(cube_faces);//将short数组包装到缓冲区中   将数据传递给当前绑定的顶点缓存对象
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, cube_faces_buffer.limit() * 2, cube_faces_buffer, GLES20.GL_STATIC_DRAW);

        GLES20.glUniform1i(GLES20.glGetUniformLocation(program_box, "texture"), 0);
        texture_id = generateOneTexture();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_id);//第一次绑定
        //四个过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);//缩小 线性
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);//放大 线性
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);//贴图
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);//贴图
    }

    public void render(Matrix44F projectionMatrix, Matrix44F cameraview, Vec2F size)
    {
        float size0 = size.data[0];//2*1 float向量  用于生产二维数组
        float size1 = size.data[1];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box);//顶点数组  第二次绑定
        float height = size0 / 1000;
        float cube_vertices[][] = {{size0 / 2, size1 / 2, 0}, {size0 / 2, -size1 / 2, 0}, {-size0 / 2, -size1 / 2, 0}, {-size0 / 2, size1 / 2, 0}};
        FloatBuffer cube_vertices_buffer = FloatBuffer.wrap(flatten(cube_vertices));//将float数组包装到缓冲区中   将数据传递给顶点缓存对象
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_buffer.limit() * 4, cube_vertices_buffer, GLES20.GL_DYNAMIC_DRAW);

        GLES20.glEnable(GLES20.GL_BLEND);//启用色彩混合
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);//操作颜色混合
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);//启用深度测试
        GLES20.glUseProgram(program_box);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box);//顶点数组   第三次绑定
        GLES20.glEnableVertexAttribArray(pos_coord_box);//enable pointer
        GLES20.glVertexAttribPointer(pos_coord_box, 3, GLES20.GL_FLOAT, false, 0, 0);//将顶点缓存对象中的数据传递给顶点数组   //  3

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_tex_box);//顶点数组  第二次绑定
        GLES20.glEnableVertexAttribArray(pos_tex_box);//enable pointer
        GLES20.glVertexAttribPointer(pos_tex_box, 2, GLES20.GL_UNSIGNED_BYTE, false, 0, 0);//将顶点缓存对象中的数字传递给顶点数组  //  2

        GLES20.glUniformMatrix4fv(pos_trans_box, 1, false, cameraview.data, 0);//参数列表中两个4*4矩阵
        GLES20.glUniformMatrix4fv(pos_proj_box, 1, false, projectionMatrix.data, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box);//索引数组  第二次绑定
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);//选择纹理单位
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_id);//创建和使用纹理对象   第二次绑定
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4, GLES20.GL_UNSIGNED_SHORT, 0);//可以改为3边形 五边形没用
        //glBindBuffer 索引数组  仅仅调用一次  绘制一个正方形  只是绘制一个形状 播放视频是另外一回事
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//停止使用这个纹理对象 返回无名称的默认纹理
    }
//一个getter方法  HelloAR.java中打开文件是用一下
    public int texId()
    {
        return texture_id;
    }
}
