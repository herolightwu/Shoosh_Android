package cz.org.shoosh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.models.CommentModel;
import cz.org.shoosh.models.ThreadModel;

public class ResultListAdapter extends RecyclerView.Adapter<ResultListAdapter.ViewHolder> {

    private List<CommentModel> mDataSet;
    //private List<ReviewModel> mDataOrigin;

    private OnItemClickListener mOnItemClickListener;
    private OnItemDeleteListener mOnItemDeleteListener;
    private OnItemEditListener mOnItemEditListener;
    private OnItemContactListener mOnItemContactListener;
    private OnItemReportListener mOnItemReportListener;

    private final Context mContext;
    public String search_number;

    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemDeleteListener (OnItemDeleteListener listener) {
        mOnItemDeleteListener = listener;
    }

    public void setOnItemEditListener (OnItemEditListener listener) {
        mOnItemEditListener = listener;
    }

    public void setmOnItemContactListener (OnItemContactListener listener) {
        mOnItemContactListener = listener;
    }

    public void setOnItemReportListener (OnItemReportListener listener) {
        mOnItemReportListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameTxt, timeTxt, contentTxt, tv_result_contact;
        public final RelativeLayout rl_result_upgrade;
        public final LinearLayout ll_result_delete, ll_result_edit, ll_result_contact, ll_result_item;
        public final LinearLayout ll_foreground, ll_report;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            nameTxt = (TextView) v.findViewById(R.id.tv_item_result_name);
            timeTxt = (TextView) v.findViewById(R.id.tv_item_result_time);
            contentTxt = (TextView) v.findViewById(R.id.tv_item_result_content);
            ll_result_item = (LinearLayout) v.findViewById(R.id.ll_result_item);
            rl_result_upgrade = (RelativeLayout) v.findViewById(R.id.rl_result_upgrade_btn);
            ll_result_delete = (LinearLayout) v.findViewById(R.id.ll_result_delete);
            ll_result_edit = (LinearLayout) v.findViewById(R.id.ll_result_edit);
            ll_result_contact = (LinearLayout) v.findViewById(R.id.ll_result_contact);
            ll_foreground = (LinearLayout) v.findViewById(R.id.ll_foreground);
            tv_result_contact = (TextView) v.findViewById(R.id.tv_result_contact);
            ll_report = (LinearLayout) v.findViewById(R.id.ll_result_report);
            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public ResultListAdapter(List<CommentModel> dataSet, Context context, String sh_number) {
        mDataSet = dataSet;
        mContext = context;
        search_number = sh_number;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_result_list, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM hh:mm a");

        CommentModel one = mDataSet.get(position);
        viewHolder.nameTxt.setText(one.name);
        Long time_stamp = Long.parseLong(one.feedtime) * 1000;
        String dateString = formatter.format(new Date(time_stamp));
        viewHolder.timeTxt.setText(dateString);
        viewHolder.contentTxt.setText(one.content);
        String contact_name = "Contact " + one.name;
        viewHolder.tv_result_contact.setText(contact_name);

        if(!one.bDisp){
            viewHolder.ll_foreground.setVisibility(View.VISIBLE);
            viewHolder.rl_result_upgrade.setVisibility(View.VISIBLE);
        } else{
            viewHolder.ll_foreground.setVisibility(View.GONE);
            viewHolder.rl_result_upgrade.setVisibility(View.GONE);
        }

        String myphone = MyApp.getInstance().myProfile.phoneno;
        String one_num = one.uphone;

        if(!myphone.equals(one_num) && one.bContact){
            viewHolder.ll_result_contact.setVisibility(View.VISIBLE);
        } else{
            viewHolder.ll_result_contact.setVisibility(View.GONE);
        }

        if(myphone.equals(search_number) && !one.breport){
            viewHolder.ll_result_delete.setVisibility(View.GONE);
            viewHolder.ll_result_edit.setVisibility(View.GONE);
            viewHolder.ll_report.setVisibility(View.VISIBLE);
        } else if(!myphone.equals(one_num)){
            viewHolder.ll_result_delete.setVisibility(View.GONE);
            viewHolder.ll_result_edit.setVisibility(View.GONE);
        } else{
            viewHolder.ll_result_delete.setVisibility(View.VISIBLE);
            viewHolder.ll_result_edit.setVisibility(View.VISIBLE);
        }

        /*viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });*/
        viewHolder.rl_result_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(position);
            }
        });

        viewHolder.ll_result_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemDeleteListener.onItemDelete(position);
            }
        });
        viewHolder.ll_result_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemEditListener.onItemEdit(position);
            }
        });

        viewHolder.ll_result_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemContactListener.onItemContact(position);
            }
        });

        viewHolder.ll_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemReportListener.onItemReport(position);
            }
        });
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        int nRet = mDataSet.size();
        return nRet;
    }

    public void setDataList(List<CommentModel> dataSet){
        mDataSet = dataSet;
    }

    public interface OnItemClickListener {
        public int onItemClick(int position);
    }

    public interface OnItemDeleteListener{
        public int onItemDelete(int position);
    }

    public interface OnItemEditListener{
        public int onItemEdit(int position);
    }

    public interface OnItemContactListener{
        public int onItemContact(int position);
    }

    public interface OnItemReportListener{
        public int onItemReport(int position);
    }
}
