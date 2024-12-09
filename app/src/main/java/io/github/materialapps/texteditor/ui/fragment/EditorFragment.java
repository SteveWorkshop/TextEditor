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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import io.github.materialapps.texteditor.BaseApplication;
import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.databinding.FragmentEditorBinding;
import io.github.materialapps.texteditor.logic.render.FormatRender;
import io.github.materialapps.texteditor.util.StatusUtil;
import io.noties.markwon.Markwon;

public class EditorFragment extends Fragment {

    private boolean showPreview;
    private boolean markdownMode;

    private static final String TAG = "EditorFragment";

    public static final int REQUEST_CODE = 114514;
    public static final int OPEN_FILE_DIALOG = 1919810;
    public static final int SAVE_FILE_DIALOG = 5201314;

    private FormatRender formatRender=new FormatRender();

    private FragmentEditorBinding binding;

    private EditorViewModel mViewModel;

    public static EditorFragment newInstance() {
        return new EditorFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditorBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.editMainToolbar);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditorViewModel.class);

        if (!Environment.isExternalStorageManager()) {
            //申请权限
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
            builder.setTitle("需要权限");
            builder.setMessage("请在接下来的操作中，选择允许访问所有文件。");
            builder.setCancelable(false);
            builder.setPositiveButton("同意并授权", ((dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }));
            builder.setNegativeButton("不同意，退出软件", ((dialog, which) -> {
                getActivity().finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }));
            builder.show();
        }

        mViewModel.getShow2Panel().observe(getViewLifecycleOwner(), o -> {
            showPreview = o;
            if (!o) {
                //binding.panelMain.closePane();
                binding.panelPreview.setVisibility(View.GONE);
            } else {
                binding.panelPreview.setVisibility(View.VISIBLE);
                //binding.panelMain.openPane();
            }
        });

        mViewModel.getMarkdownMode().observe(getViewLifecycleOwner(), o -> {
            markdownMode = o;
//            if(o){
//                //重新渲染
//                Markwon markwon=Markwon.create(binding.txbPrevArea.getContext());
//                markwon.setMarkdown(binding.txbPrevArea,mViewModel.getCurrentText().getValue());
//            }
//            else{
//                binding.txbPrevArea.setText(mViewModel.getCurrentText().getValue());
//            }
        });

        binding.txeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.onTextChanged(s.toString());//todo:bad behaviour
                mViewModel.changeSaveStatus(BaseApplication.MODIFIED);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mViewModel.getCurrentText().observe(getViewLifecycleOwner(), o -> {
            if (showPreview) {
                Log.d(TAG, "onActivityCreated: ==========================预览工作=========================");
                if (markdownMode) {
                    Markwon markwon = Markwon.create(binding.txbPrevArea.getContext());
                    markwon.setMarkdown(binding.txbPrevArea, o);
                } else {
                    binding.txbPrevArea.setText(o);
                }
            }

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
        switch (requestCode) {
            case REQUEST_CODE: {
                if (!Environment.isExternalStorageManager()) {
                    Toast.makeText(getContext(), "拒绝权限后本软件将无法工作", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                break;
            }
            /**
             * ==========================================================
             *                   关注社区不迷路，带你骑猪上高速！
             *                   千山万水总是情，点个star行不行！
             *                   百年修得共枕眠，刷刷星星不要钱！
             *                   star刷一刷，能活八十八，pr走一走，能到九十九！
             *                   走过南，闯过北，提点pr不吃亏！
             *                   人人使，人人用，平时想找就难碰。
             *                   往前走，别后退，咱家软件不收费！
             * ==========================================================
             */
            case OPEN_FILE_DIALOG: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri fileUri = data.getData();
                    mViewModel.setInstanceStatus(BaseApplication.OPEN_FILE);
                    mViewModel.setFileUriPath(fileUri);
                    //存储URI以便保存文档
                    ContentResolver crs = getActivity().getContentResolver();
                    try {
                        InputStream is = crs.openInputStream(fileUri);
                        StringBuffer sb = new StringBuffer();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        reader.close();
                        binding.txeEditor.setText(sb.toString());
                    } catch (IOException e) {
                        Log.e(TAG, "onActivityResult: ", e);
                    }
                } else {
                    Toast.makeText(getContext(), "无法打开文件，它可能不是文本文档或已被移动、删除或文件内容已损坏。", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case SAVE_FILE_DIALOG: {
                if (resultCode == Activity.RESULT_OK) {
                    mViewModel.changeSaveStatus(BaseApplication.UNMODIFIED);
                    Toast.makeText(getContext(), "Ciallo~", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "矮油，小姐姐遇到问题了", Toast.LENGTH_SHORT).show();
                }
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.edit_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        handleMenu(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    private void refreshStatus() {
        binding.txeEditor.setText(mViewModel.getCurrentText().getValue());
    }

    private void saveDocument(Uri fileUriPath) {
        try {
            ContentResolver crs = getActivity().getContentResolver();
            OutputStream os = crs.openOutputStream(fileUriPath);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            String content = binding.txeEditor.getText().toString();
            writer.write(content);
            writer.flush();
            writer.close();

            mViewModel.changeSaveStatus(BaseApplication.UNMODIFIED);
            Toast.makeText(getContext(), "Ciallo~", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "handleMenu: ", e);
        }
    }

    private void saveAs() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "新建文本文档.txt");
        startActivityForResult(intent, SAVE_FILE_DIALOG);
    }


    private void handleMenu(Integer id) {
        switch (id) {
            case R.id.menu_open_file: {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                //todo：防止乱来
                startActivityForResult(intent, OPEN_FILE_DIALOG);
                break;
            }
            case R.id.menu_save_file: {

                Uri fileUriPath = mViewModel.getFileUriPath();
                if (fileUriPath != null) {
                    saveDocument(fileUriPath);
                } else {
                    //判断是意外事件还是新文件
                    if (mViewModel.getInstanceStatus() == BaseApplication.NEW_FILE) {
                        saveAs();
                    } else {
                        Toast.makeText(getContext(), "发生错误，文件可能被移动，删除或重命名，请尝试另存为文档！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case R.id.menu_save_as_file: {
                saveAs();
                break;
            }

            case R.id.menu_exit: {
                boolean instanceStatus = mViewModel.getInstanceStatus();
                boolean saveStatus = mViewModel.getSaveStatus();
                int ret = StatusUtil.checkSaveStatus(instanceStatus, saveStatus);
                if (ret != StatusUtil.NO_ACTION) {

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                    builder.setTitle("是否保存更改？");
                } else {
                    getActivity().finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                break;
            }

            case R.id.menu_bold: {
                //如果选中了加粗选中，否则当前光标
                formatRender.renderBold(binding.txeEditor);
                break;
            }
            case R.id.menu_italic:{
                formatRender.renderItalic(binding.txeEditor);
                break;
            }

            case R.id.menu_toggle_preview: {
                Boolean twoPanel = mViewModel.getShow2Panel().getValue();
                mViewModel.getShow2Panel().setValue(!twoPanel);
                break;
            }

            case R.id.menu_preview_mode_settings: {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("选择预览模式");
                Boolean mode = mViewModel.getMarkdownMode().getValue();
                int checked = (mode ? 0 : 1);
                builder.setSingleChoiceItems(new String[]{"Markdown", "纯文本"}, checked, (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            mViewModel.getMarkdownMode().setValue(true);
                            break;
                        }
                        case 1: {
                            mViewModel.getMarkdownMode().setValue(false);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                });
                builder.setCancelable(false);
                builder.setPositiveButton("确定", (dialog, which) -> {

                });
                builder.show();
                break;
            }

            default: {
                break;
            }
        }
    }
}