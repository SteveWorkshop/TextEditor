package io.github.materialapps.texteditor.ui.fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
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
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.github.materialapps.texteditor.R;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.commonmark.node.Node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.github.materialapps.texteditor.BaseApplication;
import io.github.materialapps.texteditor.databinding.FragmentEditorBinding;
import io.github.materialapps.texteditor.logic.converter.PDFConverter;
import io.github.materialapps.texteditor.logic.network.AGIClient;
import io.github.materialapps.texteditor.logic.network.impl.GeminiClient;
import io.github.materialapps.texteditor.logic.render.FormatRender;
import io.github.materialapps.texteditor.ui.MainActivity;
import io.github.materialapps.texteditor.ui.flyout.GeminiRewriteFlyout;
import io.github.materialapps.texteditor.util.ClipBrdUtil;
import io.github.materialapps.texteditor.util.IDUtil;
import io.github.materialapps.texteditor.util.StatusUtil;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class EditorFragment extends Fragment {

    private boolean showPreview;
    private boolean markdownMode;

    private AGIClient client;

    private static final String TAG = "EditorFragment";

    public static final int REQUEST_CODE = 114514;
    public static final int OPEN_FILE_DIALOG = 1919810;
    public static final int SAVE_FILE_DIALOG = 5201314;
    public static final int SAVE_AND_EXIT_DIALOG=233666;
    public static final int ADD_IMG_DIALOG=262518;

    private FormatRender formatRender=new FormatRender();

    private ExecutorService exec = Executors.newCachedThreadPool();

    private Markwon markwon;

    private FragmentEditorBinding binding;

    private EditorViewModel mViewModel;

    private SharedViewModel sharedViewModel;

    private WindowManager wm;

    public static EditorFragment newInstance() {
        return new EditorFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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


    @SuppressLint("NewApi")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        markwon=Markwon.builder(binding.txbPrevArea.getContext())
                // create default instance of TablePlugin
                .usePlugin(TablePlugin.create(binding.txbPrevArea.getContext()))
                .usePlugin(GlideImagesPlugin.create(Glide.with(getContext())))
                .build();

        wm=getActivity().getWindowManager();
        //mViewModel = new ViewModelProvider(this).get(EditorViewModel.class);
        mViewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(getActivity().getApplication(), this)).get(EditorViewModel.class);

        sharedViewModel=new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

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


        //UI缩放
        mViewModel.getUiSize().observe(getViewLifecycleOwner(),o->{
            if(o<BaseApplication.MIN_UI_SIZE||o>BaseApplication.MAX_UI_SIZE){
                //不RUN许
                return;
            }
            else{
                binding.txeEditor.setTextSize(o);
                binding.txbPrevArea.setTextSize(o);
            }
        });

        client=new GeminiClient(mViewModel.getApiKey());
        client.build();
        binding.flyoutAi.init(client);


        binding.flyoutAi.setCallback(new GeminiRewriteFlyout.Foo() {
            @Override
            public void onConfirm(String result) {
                if(binding.flyoutAi.isNeedToInject()){
                    int sp = binding.txeEditor.getSelectionStart();
                    int ep = binding.txeEditor.getSelectionEnd();
                    if(sp<0||ep<0){
                        Toast.makeText(getContext(), "请选择要插入/替换的文本", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        formatRender.rewrite(binding.txeEditor,result);
                    }
                }
                binding.flyoutAi.setNeedToInject(false);
            }

            @Override
            public void onCancel() {
                binding.flyoutAi.setBuffer(null);
                binding.panelSidebar.setVisibility(View.GONE);
            }

            @Override
            public void onClose() {
                binding.flyoutAi.setBuffer(null);
                binding.panelSidebar.setVisibility(View.GONE);
            }
        });



        binding.uiTools.btnExitFind.setOnClickListener(v->{
            binding.panelToolsWrapper.setVisibility(View.GONE);
        });
        Disposable subscribe = Observable.create((ObservableOnSubscribe<String>) emitter -> binding.txeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before != 0 || count != 0) {
                    mViewModel.changeSaveStatus(BaseApplication.MODIFIED);
                }
                emitter.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        })).debounce(100, TimeUnit.MILLISECONDS).subscribe(s -> mViewModel.getCurrentText().postValue(s));


