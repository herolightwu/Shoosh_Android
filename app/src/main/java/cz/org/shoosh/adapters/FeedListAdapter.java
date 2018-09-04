package cz.org.shoosh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.org.shoosh.R;
import cz.org.shoosh.models.FeedModel;

public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.ViewHolder> {

    private List<FeedModel> mDataSet;
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
        public final TextView timeTxt, contentTxt;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            timeTxt = (TextView) v.findViewById(R.id.tv_item_feed_time);
            contentTxt = (TextView) v.findViewById(R.id.tv_item_feed_content);
            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public FeedListAdapter(List<FeedModel> dataSet, Context context) {
        mDataSet = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_feed_list, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        FeedModel one = mDataSet.get(position);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM hh:mm a");
        Long time_stamp = Long.parseLong(one.stime) * 1000;
        String dateString = formatter.format(new Date(time_stamp));
        viewHolder.timeTxt.setText(dateString);

        String stxt = "<b>" + one.uname + "</b> has just received a comment. Read Now.";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            viewHolder.contentTxt.setText(Html.fromHtml(stxt, Html.FROM_HTML_MODE_LEGACY));
        } else {
            viewHolder.contentTxt.setText(Html.fromHtml(stxt));
        }

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

    public void setDataList(List<FeedModel> dataSet){
        mDataSet = dataSet;
    }

    public interface OnItemClickListener {
        public int onItemClick(int position);
    }
}
