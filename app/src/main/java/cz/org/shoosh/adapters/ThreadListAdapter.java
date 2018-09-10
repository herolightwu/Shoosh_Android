package cz.org.shoosh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.org.shoosh.R;
import cz.org.shoosh.models.ThreadModel;

public class ThreadListAdapter extends RecyclerView.Adapter<ThreadListAdapter.ViewHolder> {

    private List<ThreadModel> mDataSet;
    DatabaseReference database;

    private OnItemClickListener mOnItemClickListener;
    private OnOptionMenuListener onOptionMenuListener;

    private final Context mContext;
    String[] options = {"","Delete    ",};
    int images[] = {0, R.drawable.ic_delete_gray,};
    static CustomAdapter customAdapter;


    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnOptionMenuListener (OnOptionMenuListener listener){
        onOptionMenuListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameTxt, timeTxt, contentTxt;
        //public final Spinner spOption;
        public final ImageView iv_option;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        public ViewHolder(View v) {
            super(v);
            nameTxt = (TextView) v.findViewById(R.id.tv_item_thread_name);
            timeTxt = (TextView) v.findViewById(R.id.tv_item_thread_time);
            contentTxt = (TextView) v.findViewById(R.id.tv_item_thread_content);
            iv_option = (ImageView) v.findViewById(R.id.iv_item_thread_option);
            //spOption = (Spinner) v.findViewById(R.id.sp_item_thread_option);

            //spOption.setAdapter(customAdapter);
            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public ThreadListAdapter(List<ThreadModel> dataSet, Context context) {
        mDataSet = dataSet;
        mContext = context;
        database = FirebaseDatabase.getInstance().getReference();
        //customAdapter= new CustomAdapter(mContext , images, options);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_thread_list, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        ThreadModel one = mDataSet.get(position);

        /*viewHolder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadModel other = mDataSet.get(position);
                String uid = MyApp.getInstance().myProfile.uid;
                database.child("users").child(uid).child("threads").child(other.skey).setValue(null);
                mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                mDataSet.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mDataSet.size());
                mItemManger.closeAllItems();
            }
        });*/
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM hh:mm a");
        Long time_stamp = Long.parseLong(one.stime) * 1000;
        String dateString = formatter.format(new Date(time_stamp));
        viewHolder.nameTxt.setText(one.sname);
        viewHolder.timeTxt.setText(dateString);
        if(one.sphone.equals("Shoosh")){
            String[] ct_str = one.msg.split(":::");
            viewHolder.nameTxt.setText("Shoosh");
            if(ct_str.length == 1){
                viewHolder.contentTxt.setText(one.sname + " has disputed your comment.");
            } else{
                viewHolder.contentTxt.setText(one.sname + " has disputed your comment \"" + ct_str[1] + "\"");
            }

        } else{
            viewHolder.contentTxt.setText(one.msg);
        }

        viewHolder.iv_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onOptionMenuListener != null) {
                    onOptionMenuListener.onOptionMenu(view, position);
                }
            }
        });

        /*viewHolder.spOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0) return;
                if(i == 1){
                    ThreadModel other = mDataSet.get(position);
                    String uid = MyApp.getInstance().myProfile.uid;
                    database.child("users").child(uid).child("threads").child(other.skey).setValue(null);
                    mDataSet.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mDataSet.size());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setDataList(List<ThreadModel> dataSet){
        mDataSet = dataSet;
    }

    public interface OnItemClickListener {
        public int onItemClick(int position);
    }

    public interface OnOptionMenuListener {
        public int onOptionMenu(View view, int position);
    }

    public class CustomAdapter extends BaseAdapter {
        Context context;
        int images[];
        String[] fruit;
        LayoutInflater inflter;

        public CustomAdapter(Context applicationContext, int[] flags, String[] fruit) {
            this.context = applicationContext;
            this.images = flags;
            this.fruit = fruit;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.option_item, null);
            ImageView icon = (ImageView) view.findViewById(R.id.imageView);
            TextView names = (TextView) view.findViewById(R.id.textView);
            if(images[i] != 0)
            {
                icon.setImageResource(images[i]);
            } else{
                icon.setVisibility(View.INVISIBLE);
            }
            if(fruit[i] != null)
                names.setText(fruit[i]);
            return view;
        }
    }
}