//        binding.txeEditor.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                mViewModel.onTextChanged(s.toString());
//                if(before!=0||count!=0){
//                    mViewModel.changeSaveStatus(BaseApplication.MODIFIED);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        mViewModel.getCurrentText().observe(getViewLifecycleOwner(), o -> {
            if (showPreview) {
                if (markdownMode) {
                    String raw = binding.txeEditor.getText().toString();
                    exec.execute(()->{
                        Node node = markwon.parse(raw);
                        Spanned markdown = markwon.render(node);
                        getActivity().runOnUiThread(()->{
                            markwon.setParsedMarkdown(binding.txbPrevArea, markdown);
                        });
                    });

                } else {
                    binding.txbPrevArea.setText(o);
                }
            }
        });

        //如果是打开文件，则恢复数据
        Bundle arguments = getArguments();
        if(arguments!=null){
            int mode=arguments.getInt("mode",BaseApplication.EXTERNAL_EDIT_MODE);
            //todo:预留内联模式

            Uri uri=arguments.getParcelable("filePath",Uri.class);
            if(uri!=null){
                openDocument(uri);
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Log.d(TAG, "onConfigurationChanged: 旋转！！！！！！！");
        if(wm!=null){
            WindowMetrics windowMetrics = wm.getCurrentWindowMetrics();
            Rect bounds = windowMetrics.getBounds();
            int windowWidth = bounds.width();
            if(windowWidth<=600){
                //切换垂直排列
                binding.panelContainer.setOrientation(LinearLayout.VERTICAL);
            }
            else{
                binding.panelContainer.setOrientation(LinearLayout.HORIZONTAL);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
                    openDocument(fileUri);
                } else {
                    Toast.makeText(getContext(), "无法打开文件，它可能不是文本文档或已被移动、删除或文件内容已损坏。", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case SAVE_FILE_DIALOG: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //关闭以前的文件
                    Uri fileUri = data.getData();
                    saveDocument(fileUri);
                    mViewModel.setInstanceStatus(BaseApplication.OPEN_FILE);
                    mViewModel.setFileUriPath(null);
                    mViewModel.changeSaveStatus(BaseApplication.UNMODIFIED);
                    //写文件
                    Toast.makeText(getContext(), "Ciallo~", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "矮油，小姐姐遇到问题了", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case SAVE_AND_EXIT_DIALOG:{
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri fileUri = data.getData();
                    saveDocument(fileUri);
                } else {
                    Toast.makeText(getContext(), "矮油，小姐姐遇到问题了", Toast.LENGTH_SHORT).show();
                }
                getActivity().finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
            }
            case ADD_IMG_DIALOG:{
                //formatRender.renderLine(binding.txeEditor);
                if (resultCode == Activity.RESULT_OK && data != null){
                    try {
                        Uri uri = data.getData();
                        Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(uri));
                        formatRender.renderImg(binding.txeEditor,bitmap,getActivity());
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "onActivityResult: ", e);
                    }

                }
                break;
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

    private void safeSwitch(Bar bar,boolean exit){
        boolean instanceStatus = mViewModel.getInstanceStatus();
        boolean saveStatus = mViewModel.getSaveStatus();
        Log.d(TAG, "handleMenu: "+instanceStatus+","+saveStatus);
        int ret = StatusUtil.checkSaveStatus(instanceStatus, saveStatus);
        if (ret != StatusUtil.NO_ACTION) {

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
            builder.setTitle("警告");
            builder.setMessage("是否保存对当前文档修改？");
            builder.setPositiveButton("是",(dialog, which) -> {
                saveUni(exit);
                if(!exit){
                    bar.foo();
                }
            });
            builder.setNegativeButton("否",(dialog, which) -> {
                if(!exit){
                    bar.foo();
                }
                else{
                    getActivity().finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            builder.setNeutralButton("取消",(dialog, which) -> {

            });
            builder.show();

        } else {
            bar.foo();
        }
    }

    private void openDocument(Uri fileUri){
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
    }


    private void saveUni(boolean exit){
        Uri fileUriPath = mViewModel.getFileUriPath();
        if (fileUriPath != null) {
            saveDocument(fileUriPath);
            if(exit){
                getActivity().finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        } else {
            //判断是意外事件还是新文件
            if (mViewModel.getInstanceStatus() == BaseApplication.NEW_FILE) {
                Toast.makeText(getContext(), "另存为", Toast.LENGTH_SHORT).show();
                saveAs(exit);
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
            //Toast.makeText(getContext(), "你们v", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "handleMenu: ", e);
        }
    }

    private void saveAs(boolean exit) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "新建文本文档.txt");
        if(!exit){
            startActivityForResult(intent, SAVE_FILE_DIALOG);
        }
        else{
            startActivityForResult(intent, SAVE_AND_EXIT_DIALOG);
        }
    }


    private void exportPDF(){
        String text=binding.txeEditor.getText().toString();
        Log.d(TAG, "exportPDF: TEXT"+text);
        if(TextUtils.isEmpty(text))
        {
            return;
        }
        exec.execute(()->{
            String fileName="新建pdf文档"+ IDUtil.getUUID()+".pdf";
            PDFConverter converter=new PDFConverter();
            converter.setCallback(new PDFConverter.Callback() {
                @Override
                public void onSuccess() {
                    getActivity().runOnUiThread(()->{
                        Toast.makeText(getContext(), "OK", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure() {
                    getActivity().runOnUiThread(()->{
                        Toast.makeText(getContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    });
                }
            });
            converter.convert(text,fileName);
        });

    }

    private void startAiFlyout(){
        binding.panelSidebar.setVisibility(View.VISIBLE);
        binding.flyoutAi.setActivity(getActivity());
    }

    private void handleMenu(Integer id) {
        int sp = binding.txeEditor.getSelectionStart();
        int ep = binding.txeEditor.getSelectionEnd();
        switch (id) {
            case R.id.menu_new_file:{
                safeSwitch(()->{
                    //清空内容
                    binding.txeEditor.setText("");
                    mViewModel.getCurrentText().setValue("");
                    mViewModel.getCurrentFileUri().setValue(null);
                    mViewModel.getInstanceType().setValue(BaseApplication.NEW_FILE);
                    mViewModel.getHasEdited().setValue(false);
                },false);
                break;
            }

            case R.id.menu_open_file: {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, OPEN_FILE_DIALOG);
                break;
            }
            case R.id.menu_save_file: {
                saveUni(false);
                break;
            }
            case R.id.menu_save_as_file: {
                saveAs(false);
                break;
            }

            case R.id.menu_export:{
                exportPDF();
                break;
            }

            case R.id.menu_exit: {
                safeSwitch(()->{
                },true);
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
                            Toast.makeText(getContext(), "不RUN许设置过多层级！", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            formatRender.renderHeader(binding.txeEditor,i);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "不RUN许设置过多层级！", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("取消",(dialog, which) -> {});
                builder.show();
                break;
            }

            case R.id.menu_zoom_in:{
                mViewModel.incSize();
                break;
            }

            case R.id.menu_zoom_out:{
                mViewModel.decSize();
                break;
            }

            case R.id.menu_zoom_custom:{
                View view=LayoutInflater.from(getContext()).inflate(R.layout.flyout_ui_zoom_picker,null);
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("选择字号");
                builder.setView(view);
                builder.setPositiveButton("确定",(dialog, which) -> {
                    EditText viewById = view.findViewById(R.id.txb_zoom);
                    String string = viewById.getText().toString();
                    try {
                        int zoom = Integer.parseInt(string);
                        mViewModel.getUiSize().setValue(zoom);
                    }
                    catch (NumberFormatException e){
                        Toast.makeText(getContext(), "数值过大！", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("取消",(dialog, which) -> {

                });
                builder.setNeutralButton("重置默认",(dialog, which) -> {
                    mViewModel.getUiSize().setValue(EditorViewModel.ZOOM_DEFAULT);
                });
                builder.show();
                break;
            }

            case R.id.menu_find_and_rp:{
                binding.panelToolsWrapper.setVisibility(View.VISIBLE);
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
                        if(i<0){
                            Toast.makeText(getContext(), "这TM绝对是来捣乱的！", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            formatRender.renderOl(binding.txeEditor,i);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "输入值过大，哒咩~꒰๑´•.̫ • `๑꒱", Toast.LENGTH_SHORT).show();
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

            case R.id.menu_add_image:{

                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("image/*");
                startActivityForResult(chooseFile,ADD_IMG_DIALOG);
                //Toast.makeText(getContext(), "暂不支持，敬请期待！", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.menu_add_table:{
                View view=LayoutInflater.from(getContext()).inflate(R.layout.flyout_table_selector,null);
                EditText rowIn=view.findViewById(R.id.txb_row);
                EditText colIn=view.findViewById(R.id.txb_col);

                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("插入表格");
                builder.setView(view);
                builder.setPositiveButton("确定",(dialog, which) -> {
                    try {
                        String rowSt = rowIn.getText().toString();
                        String colSt = colIn.getText().toString();
                        int row = Integer.parseInt(rowSt);
                        int col = Integer.parseInt(colSt);
                        if(row<2||col<2){
                            Toast.makeText(getContext(), "Markdown不RUN许设置单行单列表格", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            formatRender.renderTable(binding.txeEditor,row,col);
                        }
                    }
                    catch (NumberFormatException e){
                        Toast.makeText(getContext(), "输入值过大，哒咩~꒰๑´•.̫ • `๑꒱", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("取消",(dialog, which) -> {});
                builder.show();
                break;
            }
            
            case R.id.ai_rewrite:{
                if(TextUtils.isEmpty(mViewModel.getApiKey()))
                {
                    Toast.makeText(getContext(), "未设置Gemini API密钥，此功能不可用", Toast.LENGTH_SHORT).show();
                    break;
                }
                if(sp<0||ep<0||sp==ep){
                    Toast.makeText(getContext(), "请先选择内容", Toast.LENGTH_SHORT).show();
                    break;
                }
                binding.flyoutAi.setBuffer(String.valueOf(binding.txeEditor.getEditableText().subSequence(sp,ep)));
                startAiFlyout();
                binding.flyoutAi.rewrite();
                break;
            }

            case R.id.ai_conclusion:{
                if(TextUtils.isEmpty(mViewModel.getApiKey()))
                {
                    Toast.makeText(getContext(), "未设置Gemini API密钥，此功能不可用", Toast.LENGTH_SHORT).show();
                    break;
                }
                String text="";
                if(sp<0||ep<0||sp==ep){
                    text=binding.txeEditor.getText().toString();
                }
                else{
                    text= String.valueOf(binding.txeEditor.getEditableText().subSequence(sp,ep));
                }
                Log.d(TAG, "handleMenu: 要总结的文本："+text);
                startAiFlyout();
                binding.flyoutAi.summarize(text);
                break;
            }

            case R.id.ai_write_by_kb:{
                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("输入提示词");
                View dialogView=LayoutInflater.from(getContext()).inflate(R.layout.flyout_new_article,null);
                EditText txe=dialogView.findViewById(R.id.txe_help_write_prompt);
                builder.setView(dialogView);
                builder.setPositiveButton("确定",((dialog, which) -> {
                    String hint = txe.getText().toString();
                    if(!TextUtils.isEmpty(hint)){
                        startAiFlyout();
                        binding.flyoutAi.aiGen(hint);
                    }
                }));
                builder.setNegativeButton("取消",((dialog, which) -> {

                }));
                builder.show();
                break;
            }

            case R.id.menu_options:{
                ((MainActivity)(getActivity())).getNavController().navigate(R.id.settingsFragment);
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

    public interface Bar{
        void foo();
    }
}