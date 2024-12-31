package io.github.materialapps.texteditor.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.materialapps.texteditor.R;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder>{

    private List<ColorTag> colorTagList;

    @Getter
    @Setter
    private int mPosition=-1;

    public ColorAdapter(){}
    public ColorAdapter(List<ColorTag> colorTagList){
        this.colorTagList=colorTagList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.color_selector_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        holder.itemView.setOnClickListener(v->{
            int position= holder.getAbsoluteAdapterPosition();
            //Toast.makeText(parent.getContext(), "Ciallo~"+position, Toast.LENGTH_SHORT).show();
            mPosition=position;
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ColorTag colorTag=colorTagList.get(position);
        String name=colorTag.name;
        int color=colorTag.value;

        holder.name.setText(name);
        holder.colorBlock.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return colorTagList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        View colorBlock;
        TextView name;

        View colorView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView=itemView;
            colorBlock=itemView.findViewById(R.id.block_color_preview);
            name=itemView.findViewById(R.id.txb_color_name);
        }
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ColorTag{
        String name;
        int value;
    }
}
