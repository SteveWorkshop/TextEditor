package io.github.materialapps.texteditor.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.logic.entity.Note;
import io.github.materialapps.texteditor.logic.entity.Tag;
import io.github.materialapps.texteditor.logic.entity.vo.NoteVO;

public class NoteAdapter extends PagedListAdapter<NoteVO,NoteAdapter.ViewHolder> {

    public static final Callback callback=new Callback();

    public NoteAdapter(@NonNull DiffUtil.ItemCallback<NoteVO> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoteVO note = getItem(position);
        if(note!=null){
            holder.txbNoteTitle.setText(note.getTitle());
            holder.txbAbstract.setText(note.getAbsc());
            holder.txbTagPName.setText(note.getTagName());
            holder.tagColor.setBackgroundColor(note.getColor());
        }
    }


    public static class Callback extends DiffUtil.ItemCallback<NoteVO>{

        @Override
        public boolean areItemsTheSame(@NonNull NoteVO oldItem, @NonNull NoteVO newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull NoteVO oldItem, @NonNull NoteVO newItem) {
            return oldItem.equals(newItem);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View noteView;
        TextView txbNoteTitle;
        TextView txbAbstract;
        View tagColor;
        TextView txbTagPName;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            noteView=itemView;
            txbNoteTitle=itemView.findViewById(R.id.txb_note_title);
            txbAbstract=itemView.findViewById(R.id.txb_abstract);
            tagColor=itemView.findViewById(R.id.blk_tag_color);
            txbTagPName=itemView.findViewById(R.id.txb_tag_p_name);

            noteView.setOnClickListener(v->{
                //todo:跳转逻辑
            });
        }
    }
}
