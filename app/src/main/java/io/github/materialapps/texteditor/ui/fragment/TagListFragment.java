package io.github.materialapps.texteditor.ui.fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.databinding.FragmentTagListBinding;
import io.github.materialapps.texteditor.logic.entity.Tag;
import io.github.materialapps.texteditor.ui.adapter.ColorAdapter;
import io.github.materialapps.texteditor.ui.adapter.TagAdapter;

public class TagListFragment extends Fragment {

    private FragmentTagListBinding binding;

    private TagListViewModel mViewModel;

    private int colorSelected= Color.CYAN;
    private List<ColorAdapter.ColorTag> colorList;

    public static final long NO_NEED_EDIT=-1024L;

    public TagListFragment() {
        super();
        colorList = new ArrayList<>();
        colorList.add(new ColorAdapter.ColorTag("青色", Color.CYAN));
        colorList.add(new ColorAdapter.ColorTag("红色", Color.RED));
        colorList.add(new ColorAdapter.ColorTag("淡蓝", R.color.aqua));
        colorList.add(new ColorAdapter.ColorTag("绿色", R.color.green));
        colorList.add(new ColorAdapter.ColorTag("黑色", R.color.black));
        colorList.add(new ColorAdapter.ColorTag("蓝色", R.color.blue));
        colorList.add(new ColorAdapter.ColorTag("粉色", R.color.hotpink));
        colorList.add(new ColorAdapter.ColorTag("紫色", R.color.blueviolet));
    }

    public static TagListFragment newInstance() {
        return new TagListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //inflater.inflate(R.layout.fragment_tag_list, container, false);
        binding=FragmentTagListBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.tagToolbar);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TagListViewModel.class);
        // TODO: Use the ViewModel
        mViewModel.getLoading().observe(getViewLifecycleOwner(),o->{
            if(o){
                binding.progLoading.setVisibility(View.VISIBLE);
            }
            else{
                binding.progLoading.setVisibility(View.GONE);
            }
        });
        mViewModel.getFaliure().observe(getViewLifecycleOwner(),o->{
            if(o){
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("哦唷，崩溃了/(ㄒoㄒ)/~~");
                builder.setMessage("发生了一个未知错误，请重试");
                builder.setCancelable(false);
                builder.setPositiveButton("确定",((dialog, which) -> {
                    mViewModel.getFaliure().setValue(false);
                }));
            }
            else{
                //do nothing
            }
            initView();
        });

        binding.addTagFab.setOnClickListener(v->{
            showTagEditor(NO_NEED_EDIT,0,"",0);
        });
    }

    private void showColorPicker(View tagView){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("选择颜色");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.flyout_simple_color_picker_v2, null);
        builder.setView(dialogView);
        EditText colorHex = dialogView.findViewById(R.id.txb_color_hex_v);
        RecyclerView list = dialogView.findViewById(R.id.color_pane);
        View preView=dialogView.findViewById(R.id.block_selected_preview);
        preView.setBackgroundColor(colorSelected);
        ColorAdapter colorAdapter = loadColorPane(list);
        builder.setPositiveButton("确定", (dialog, which) ->{
            ColorAdapter adapter = (ColorAdapter) list.getAdapter();
            if (adapter != null) {
                int mPosition = adapter.getMPosition();
                if (mPosition >= 0) {
                    ColorAdapter.ColorTag tag = colorList.get(mPosition);
                    colorSelected = tag.getValue();
                }
            }
        });
        builder.setNegativeButton("取消", ((dialog, which) -> {

        }));
        AlertDialog ad = builder.show();
        colorAdapter.setSelectInterface(position -> {
            if (position >= 0) {
                ColorAdapter.ColorTag tag = colorList.get(position);
                colorSelected = tag.getValue();
                tagView.setBackgroundColor(colorSelected);
            }
            ad.dismiss();
        });
    }

    private ColorAdapter loadColorPane(RecyclerView view) {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        view.setLayoutManager(layoutManager);
        ColorAdapter adapter = new ColorAdapter(colorList);
        view.setAdapter(adapter);
        return adapter;
    }

    private void showTagEditor(Long tagId,int mode,String tagName, int color){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("添加标签");
        View view=LayoutInflater.from(getContext()).inflate(R.layout.flyout_edit_tag,null);
        EditText txeName=view.findViewById(R.id.txe_tag_name);
        View tagView=view.findViewById(R.id.block_tag_cp);

        if(mode==1){
            txeName.setText(tagName);
            tagView.setBackgroundColor(color);
        }

        tagView.setOnClickListener(mv->{
            showColorPicker(tagView);
        });
        //todo:点击修改颜色
        builder.setView(view);
        builder.setPositiveButton("确定",(dialog, which) -> {
            Tag tag=new Tag();
            tag.setTagName(txeName.getText().toString());
            tag.setColor(colorSelected);
            if(mode==0){
                mViewModel.addTag(tag);
            }
            else{
                tag.setId(tagId);
                mViewModel.updateTag(tag);
            }
        });
        builder.setNegativeButton("取消",(dialog, which) -> {

        });
        builder.show();
    }

    private void initView(){
        TagAdapter adapter=new TagAdapter(TagAdapter.callback);
        adapter.setClickCall(new TagAdapter.ClickCall() {
            @Override
            public void clickEdit(Long tid, String tagName, int color) {
                showTagEditor(tid,1,tagName,color);
            }

            @Override
            public void clickDelete(Long tid) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("确认删除");
                builder.setMessage("确认删除此标签？此操作不可逆！\n删除此标签后，使用该标签的笔记将被重置为默认标签。");
                builder.setPositiveButton("确定",(dialog, which) -> {
                    mViewModel.deleteTag(tid);
                });
                builder.setNegativeButton("取消",(dialog, which) -> {

                });
                builder.show();

            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.listTag.setLayoutManager(layoutManager);
        binding.listTag.setAdapter(adapter);
        mViewModel.getAllData().observe(getViewLifecycleOwner(), o -> {
            adapter.submitList((PagedList<Tag>) o);
            binding.listTag.scrollToPosition(0);
        });
    }

    private void switchLoadingStatus(boolean loading)
    {
        if(loading)
        {
            binding.progLoading.setVisibility(View.VISIBLE);
        }
        else{
            binding.progLoading.setVisibility(View.GONE);
        }
    }

    private void switchError(boolean failure)
    {
        mViewModel.getFaliure().setValue(failure);
    }
}