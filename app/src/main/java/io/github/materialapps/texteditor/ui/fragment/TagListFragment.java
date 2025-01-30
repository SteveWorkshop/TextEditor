package io.github.materialapps.texteditor.ui.fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.databinding.FragmentTagListBinding;
import io.github.materialapps.texteditor.logic.entity.Tag;
import io.github.materialapps.texteditor.ui.adapter.TagAdapter;

public class TagListFragment extends Fragment {

    private FragmentTagListBinding binding;

    private TagListViewModel mViewModel;

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
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
            builder.setTitle("添加标签");
            View view=LayoutInflater.from(getContext()).inflate(R.layout.flyout_edit_tag,null);
            EditText txeName=view.findViewById(R.id.txe_tag_name);
            int colorSelected= Color.CYAN;
            //todo:点击修改颜色
            builder.setView(view);
            builder.setPositiveButton("确定",(dialog, which) -> {
                Tag tag=new Tag();
                tag.setTagName(txeName.getText().toString());
                tag.setColor(colorSelected);
                mViewModel.addTag(tag);
            });
            builder.setNegativeButton("取消",(dialog, which) -> {

            });
            builder.show();
        });
    }

    private void initView(){
        TagAdapter adapter=new TagAdapter(TagAdapter.callback);
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