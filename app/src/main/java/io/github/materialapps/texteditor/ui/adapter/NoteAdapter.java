package io.github.materialapps.texteditor.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.logic.entity.Note;
import io.github.materialapps.texteditor.logic.entity.Tag;
import io.github.materialapps.texteditor.logic.entity.vo.NoteVO;
import lombok.Getter;
import lombok.Setter;

public class NoteAdapter extends PagedListAdapter<NoteVO,NoteAdapter.ViewHolder> {

    private static final String TAG = "NoteAdapter";

    @Getter
    @Setter
    private NoteInterface intf;

    @Getter
    @Setter
    private int mPosition=-1;

    @Getter
    @Setter
    private  Long mId=-1l;

    public static final Callback callback=new Callback();

    public NoteAdapter(@NonNull DiffUtil.ItemCallback<NoteVO> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(v->{
            int position=viewHolder.getAbsoluteAdapterPosition();
            NoteVO item = getItem(position);
            //回调
            if(intf!=null){
                intf.click(position,item);
            }
        });
        viewHolder.itemView.setOnLongClickListener(v->{
            mPosition=viewHolder.getAbsoluteAdapterPosition();
            mId= Objects.requireNonNull(getItem(mPosition)).getId();
            return false;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoteVO note = getItem(position);
        if(note!=null){
            holder.txbNoteTitle.setText(note.getTitle());
            holder.txbAbstract.setText(note.getAbsc());
            Long tagId = note.getTagId();

            //Log.d(TAG, "onBindViewHolder: ---标签："+tagId+"\t什么？ "+note.getTagName());

            if(tagId==null||tagId.equals(Tag.DEFAULT_TAG)){
                holder.txbTagPName.setText("默认标签");
                holder.tagColor.setVisibility(View.INVISIBLE);
            }
            else{
                holder.tagColor.setVisibility(View.VISIBLE);
                Log.d(TAG, "onBindViewHolder: 雪豹闭嘴，悦刻回笼");
                holder.txbTagPName.setText(note.getTagName());
                holder.tagColor.setBackgroundColor(note.getColor());
            }
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
        }
    }

    public interface NoteInterface{
        void click(int index,NoteVO vo);
    }
}
