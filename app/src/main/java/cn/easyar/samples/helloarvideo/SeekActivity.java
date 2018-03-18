package cn.easyar.samples.helloarvideo;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.List;

public class SeekActivity extends Activity implements Inputtips.InputtipsListener{
    String poi;//63行是唯一用到poi的地方

    App app;//App
    EditText searchView;//搜索框
    private ListView mListView;
    private List<Tip> mDatas=new ArrayList<>();
    //  List<Tip> tiplist=new ArrayList<>();//Tip   tiplist和mDatas都是以Tip为泛型的容器 我这里填充自定义adapter是以tiplist

    private adapter madapter;//避免空指针
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//Activity去标题栏
        setContentView(R.layout.searchbox_activity);
        app = (App) getApplication();
        searchView = (EditText) findViewById(R.id.et_search);
        mListView = (ListView) findViewById(R.id.list_search);

        madapter=new adapter(SeekActivity.this,mDatas);
        mListView.setAdapter(madapter);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                madapter.getFilter().filter(s);   //这句存在的价值就是EditText与ListView联合
                //这五句可以调用onGetInputtips()  打印出hello
                if (searchView.getText()!=null){
                    InputtipsQuery inputquery = new InputtipsQuery(searchView.getText()+"",app.city);
                    inputquery.setCityLimit(true);//限制在当前城市
                    Inputtips inputTips = new Inputtips(SeekActivity.this, inputquery);
                    inputTips.setInputtipsListener(SeekActivity.this);
                    inputTips.requestInputtipsAsyn();
                }
                //madapter.getFilter().filter(s)和后五句各有各的作用都不能少

                //  Toast.makeText(SeekActivity.this, "" + s, Toast.LENGTH_SHORT).show();//这里肯定已经被调用
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onGetInputtips(List<Tip> list, int i) {//他这个list是他自己产生的

        mDatas = list;
        //  Toast.makeText(this,"hello "+list,Toast.LENGTH_SHORT).show();//此方法未被调用
        madapter=new adapter(SeekActivity.this,mDatas);
//        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, null);//数组或容器
        mListView.setAdapter(madapter);
        //监听点击事件   //ListView点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                app.setTip(mDatas.get(position));

                SeekActivity.this.finish();//结束活动后到了MainActivity  onRestart()
            }
        });
    }
    //新建一个adapter
    class adapter extends BaseAdapter implements Filterable {
        public adapter(Context context, List<Tip> list){
            this.context=context;mDatas=list; mInflater = LayoutInflater.from(context);
        }
        Context context;  private LayoutInflater mInflater;
        //四个方法是以tiplist为核心的
        @Override
        public int getCount() {
            return mDatas.size() > 0 ? mDatas.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
           /* convertView = View.inflate(SeekActivity.this, R.layout.item_seek,null);
            TextView textView = (TextView) convertView.findViewById(R.id.item_text);
            textView.setText(tiplist.get(position).getName());
            return convertView;*/
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_seek, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.item_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Tip user = mDatas.get(position);//java.lang.IndexOutOfBoundsException:Index 0,size 0
            holder.name.setText(user.getName());
            return convertView;
        }
        private ArrayFilter mFilter;
        public class ViewHolder {
            TextView name;
        }
        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new ArrayFilter();
            }
            return mFilter;
        }
        private ArrayList<Tip> mOriginalValues;
        private final Object mLock = new Object();
        //避免空指针

        private class ArrayFilter extends Filter {
            //执行刷选
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();//过滤的结果
                //原始数据备份为空时，上锁，同步复制原始数据
                if (mOriginalValues == null) {
                    synchronized (mLock) {
                        mOriginalValues = new ArrayList<>(mDatas);
                    }
                }
                //当首字母为空时
                if (prefix == null || prefix.length() == 0) {
                    ArrayList<Tip> list;
                    synchronized (mLock) {//同步复制一个原始备份数据
                        list = new ArrayList<>(mOriginalValues);
                    }
                    results.values = list;
                    results.count = list.size();//此时返回的results就是原始的数据，不进行过滤
                } else {
                    String prefixString = prefix.toString().toLowerCase();//转化为小写

                    ArrayList<Tip> values;
                    synchronized (mLock) {//同步复制一个原始备份数据
                        values = new ArrayList<>(mOriginalValues);
                    }
                    final int count = values.size();
                    final ArrayList<Tip> newValues = new ArrayList<>();

                    for (int i = 0; i < count; i++) {
                        final Tip value = values.get(i);//从List<User>中拿到User对象
//                    final String valueText = value.toString().toLowerCase();
                        final String valueText = value.getName().toString().toLowerCase();//User对象的name属性作为过滤的参数
                        // First match against the whole, non-splitted value
                        if (valueText.startsWith(prefixString) || valueText.indexOf(prefixString.toString()) != -1) {//第一个字符是否匹配
                            newValues.add(value);//将这个item加入到数组对象中
                        } else {//处理首字符是空格
                            final String[] words = valueText.split(" ");
                            final int wordCount = words.length;

                            // Start at index 0, in case valueText starts with space(s)
                            for (int k = 0; k < wordCount; k++) {
                                if (words[k].startsWith(prefixString)) {//一旦找到匹配的就break，跳出for循环
                                    newValues.add(value);
                                    break;
                                }
                            }
                        }
                    }
                    results.values = newValues;//此时的results就是过滤后的List<User>数组
                    results.count = newValues.size();
                }
                return results;
            }

            //刷选结果
            @Override
            protected void publishResults(CharSequence prefix, FilterResults results) {
                //noinspection unchecked
                mDatas = (List<Tip>) results.values;//此时，Adapter数据源就是过滤后的Results
                if (results.count > 0) {
                    notifyDataSetChanged();//这个相当于从mDatas中删除了一些数据，只是数据的变化，故使用notifyDataSetChanged()
                } else {
                    /**
                     * 数据容器变化 ----> notifyDataSetInValidated

                     容器中的数据变化  ---->  notifyDataSetChanged
                     */
                    notifyDataSetInvalidated();//当results.count<=0时，此时数据源就是重新new出来的，说明原始的数据源已经失效了
                }
            }
        }
    }
}
