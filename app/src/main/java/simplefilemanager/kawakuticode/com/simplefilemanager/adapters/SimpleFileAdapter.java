package simplefilemanager.kawakuticode.com.simplefilemanager.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import simplefilemanager.kawakuticode.com.simplefilemanager.R;

/**
 * Created by russeliusernestius on 20/07/17.
 */

public class SimpleFileAdapter extends RecyclerView.Adapter<SimpleFileAdapter.SimpleAdapterViewHolder> {

    private List<File> mFilesData;
    private Context mContext;
    private OnItemClickListener mItemlistener;
    private OnLongItemClickListener mlongItemClickListener;
    private Animation mAnimator;
    private Explode mExplode;

    public interface OnItemClickListener {
        void onItemClick(File item);
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(File item);
    }


    public SimpleFileAdapter(Context context, OnItemClickListener itemlistener, OnLongItemClickListener longItemListener) {
        this.mContext = context;
        this.mFilesData = new ArrayList<>();
        this.mItemlistener = itemlistener;
        this.mlongItemClickListener = longItemListener;
        this.mAnimator = AnimationUtils.loadAnimation(mContext, R.anim.blink);

    }

    public OnLongItemClickListener getMlongItemClickListener() {
        return mlongItemClickListener;
    }

    public void setMlongItemClickListener(OnLongItemClickListener mlongItemClickListener) {
        this.mlongItemClickListener = mlongItemClickListener;
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemlistener = listener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return this.mItemlistener;
    }

    public Context getmContext() {
        return mContext;
    }


    @Override
    public SimpleFileAdapter.SimpleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutIdForListItem = R.layout.file_row;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new SimpleAdapterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(SimpleAdapterViewHolder fileholder, int position) {
        fileholder.bind(mFilesData.get(position), mItemlistener, mlongItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mFilesData.size();
    }


    public void setDirContentData(List<File> dirFilesData) {

        if (!dirFilesData.isEmpty()) {
            this.mFilesData = dirFilesData;
            notifyDataSetChanged();
        }
        else {
            this.mFilesData = new ArrayList<>();
            notifyDataSetChanged();
        }
    }


    public class SimpleAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView size;
        ImageView icon;

        SimpleAdapterViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tv_name);
            size = (TextView) v.findViewById(R.id.tv_size);
            icon = (ImageView) v.findViewById(R.id.icon);
        }

        public void bind(final File file, final OnItemClickListener itemlistener, final OnLongItemClickListener longItemClickListener) {

            icon.setImageResource(file.isDirectory() ? R.drawable.ic_folder_ : R.mipmap.ic_file);
            name.setText(file.getName());
            generateTextView(file);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (file.isDirectory())
                        icon.startAnimation(mAnimator);
                        itemlistener.onItemClick(file);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longItemClickListener.onLongItemClick(file);
                    return true;
                }
            });
        }


        public void generateTextView(File file) {
            if (file.isDirectory()) {
                size.setVisibility(View.GONE);
            } else {
                size.setVisibility(View.VISIBLE);
                size.setText(String.valueOf(file.length()) + "MB");
            }
        }
    }
}




