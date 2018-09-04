package cz.org.shoosh.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.R;
import cz.org.shoosh.models.ContactModel;

public class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.ViewHolder> implements Filterable {

    private List<ContactModel> mDataSet;
    private List<ContactModel> filteredList;
    private ContactFilter contactFilter;

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
        public final TextView nameTxt, phoneTxt;
        public final ImageView iv_photo;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            nameTxt = (TextView) v.findViewById(R.id.tv_item_contact_name);
            phoneTxt = (TextView) v.findViewById(R.id.tv_item_contact_phone);
            iv_photo = (ImageView) v.findViewById(R.id.iv_item_contact_photo);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public ContactsListAdapter(List<ContactModel> dataSet, Context context) {
        mDataSet = dataSet;
        filteredList = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_contact_list, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ContactModel one = filteredList.get(position);

        viewHolder.nameTxt.setText(one.name);
        viewHolder.phoneTxt.setText(one.phone);
        if(one.photo != null && one.photo.length() > 0){
            Uri myUri = Uri.parse(one.photo);
            viewHolder.iv_photo.setImageURI(myUri);
            //viewHolder.iv_photo.setImageBitmap(BitmapFactory.decodeFile(one.photo));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    ContactModel one = filteredList.get(position);
                    mOnItemClickListener.onItemClick(one.phone);
                }
            }
        });
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setDataList(List<ContactModel> dataSet){
        mDataSet = dataSet;
        filteredList = dataSet;
    }

    public interface OnItemClickListener {
        public int onItemClick(String phoneStr);
    }

    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (contactFilter == null) {
            contactFilter = new ContactFilter();
        }

        return contactFilter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class ContactFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<ContactModel> tempList = new ArrayList<ContactModel>();

                // search content in friend list
                for (ContactModel one : mDataSet) {
                    if (one.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(one);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = mDataSet.size();
                filterResults.values = mDataSet;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<ContactModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
