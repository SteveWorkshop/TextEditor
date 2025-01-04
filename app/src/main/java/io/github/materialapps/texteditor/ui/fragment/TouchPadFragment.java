package io.github.materialapps.texteditor.ui.fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.databinding.FragmentTouchPadBinding;
import io.github.materialapps.texteditor.ui.adapter.ColorAdapter;
import io.github.materialapps.texteditor.ui.flyout.CanvasFlyout;
import io.github.materialapps.texteditor.util.IDUtil;
import io.github.materialapps.texteditor.util.ScreenShotUtil;

public class TouchPadFragment extends Fragment {


    public static final int TAKE_PHOTO = 1;
    public static final int SELECT_FILE = 2;

    private FragmentTouchPadBinding binding;

    private TouchPadViewModel mViewModel;

    private CanvasFlyout canvasFlyout;

    private int colorSelected = Color.CYAN;

    private Uri imageUri;
    private File outputFile;

    private List<ColorAdapter.ColorTag> colorList;

    public TouchPadFragment(){
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

    public static TouchPadFragment newInstance() {
        return new TouchPadFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //inflater.inflate(R.layout.fragment_touch_pad, container, false);
        binding=FragmentTouchPadBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.drawToolbar);
        setHasOptionsMenu(true);
        canvasFlyout = binding.canvas;

        binding.btnPenI.setOnClickListener(v -> {
            showPopupMenu(binding.btnPenI);
        });


        binding.btnEraserI.setOnClickListener(v -> {
            if (binding.btnEraserI.isSelected()) {
                //todo: 记住上一个选择颜色
                canvasFlyout.setPaintColor(Color.CYAN);
                binding.btnEraserI.setSelected(false);
            } else {
                canvasFlyout.setPaintColor(canvasFlyout.getBackGround());
                binding.btnEraserI.setSelected(true);
            }
        });

        binding.btnInsertImageI.setOnClickListener(v->{

            selectInsertSource(binding.btnInsertImageI);
        });

        binding.btnCleanI.setOnClickListener(v -> {
            canvasFlyout.clearAll();
        });

        binding.btnUndoI.setOnClickListener(v -> {
            canvasFlyout.undo();
        });


        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.draw_export_menu, menu);
    }

    @SuppressLint("WrongThread")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        View pic = binding.canvas;
        Bitmap bitmap = ScreenShotUtil.createBitMapScreenSize(pic);
        switch (item.getItemId()) {
            case R.id.album_exp_menu: {
                //Toast.makeText(getContext(), "Ciallo", Toast.LENGTH_SHORT).show();
                String fileName = IDUtil.getUUID() + ".png";
                saveBitMapOnDisk(bitmap, fileName);
                break;
            }
            case R.id.share_exp_munu: {
                //仅在内部缓存处理
                try {
                    String fileName = IDUtil.getUUID() + ".png";
                    String tmpp = getActivity().getCacheDir().getAbsolutePath() + "/" + fileName;
                    File tmpf = new File(tmpp);
                    if (tmpf.exists()) {
                        tmpf.delete();
                    }
                    tmpf.createNewFile();
                    FileOutputStream fs=new FileOutputStream(tmpf);
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,fs);//？？
                    fs.flush();
                    fs.close();
                    //这样会挂
                    //todo：老系统可以用Uri.fromFile(tmpf)
                    Uri uri=FileProvider.getUriForFile(getContext(),"io.github.materialapps.texteditor.fileprovider",tmpf);
                    //打开分享窗口
                    Intent shareIntent=new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/**");
                    shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
                    Intent chooser = Intent.createChooser(shareIntent, "将手稿发送到");
                    getActivity().startActivity(chooser);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "哦我们都有不顺利的时候", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            }
            //输入设备选择菜单
            case R.id.input_touch_mode:{
                canvasFlyout.setMode(CanvasFlyout.TOUCH_MODE);
                break;
            }
            case R.id.input_pen_mode:{
                canvasFlyout.setMode(CanvasFlyout.PEN_MODE);
                break;
            }
            case R.id.input_mouse_mode:{
                canvasFlyout.setMode(CanvasFlyout.MOUSE_MODE);
                break;
            }
            case R.id.input_hybrid_mode:{
                canvasFlyout.setMode(CanvasFlyout.HYBRID_MODE);
                break;
            }
            default: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TouchPadViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO: {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(imageUri));
                        bitmap=autoRotate(bitmap);
                        canvasFlyout.addBitMap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "呜呜呜~不开心~", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case SELECT_FILE:{
                if (resultCode == Activity.RESULT_OK && data != null)
                {

                    try {
                        Uri uri = data.getData();
                        ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(uri, "r");
                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                        //bitmap=autoRotate(bitmap);//这里不需要！！！！
                        canvasFlyout.addBitMap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "呜呜呜~不开心~", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    private void saveBitMapOnDisk(Bitmap bitmap, String fileName) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        //todo:安卓9以下要不要照顾？
        cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        //插入数据库后通过系统获取文件句柄
        if (uri != null) {
            try {
                OutputStream os = getContext().getContentResolver().openOutputStream(uri);
                if (os != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.close();
                }
                Toast.makeText(getContext(), "已保存到DCIM文件夹", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "哦我们都有不顺利的时候", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "哦我们都有不顺利的时候", Toast.LENGTH_SHORT).show();
        }

    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.pen_select_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                //效率有点低等优化
                case R.id.size_1px: {
                    canvasFlyout.setStrokeSize(1f);
                    break;
                }
                case R.id.size_5px: {
                    canvasFlyout.setStrokeSize(5f);
                    break;
                }
                case R.id.size_10px: {
                    canvasFlyout.setStrokeSize(10f);
                    break;
                }
                case R.id.size_more: {
                    showSizeSelector();
                    break;
                }
                case R.id.popup_picker: {
                    shoColorPicker();
                    break;
                }
                default: {
                    break;
                }
            }
            return false;
        });

        popupMenu.show();
    }

    private void showSizeSelector() {
        EditText editText = new EditText(getContext());
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("设置笔画半径");
        builder.setView(editText);
        builder.setPositiveButton("确定", (dialog, which) -> {
            Float value = Float.parseFloat(editText.getText().toString());
            canvasFlyout.setStrokeSize(value);
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
            //do nothing
        });
        builder.show();
    }

    private void shoColorPicker() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("选择画笔颜色");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.flyout_simple_color_picker_v2, null);
        builder.setView(dialogView);
        EditText colorHex = dialogView.findViewById(R.id.txb_color_hex_v);
        RecyclerView list = dialogView.findViewById(R.id.color_pane);
        loadColorPane(list);
        builder.setPositiveButton("确定", (dialog, which) -> {
            String custom=colorHex.getText().toString();

            if(!custom.isEmpty())
            {
                if(!custom.startsWith("#")){
                    custom="#"+custom;
                }
                int color=Color.CYAN;
                try{
                    color=Color.parseColor(custom);
                }
                catch (IllegalArgumentException e)
                {
                    Toast.makeText(getContext(), "输入格式不正确", Toast.LENGTH_SHORT).show();
                }
                colorSelected=color;
                canvasFlyout.setPaintColor(color);
            }
            else{
                ColorAdapter adapter = (ColorAdapter) list.getAdapter();
                if (adapter != null) {
                    int mPosition = adapter.getMPosition();
                    if (mPosition >= 0) {
                        ColorAdapter.ColorTag tag = colorList.get(mPosition);
                        colorSelected = tag.getValue();
                        canvasFlyout.setPaintColor(colorSelected);
                    }
                }
            }

        });
        builder.setNegativeButton("取消", ((dialog, which) -> {

        }));
        builder.show();
    }

    private void loadColorPane(RecyclerView view) {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        view.setLayoutManager(layoutManager);
        ColorAdapter adapter = new ColorAdapter(colorList);
        view.setAdapter(adapter);
    }

    private void selectInsertSource(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.insert_source_selector, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.select_from_camera: {
                    String fileName = IDUtil.getUUID();


                    outputFile = new File(getActivity().getExternalCacheDir(), fileName);
                    try {
                        if (outputFile.exists()) {
                            outputFile.delete();
                        }
                        outputFile.createNewFile();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), "喔唷，崩溃了" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    imageUri = FileProvider.getUriForFile(getActivity(), "io.github.materialapps.texteditor.fileprovider", outputFile);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, TAKE_PHOTO);
                    break;
                }
                case R.id.select_from_file: {
                    //打开文件选择器
                    Intent intent = new  Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent .setType("image/*");
                    startActivityForResult(intent,SELECT_FILE);
                    break;
                }
                default: {
                    break;
                }
            }
            return false;
        });
        popupMenu.show();
    }


    private void showDebugWarningDialog()
    {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("Ninja Cat");
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View digView = layoutInflater.inflate(R.layout.warning_dialog, null);
        builder.setView(digView);
        builder.setPositiveButton("确定", (dialog, which) -> {
        });
        builder.show();
    }

    private Bitmap autoRotate(Bitmap bitmap) throws IOException {
        ExifInterface exifInterface=new ExifInterface(outputFile.getPath());
        Bitmap rotated=bitmap;
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch(orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:{
                rotated=rotateBitmap(bitmap,90);
                break;
            }
            case ExifInterface.ORIENTATION_ROTATE_180:{
                rotated=rotateBitmap(bitmap,180);
                break;
            }
            case ExifInterface.ORIENTATION_ROTATE_270:{
                rotated=rotateBitmap(bitmap,270);
                break;
            }
            default:{break;}
        }
        return rotated;
    }

    private Bitmap rotateBitmap(Bitmap bitmap,int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
        bitmap.recycle() ;// 将不再需要的Bitmap对象回收 return rotatedBitmap
        return rotatedBitmap;
    }

}