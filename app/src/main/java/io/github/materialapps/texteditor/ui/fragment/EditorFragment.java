package io.github.materialapps.texteditor.ui.fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.databinding.FragmentEditorBinding;
import io.noties.markwon.Markwon;

public class EditorFragment extends Fragment {

    private static final String TAG = "EditorFragment";

    public static final int REQUEST_CODE=114514;
    public static final int OPEN_FILE_DIALOG=1919810;

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
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.editMainToolbar);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditorViewModel.class);

        if(!Environment.isExternalStorageManager())
        {
            //申请权限
            MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
            builder.setTitle("需要权限");
            builder.setMessage("请在接下来的操作中，选择允许访问所有文件。");
            builder.setCancelable(false);
            builder.setPositiveButton("同意并授权",((dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" +getContext().getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }));
            builder.setNegativeButton("不同意，退出软件",((dialog, which) -> {
                getActivity().finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }));
            builder.show();
        }

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

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE:{
                if(!Environment.isExternalStorageManager()){
                    Toast.makeText(getContext(), "拒绝权限后本软件将无法工作", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                break;
            }
            /**
             * ==========================================================
             *                   关注我不迷路，带你骑猪上高速！
             * ==========================================================
             */
            case OPEN_FILE_DIALOG:{
                if (resultCode == Activity.RESULT_OK && data != null){
                    Uri fileUri = data.getData();
                    //存储URI以便保存文档
                    ContentResolver crs=getActivity().getContentResolver();
                    try {
                        InputStream is=crs.openInputStream(fileUri);
                        StringBuffer sb=new StringBuffer();
                        BufferedReader reader=new BufferedReader(new InputStreamReader(is));
                        String line;
                        while ((line=reader.readLine())!=null){
                            sb.append(line);
                        }
                        reader.close();
                        binding.txeEditor.setText(sb.toString());
                    } catch (IOException e) {
                        Log.e(TAG, "onActivityResult: "+e.getMessage());
                    }
                }
                    break;
            }
            default:{break;}
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.edit_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        handleMenu(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    private void refreshStatus()
    {
        binding.txeEditor.setText(mViewModel.getCurrentText().getValue());
    }


    private void handleMenu(Integer id){
        switch (id){
            case R.id.menu_open_file:{
                //todo:提示存储当前文档
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                //todo：防止乱来
                startActivityForResult(intent,OPEN_FILE_DIALOG);
                break;
            }
            default:{break;}
        }
    }
}