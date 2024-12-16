package io.github.materialapps.texteditor.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import io.github.materialapps.texteditor.BaseApplication;
import io.github.materialapps.texteditor.R;
import lombok.Getter;
import lombok.Setter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Getter
    @Setter
    private NavHostFragment navHostFragment;

    @Getter
    @Setter
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //todo:处理关联打开的情况

        navHostFragment= (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.my_nav_host_fragment);
        navController=navHostFragment.getNavController();

        Intent intent=getIntent();

        String action = intent.getAction();
        if(intent.ACTION_VIEW.equals(action)){
            Uri uri= intent.getData();
            if(uri!=null){
                Bundle bundle=new Bundle();
                bundle.putInt("mode", BaseApplication.EXTERNAL_EDIT_MODE);
                bundle.putParcelable("filePath",uri);
                navController.navigate(R.id.editorFragment,bundle);
            }
        }

    }
}