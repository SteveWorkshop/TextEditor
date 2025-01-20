package io.github.materialapps.texteditor.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.logic.entity.Tag;

public class TagAdapter extends PagedListAdapter<Tag,TagAdapter.ViewHolder> {

    public static final Callback callback=new Callback();

    public TagAdapter(@NonNull DiffUtil.ItemCallback<Tag> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item, parent, false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tag tag=getItem(position);
        if(tag!=null){
            holder.colorView.setBackgroundColor(tag.getColor());
            holder.txbTagName.setText(tag.getTagName());
        }
    }

    public static class Callback extends DiffUtil.ItemCallback<Tag>{

        @Override
        public boolean areItemsTheSame(@NonNull Tag oldItem, @NonNull Tag newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Tag oldItem, @NonNull Tag newItem) {
            return oldItem.equals(newItem);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View tagView;
        View colorView;
        TextView txbTagName;
        Button btnEditTag;
        Button btnDeleteTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tagView=itemView;
            colorView=itemView.findViewById(R.id.block_tag_color);
            txbTagName=itemView.findViewById(R.id.txb_tag_name);
            btnEditTag=itemView.findViewById(R.id.btn_edit_tag);
            btnDeleteTag=itemView.findViewById(R.id.btn_delete_tag);

            btnEditTag.setOnClickListener(v->{

            });

            btnDeleteTag.setOnClickListener(v->{

            });
        }
    }
}
