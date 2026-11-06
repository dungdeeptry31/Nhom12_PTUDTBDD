package com.educenter.app.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.educenter.app.R;
import com.educenter.app.data.local.SessionManager;
import com.educenter.app.databinding.ActivityMainBinding;
import com.educenter.app.view.fragment.ClassFragment;
import com.educenter.app.view.fragment.DashboardFragment;
import com.educenter.app.view.fragment.ProfileFragment;
import com.educenter.app.view.fragment.StudentFragment;
import com.educenter.app.view.fragment.TuitionFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Main Activity container with BottomNavigationView, Role permission enforcement,
 * and top toolbar user badge.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);

        // Check if logged in; if not, redirect to LoginActivity
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setupToolbar();
        setupBottomNavigation();

        // Default to Dashboard tab
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment.newInstance());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.topToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        String role = sessionManager.getUserRole();
        binding.tvUserRoleBadge.setText(role.isEmpty() ? "Hệ thống" : role);

        if (sessionManager.isAdmin()) {
            binding.tvUserRoleBadge.setBackgroundResource(R.drawable.bg_badge_primary);
            binding.tvUserRoleBadge.setTextColor(ContextCompat.getColor(this, R.color.primary));
        } else if (sessionManager.isTeacher()) {
            binding.tvUserRoleBadge.setBackgroundResource(R.drawable.bg_badge_warning);
            binding.tvUserRoleBadge.setTextColor(ContextCompat.getColor(this, R.color.warning));
        } else {
            binding.tvUserRoleBadge.setBackgroundResource(R.drawable.bg_badge_success);
            binding.tvUserRoleBadge.setTextColor(ContextCompat.getColor(this, R.color.success));
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                loadFragment(DashboardFragment.newInstance());
                return true;
            } else if (itemId == R.id.nav_students) {
                loadFragment(StudentFragment.newInstance());
                return true;
            } else if (itemId == R.id.nav_classes) {
                loadFragment(ClassFragment.newInstance());
                return true;
            } else if (itemId == R.id.nav_tuition) {
                // TC22: Teacher cannot access tuition
                if (sessionManager.isTeacher()) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Quyền truy cập bị từ chối")
                            .setMessage("Tài khoản Giáo viên không có quyền truy cập vào phân hệ Học phí và Tài chính!")
                            .setPositiveButton("Đồng ý", null)
                            .show();
                    return false;
                }
                loadFragment(TuitionFragment.newInstance());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(ProfileFragment.newInstance());
                return true;
            }
            return false;
        });
    }

    public void navigateToTab(int menuId) {
        binding.bottomNavigation.setSelectedItemId(menuId);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commitAllowingStateLoss();
    }
}
