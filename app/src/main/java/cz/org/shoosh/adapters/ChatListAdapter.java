package cz.org.shoosh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.models.MessageModel;
import cz.org.shoosh.utils.Constants;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private List<MessageModel> mDataSet;
    //private List<ReviewModel> mDataOrigin;

    private OnItemClickListener mOnItemClickListener;

    private final Context mContext;

    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        public final LinearLayout ll_item_chat_receive, ll_item_chat_send;
        public final TextView sendtimeTxt, sendcontentTxt, receivetimeTxt, receivecontentTxt;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            ll_item_chat_send = (LinearLayout) v.findViewById(R.id.ll_item_chat_send);
            ll_item_chat_receive = (LinearLayout) v.findViewById(R.id.ll_item_chat_receive);
            sendtimeTxt = (TextView) v.findViewById(R.id.tv_item_chat_send_time);
            sendcontentTxt = (TextView) v.findViewById(R.id.tv_item_chat_send_content);
            receivecontentTxt = (TextView) v.findViewById(R.id.tv_item_chat_receive_content);
            receivetimeTxt = (TextView) v.findViewById(R.id.tv_item_chat_receive_time);
            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public ChatListAdapter(List<MessageModel> dataSet, Context context) {
        mDataSet = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_chat_list, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        MessageModel one = mDataSet.get(position);

        String myID = MyApp.getInstance().myProfile.uid;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM hh:mm a");
        Long time_stamp = Long.parseLong(one.stime) * 1000;
        String dateString = formatter.format(new Date(time_stamp));
        if(one.sid.equals(myID)){
            viewHolder.ll_item_chat_receive.setVisibility(View.GONE);
            viewHolder.ll_item_chat_send.setVisibility(View.VISIBLE);
            viewHolder.sendcontentTxt.setText(one.msg);
            viewHolder.sendtimeTxt.setText(dateString);
        } else {
            viewHolder.ll_item_chat_send.setVisibility(View.GONE);
            viewHolder.ll_item_chat_receive.setVisibility(View.VISIBLE);
            viewHolder.receivetimeTxt.setText(dateString);
            viewHolder.receivecontentTxt.setText(one.msg);
        } /*else{
            viewHolder.ll_item_chat_send.setVisibility(View.GONE);
            viewHolder.ll_item_chat_receive.setVisibility(View.GONE);
        }*/

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

    public void setDataList(List<MessageModel> dataSet){
        mDataSet = dataSet;
    }

    public interface OnItemClickListener {
        public int onItemClick(int position);
    }
}
