package cn.easyar.samples.helloarvideo.ui.fragment;

import android.content.Context;
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

import cn.easyar.samples.helloarvideo.R;
import cn.easyar.samples.helloarvideo.ui.activity.BaseActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Regist.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Regist#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Regist extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private FragmentActivity activity;

    public Regist() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Regist.
     */
    // TODO: Rename and change types and number of parameters
    public static Regist newInstance(String param1, String param2) {
        Regist fragment = new Regist();
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

    EditText regusername, regpassword;
    Button registbtn;
    private String url = BaseActivity.originUrl + "user/regist";
    private RequestQueue queue;

    private void getStringRequest() {
        queue = Volley.newRequestQueue(activity);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    JSONObject jsonObject = new JSONObject();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                    }
                    int id = jsonObject.getInt("id");//这个键username  服务端没有resultMap也可以 只要和服务端Bean中属性相同就行
                    Toast.makeText(activity, "注册成功，你的序号是 " + id + " ,请登录", Toast.LENGTH_LONG).show();
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
                map.put("username", usernamevalue);
                map.put("password", passwordvalue);
                return map;

            }
        };
        queue.add(request);
    }

    private String usernamevalue, passwordvalue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_regist, container, false);
        regusername = (EditText) view.findViewById(R.id.regusername);
        regpassword = (EditText) view.findViewById(R.id.regpassword);
        registbtn = (Button) view.findViewById(R.id.registbtn);
        registbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernamevalue = regusername.getText().toString().trim();
                passwordvalue = regpassword.getText().toString().trim();
                if (usernamevalue.equals("") || passwordvalue.equals("")) {
                    Toast.makeText(activity, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();

                } else {
                    getStringRequest();
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
