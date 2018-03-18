package cn.easyar.samples.helloarvideo.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.easyar.samples.helloarvideo.Main2Activity;
import cn.easyar.samples.helloarvideo.R;
import cn.easyar.samples.helloarvideo.ui.activity.BaseActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Login.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Login extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnDataTransmissionListener mllistener;  //定义引用

    public interface OnDataTransmissionListener {//内部接口  嵌套类

        void dataTransmission(int data);
    }

    private OnFragmentInteractionListener mListener;
    private FragmentActivity activity;

    public Login() {
        // Required empty public constructor
    }

    public static Login newInstance(String param1, String param2) {
        Login fragment = new Login();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        activity = getActivity();
    }

    EditText username, password;
    Button loginbtn, regist;
    String usernamevalue, passwordvalue;
    private String url = BaseActivity.originUrl + "user/login";
    private RequestQueue queue;

    private void getStringRequest() {
        queue = Volley.newRequestQueue(activity);
        StringRequest sr2 = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    JSONObject jsonObject = new JSONObject();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                    }
                    // Log.d("xxx", jsonArray.length() + "");
                    if (jsonArray.length() != 0) {
                        String name = jsonObject.getString("username");//这个键username  服务端没有resultMap也可以 只要和服务端Bean中属性相同就行
                        //   int role = jsonObject.getInt("roleId");  这个要注释掉  否则报错 因为在try..catch..中 跳到catch中去了 后面的不执行
                        int id = jsonObject.getInt("id");
                        Toast.makeText(activity, "登录成功", Toast.LENGTH_SHORT).show();
                        //跳转界面
                        Intent intent = new Intent(activity, Main2Activity.class);//主界面 Main2Activity.java
                        //   intent.putExtra("ROLE",role);//仅这个用过大写
                        intent.putExtra("username", name);
                        BaseActivity.USER_ID = id;
                        BaseActivity.USER_NAME = name;
                        activity.startActivity(intent);
                    } else {
                        Toast.makeText(activity, "序号或密码错误", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("xxx", e + "");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(activity, BaseActivity.ERROR, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", usernamevalue);
                map.put("password", passwordvalue);
                return map;
            }
        };
        queue.add(sr2);
    }

    public void setmllistener(OnDataTransmissionListener mllistener)    //set引用
    {
        this.mllistener = mllistener;
    }
Button nologinbtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        username = (EditText) view.findViewById(R.id.username);
        password = (EditText) view.findViewById(R.id.password);
        loginbtn = (Button) view.findViewById(R.id.loginbtn);
        nologinbtn=(Button)view.findViewById(R.id.nologinbtn);
        nologinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, Main2Activity.class);
                activity.startActivity(intent);
            }
        });
        regist = (Button) view.findViewById(R.id.regist);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernamevalue = username.getText().toString();//序号
                passwordvalue = password.getText().toString();//密码
                if (usernamevalue.equals("") || passwordvalue.equals("")) {
                    Toast.makeText(activity, "序号或密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    getStringRequest();
                }
            }
        });
        regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mllistener != null) {
                    mllistener.dataTransmission(2);   //将登录碎片换成注册碎片 这里要有返回栈 在LoginActivity.java中实现
                }
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
