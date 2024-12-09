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
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.github.materialapps.texteditor.R;

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
import io.github.materialapps.texteditor.util.ClipBrdUtil;
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
                if(before!=0||count!=0){
                    mViewModel.changeSaveStatus(BaseApplication.MODIFIED);
                }
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
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //关闭以前的文件
                    Uri fileUri = data.getData();
                    mViewModel.setInstanceStatus(BaseApplication.OPEN_FILE);
                    mViewModel.setFileUriPath(fileUri);
                    saveDocument(fileUri);
                    mViewModel.changeSaveStatus(BaseApplication.UNMODIFIED);
                    //写文件
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

    private void saveUni(){
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
        int sp = binding.txeEditor.getSelectionStart();
        int ep = binding.txeEditor.getSelectionEnd();
        switch (id) {
            case R.id.menu_open_file: {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                //todo：防止乱来
                startActivityForResult(intent, OPEN_FILE_DIALOG);
                break;
            }
            case R.id.menu_save_file: {
                saveUni();
                break;
            }
            case R.id.menu_save_as_file: {
                saveAs();
                break;
            }

            case R.id.menu_exit: {
                boolean instanceStatus = mViewModel.getInstanceStatus();
                boolean saveStatus = mViewModel.getSaveStatus();
                Log.d(TAG, "handleMenu: "+instanceStatus+","+saveStatus);
                int ret = StatusUtil.checkSaveStatus(instanceStatus, saveStatus);
                if (ret != StatusUtil.NO_ACTION) {

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                    builder.setTitle("警告");
                    builder.setMessage("是否保存对当前文档修改？");
                    builder.setPositiveButton("是",(dialog, which) -> {
                        saveUni();
                        getActivity().finish();
                        //android.os.Process.killProcess(android.os.Process.myPid());
                    });
                    builder.setNegativeButton("否",(dialog, which) -> {
                        getActivity().finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    });
                    builder.setNeutralButton("取消",(dialog, which) -> {

                    });
                    builder.show();

                } else {
                    getActivity().finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                break;
            }

            case R.id.menu_select_all:{
                binding.txeEditor.selectAll();
                break;
            }

            case R.id.menu_copy:{
                if(sp<0||ep<0||sp==ep){break;}
                CharSequence cp = binding.txeEditor.getEditableText().subSequence(sp, ep);
                ClipBrdUtil clipBrdUtil=new ClipBrdUtil(getContext());
                clipBrdUtil.copyToClipboard(cp.toString());
                break;
            }

            case R.id.menu_cut:{
                if(sp<0||ep<0||sp==ep){break;}
                Editable editArea = binding.txeEditor.getEditableText();
                CharSequence cp = editArea.subSequence(sp, ep);
                ClipBrdUtil clipBrdUtil=new ClipBrdUtil(getContext());
                clipBrdUtil.copyToClipboard(cp.toString());
                editArea.replace(sp,ep,"");
                break;
            }

            case R.id.menu_paste:{
                if(sp<0||ep<0){break;}
                ClipBrdUtil clipBrdUtil=new ClipBrdUtil(getContext());
                String paste = clipBrdUtil.paste();
                Editable editArea = binding.txeEditor.getEditableText();
                if(!TextUtils.isEmpty(paste)){
                    if(sp==ep){
                        editArea.insert(sp,paste);
                    }
                    else{
                        editArea.replace(sp,ep,paste);
                    }
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
            case R.id.menu_level_1:{
                formatRender.renderHeader(binding.txeEditor,1);
                break;
            }
            case R.id.menu_level_2:{
                formatRender.renderHeader(binding.txeEditor,2);
                break;
            }
            case R.id.menu_level_3:{
                formatRender.renderHeader(binding.txeEditor,3);
                break;
            }

            case R.id.menu_level_custom:{
                EditText editText = new EditText(getContext());
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("设置标题级别");
                builder.setView(editText);
                builder.setPositiveButton("确定",(dialog, which) -> {
                    String string = editText.getText().toString();
                    try {
                        int i = Integer.parseInt(string);
                        if(i>10){
                            Toast.makeText(getContext(), "这TM绝对是来捣乱的！", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            formatRender.renderHeader(binding.txeEditor,i);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "哒咩~꒰๑´•.̫ • `๑꒱", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("取消",(dialog, which) -> {});
                builder.show();
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

            case R.id.menu_add_ul:{
                formatRender.renderUl(binding.txeEditor);
                break;
            }
            case R.id.menu_add_ol:{
                EditText editText = new EditText(getContext());
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("设置编号值");
                builder.setView(editText);
                builder.setPositiveButton("确定",(dialog, which) -> {
                    String string = editText.getText().toString();
                    try {
                        int i = Integer.parseInt(string);
                        if(i>10){
                            Toast.makeText(getContext(), "这TM绝对是来捣乱的！", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            formatRender.renderOl(binding.txeEditor,i);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "哒咩~꒰๑´•.̫ • `๑꒱", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("取消",(dialog, which) -> {});
                builder.show();
                break;
            }
            case R.id.menu_add_line:{
                formatRender.renderLine(binding.txeEditor);
                break;
            }

            case R.id.menu_about_us:{
                View dialogView=LayoutInflater.from(getContext()).inflate(R.layout.flyout_about_us,null);
                TextView textView = dialogView.findViewById(R.id.txb_my_link);
                TextView textView2=dialogView.findViewById(R.id.txb_nova);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView2.setMovementMethod(LinkMovementMethod.getInstance());

                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("关于此软件");
                builder.setView(dialogView);
                builder.setPositiveButton("确定",(dialog, which) -> {

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