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
import lombok.Getter;
import lombok.Setter;

public class TagAdapter extends PagedListAdapter<Tag,TagAdapter.ViewHolder> {

    public static final Callback callback=new Callback();

    public TagAdapter(@NonNull DiffUtil.ItemCallback<Tag> diffCallback) {
        super(diffCallback);
    }

    @Getter
    @Setter
    private ClickCall clickCall;

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
            holder.tid=tag.getId();
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

    public interface ClickCall{
        void clickEdit(Long tid,String tagName,int color);
        void clickDelete(Long tid);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        View tagView;
        View colorView;
        TextView txbTagName;
        Button btnEditTag;
        Button btnDeleteTag;
        Long tid;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tagView=itemView;
            colorView=itemView.findViewById(R.id.block_tag_color);
            txbTagName=itemView.findViewById(R.id.txb_tag_name);
            btnEditTag=itemView.findViewById(R.id.btn_edit_tag);
            btnDeleteTag=itemView.findViewById(R.id.btn_delete_tag);

            btnEditTag.setOnClickListener(v->{
                int position = getAbsoluteAdapterPosition();
                Tag tag=getItem(position);
                if(clickCall!=null){
                    clickCall.clickEdit(tag.getId(),tag.getTagName(),tag.getColor());
                }
            });

            btnDeleteTag.setOnClickListener(v->{
                int position = getAbsoluteAdapterPosition();
                Tag tag=getItem(position);
                if(clickCall!=null){
                    clickCall.clickDelete(tag.getId());
                }
            });
        }
    }
}
