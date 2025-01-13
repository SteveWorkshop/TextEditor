package io.github.materialapps.texteditor.ui.adapter;

import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;

import io.github.materialapps.texteditor.R;

import java.util.List;

import io.github.materialapps.texteditor.util.ScreenUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder>{

    private static final String TAG = "ColorAdapter";

    private List<ColorTag> colorTagList;

    @Getter
    @Setter
    private int mPosition=-1;

    @Getter
    @Setter
    private SelectInterface selectInterface;

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
            mPosition=position;
            if(selectInterface!=null){
                selectInterface.onSelect(position);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ColorTag colorTag=colorTagList.get(position);
        String name=colorTag.name;
        int color=colorTag.value;

        ShapeAppearanceModel.Builder builder = ShapeAppearanceModel.builder();
        builder.setAllCorners(new RoundedCornerTreatment());
        builder.setAllCornerSizes(ScreenUtil.dp2px(15));//todo:这里的单位是什么有待考证
        ShapeAppearanceModel shapeAppearanceModel = builder.build();
        MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
        materialShapeDrawable.setTint(color);
        materialShapeDrawable.setPaintStyle(Paint.Style.FILL);
        holder.name.setText(name);
        holder.colorBlock.setBackground(materialShapeDrawable);
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

    //add an interface so that the caller knows I selected sth...
    public interface SelectInterface {
        void onSelect(int position);
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ColorTag{
        String name;
        int value;
    }
}
