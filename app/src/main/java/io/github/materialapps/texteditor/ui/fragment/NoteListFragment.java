package io.github.materialapps.texteditor.ui.fragment;

import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.materialapps.texteditor.BaseApplication;
import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.databinding.FragmentNoteListBinding;
import io.github.materialapps.texteditor.logic.entity.Note;
import io.github.materialapps.texteditor.logic.entity.Tag;
import io.github.materialapps.texteditor.logic.entity.vo.NoteVO;
import io.github.materialapps.texteditor.ui.adapter.NoteAdapter;

public class NoteListFragment extends Fragment {

    private static final String TAG = "NoteListFragment";

    private FragmentNoteListBinding binding;

    private NoteListViewModel mViewModel;

    private SharedViewModel sharedViewModel;

    private ExecutorService exec = Executors.newCachedThreadPool();

    public static NoteListFragment newInstance() {
        return new NoteListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // inflater.inflate(R.layout.fragment_note_list, container, false);
        binding=FragmentNoteListBinding.inflate(inflater,container,false);
        View view=binding.getRoot();

        registerForContextMenu(binding.noteList);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this,new SavedStateViewModelFactory(getActivity().getApplication(), this)).get(NoteListViewModel.class);

        //获取共享vm
        sharedViewModel=new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        initView();

        binding.addNoteFab.setOnClickListener(v->{
            //db模式启动系统
            sharedViewModel.triggerNewNote();
            binding.noteListPanel.openPane();
        });

        Bundle arguments = getArguments();
        if(arguments!=null) {
            int mode = arguments.getInt("mode", BaseApplication.DB_EDIT);
            if(mode==BaseApplication.DB_EDIT){
                Log.d(TAG, "onActivityCreated: 艹");
                sharedViewModel.triggerNewNote();
            }
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.note_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_delete_note:{
                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("确认删除？");
                builder.setMessage("此操作不可恢复！");
                builder.setPositiveButton("确定",((dialog, which) -> {
                    exec.execute(()->{
                        int mPosition = ((NoteAdapter) binding.noteList.getAdapter()).getMPosition();
                        Long mid=((NoteAdapter) binding.noteList.getAdapter()).getMId();
                        int rows= mViewModel.delete(mid);
                        getActivity().runOnUiThread(()->{
                            if(rows>0){
                                Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                sharedViewModel.triggerNewNote();
                            }
                            else{
                                Toast.makeText(getContext(), "发生错误！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }));
                builder.setNegativeButton("取消",((dialog, which) -> {

                }));
                builder.show();
                break;
            }
            default:{break;}
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(binding.noteList);
    }


    private void initView(){
        NoteAdapter adapter=new NoteAdapter(NoteAdapter.callback);

        adapter.setIntf((index, vo) -> {
            Long id = vo.getId();

            exec.execute(()->{
                NoteVO qvo = mViewModel.getById(id);
                sharedViewModel.setDbMode(true);
                sharedViewModel.getCurrentNote().postValue(qvo);
                getActivity().runOnUiThread(()->{
                    binding.noteListPanel.openPane();
                });
            });
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.noteList.setLayoutManager(layoutManager);
        binding.noteList.setAdapter(adapter);
        mViewModel.getAllData().observe(getViewLifecycleOwner(),o->{
            adapter.submitList((PagedList<NoteVO>) o);
            binding.noteList.scrollToPosition(0);
        });
    }
}