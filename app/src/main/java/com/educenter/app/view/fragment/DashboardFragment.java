package com.educenter.app.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.educenter.app.R;
import com.educenter.app.data.local.SessionManager;
import com.educenter.app.databinding.FragmentDashboardBinding;
import com.educenter.app.repository.StatisticsRepository;
import com.educenter.app.utils.CurrencyUtils;
import com.educenter.app.utils.DateUtils;
import com.educenter.app.view.activity.MainActivity;
import com.educenter.app.view.activity.StatisticsActivity;
import com.educenter.app.viewmodel.DashboardViewModel;

import java.util.Calendar;
import java.util.List;

/**
 * Dashboard Overview Fragment.
 * Displays key center metrics, quick action triggers, and recent activities.
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    private SessionManager sessionManager;

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupHeader();
        setupQuickActions();
        observeViewModel();

        binding.swipeRefresh.setOnRefreshListener(() -> dashboardViewModel.loadDashboardData());

        dashboardViewModel.loadDashboardData();
    }

    private void setupHeader() {
        // Time of day greeting
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = "Chào buổi sáng,";
        } else if (hour < 18) {
            greeting = "Chào buổi chiều,";
        } else {
            greeting = "Chào buổi tối,";
        }
        binding.tvGreeting.setText(greeting);

        // User info from Session
        binding.tvUserName.setText(sessionManager.getFullName());
        binding.tvHeaderRole.setText(sessionManager.getUserRole());
        binding.tvDateDisplay.setText("Hôm nay: " + DateUtils.toDisplayDate(DateUtils.getTodayDb()));
    }

    private void setupQuickActions() {
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            binding.actionStudents.setOnClickListener(v -> main.navigateToTab(R.id.nav_students));
            binding.actionClasses.setOnClickListener(v -> main.navigateToTab(R.id.nav_classes));
            binding.actionAttendance.setOnClickListener(v -> main.navigateToTab(R.id.nav_classes)); // Classes leads to attendance
        }

        binding.actionStats.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), StatisticsActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        dashboardViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading != null && isLoading);
        });

        // 4 Key metrics
        dashboardViewModel.getOverviewLiveData().observe(getViewLifecycleOwner(), overview -> {
            if (overview != null) {
                binding.tvStatStudents.setText(String.valueOf(overview.totalStudents));
                binding.tvStatClasses.setText(String.valueOf(overview.totalOpenClasses));
                binding.tvStatTeachers.setText(String.valueOf(overview.totalActiveTeachers));
                binding.tvStatRevenue.setText(CurrencyUtils.formatVnd(overview.currentMonthRevenue));
            }
        });

        // 6-Month Revenue chart
        dashboardViewModel.getRevenueListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                binding.dashboardBarChart.setData(list);
            }
        });

        // Recent activities
        dashboardViewModel.getActivitiesLiveData().observe(getViewLifecycleOwner(), this::renderRecentActivities);
    }

    private void renderRecentActivities(List<StatisticsRepository.RecentActivity> activities) {
        binding.layoutRecentActivities.removeAllViews();

        if (activities == null || activities.isEmpty()) {
            TextView emptyTv = new TextView(requireContext());
            emptyTv.setText("Chưa có hoạt động gần đây.");
            emptyTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            emptyTv.setPadding(16, 16, 16, 16);
            binding.layoutRecentActivities.addView(emptyTv);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (StatisticsRepository.RecentActivity act : activities) {
            View itemView = inflater.inflate(R.layout.item_recent_activity, binding.layoutRecentActivities, false);

            ImageView ivIcon = itemView.findViewById(R.id.ivActivityIcon);
            TextView tvTitle = itemView.findViewById(R.id.tvActivityTitle);
            TextView tvDesc = itemView.findViewById(R.id.tvActivityDesc);
            TextView tvTime = itemView.findViewById(R.id.tvActivityTime);

            tvTitle.setText(act.title);
            tvDesc.setText(act.description);
            tvTime.setText(act.time);

            if ("PAYMENT".equals(act.iconType)) {
                ivIcon.setImageResource(R.drawable.ic_tuition);
            } else if ("STUDENT".equals(act.iconType)) {
                ivIcon.setImageResource(R.drawable.ic_students);
            } else {
                ivIcon.setImageResource(R.drawable.ic_attendance);
            }

            binding.layoutRecentActivities.addView(itemView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
