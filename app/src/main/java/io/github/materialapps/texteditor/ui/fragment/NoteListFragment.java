package io.github.materialapps.texteditor.ui.fragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    public static NoteListFragment newInstance() {
        return new NoteListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // inflater.inflate(R.layout.fragment_note_list, container, false);
        binding=FragmentNoteListBinding.inflate(inflater,container,false);
        View view=binding.getRoot();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NoteListViewModel.class);
        // TODO: Use the ViewModel

        //获取共享vm
        sharedViewModel=new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


    }

    private void initView(){
        NoteAdapter adapter=new NoteAdapter(NoteAdapter.callback);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.noteList.setLayoutManager(layoutManager);
        binding.noteList.setAdapter(adapter);
        mViewModel.getAllData().observe(getViewLifecycleOwner(),o->{
            adapter.submitList((PagedList<NoteVO>) o);
            binding.noteList.scrollToPosition(0);
        });
    }
}