package com.example.agc_inventory.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.agc_inventory.R;
import com.example.agc_inventory.entity.*;

public class BookInfoAdapter extends BaseAdapter implements Filterable {
    private ArrayFilter mFilter;
    private List<BookInfo> mList;
    private Context context;
    private ArrayList<BookInfo> mUnfilteredData;

    public BookInfoAdapter(List<BookInfo> mList, Context context) {
        this.mList = mList;
        this.context = context;
    }

    @Override
    public int getCount() { return mList==null ? 0:mList.size(); }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView==null){
            view = View.inflate(context, R.layout.book_item, null);

            holder = new ViewHolder();
            holder.tv_roomname = (TextView) view.findViewById(R.id.tv_roomname);
            holder.tv_bookno = (TextView) view.findViewById(R.id.tv_bookno);
            holder.tv_bookname = (TextView) view.findViewById(R.id.tv_bookname);

            view.setTag(holder);
        }else{
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        BookInfo bi = mList.get(position);

        holder.tv_roomname.setText("辨公室："+bi.getRoomName());
        holder.tv_bookno.setText("編號："+bi.getBookNo());
        holder.tv_bookname.setText("名稱："+bi.getBookName());

        return view;
    }

    static class ViewHolder{
        public TextView tv_roomname;
        public TextView tv_bookno;
        public TextView tv_bookname;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<BookInfo>(mList);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<BookInfo> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();
                ArrayList<BookInfo> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<BookInfo> newValues = new ArrayList<BookInfo>(count);

                for (int i = 0; i < count; i++) {
                    BookInfo bi = unfilteredValues.get(i);
                    if (bi != null) {
                        if(bi.getBookNo().toLowerCase().startsWith(prefixString)){
                            newValues.add(bi);
                        }else if(bi.getBookName().toLowerCase().startsWith(prefixString)){
                            newValues.add(bi);
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            //noinspection unchecked
            mList = (List<BookInfo>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
