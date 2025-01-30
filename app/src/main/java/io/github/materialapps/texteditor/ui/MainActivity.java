package io.github.materialapps.texteditor.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;

import io.github.materialapps.texteditor.BaseApplication;
import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.databinding.ActivityMainBinding;
import io.github.materialapps.texteditor.ui.fragment.EditorViewModel;
import lombok.Getter;
import lombok.Setter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    @Getter
    @Setter
    private NavHostFragment navHostFragment;

    @Getter
    @Setter
    private NavController navController;

    private MainViewModel mViewModel;

    private SharedPreferences spf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init mv
        mViewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(getApplication(), this)).get(MainViewModel.class);


        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.my_nav_host_fragment);
        navController = navHostFragment.getNavController();

        ((NavigationBarView)binding.navigationRail).setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.edit_window: {
                    navController.navigate(R.id.editorFragment);
                    break;
                }

                case R.id.list_window: {
                    navController.navigate(R.id.noteListFragment);
                    break;
                }

                case R.id.draw_window: {
                    navController.navigate(R.id.touchPadFragment);
                    break;
                }
                case R.id.tag_window:{
                    navController.navigate(R.id.tagListFragment);
                    break;
                }
                case R.id.settings_window: {
                    navController.navigate(R.id.settingsFragment);
                    break;
                }
                default: {
                    break;
                }
            }
            return true;
        });

        //todo:use navigation view
        mViewModel.getSideBarStatus().observe(this, o -> {
            if (o) {
                //todo:修改状态
            } else {

            }
        });

        //读取起始页
        spf = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getApplication());
        String page = spf.getString("start_page", "note");
        if(page.equals("draw")){
            navController.navigate(R.id.touchPadFragment);
        }

//        View view = binding.navigationRail.getHeaderView();
//        if(view!=null){
//            view.findViewById(R.id.btn_collapse_nav).setOnClickListener(v->{
//                Boolean expand = mViewModel.getSideBarStatus().getValue();
//                mViewModel.getSideBarStatus().setValue(Boolean.FALSE.equals(expand));
//            });
//        }


        Intent intent = getIntent();

        String action = intent.getAction();
        if (intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                Bundle bundle = new Bundle();
                bundle.putInt("mode", BaseApplication.EXTERNAL_EDIT_MODE);
                bundle.putParcelable("filePath", uri);
                navController.navigate(R.id.editorFragment, bundle);
            }
        }

    }
}