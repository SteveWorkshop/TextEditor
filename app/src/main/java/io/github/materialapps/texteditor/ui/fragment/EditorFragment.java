package io.github.materialapps.texteditor.ui.fragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.databinding.FragmentEditorBinding;
import io.noties.markwon.Markwon;

public class EditorFragment extends Fragment {

    private FragmentEditorBinding binding;

    private EditorViewModel mViewModel;

    public static EditorFragment newInstance() {
        return new EditorFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding=FragmentEditorBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditorViewModel.class);
        // TODO: Use the ViewModel
        binding.txeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.onTextChanged(s.toString());//todo:bad behaviour
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mViewModel.getCurrentText().observe(getViewLifecycleOwner(),o->{
            Markwon markwon=Markwon.create(binding.txbPrevArea.getContext());
            markwon.setMarkdown(binding.txbPrevArea,(String)o);
        });
    }

}