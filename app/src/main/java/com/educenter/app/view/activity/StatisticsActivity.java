package com.educenter.app.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.educenter.app.R;
import com.educenter.app.databinding.ActivityStatisticsBinding;
import com.educenter.app.repository.StatisticsRepository;
import com.educenter.app.utils.CurrencyUtils;
import com.educenter.app.viewmodel.StatisticsViewModel;

import java.util.List;
import java.util.Map;

/**
 * Statistics and Visual Analytics Activity displaying revenue trends,
 * tuition status composition, and student demographics.
 */
public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private StatisticsViewModel statisticsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        setupToolbar();
        observeViewModel();

        statisticsViewModel.loadAllStatistics();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarStats);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbarStats.setNavigationOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        // 6 Months Revenue breakdown
        statisticsViewModel.getRevenue6MonthsLiveData().observe(this, this::renderRevenueBars);

        // Tuition distribution
        statisticsViewModel.getTuitionDistributionLiveData().observe(this, dist -> {
            if (dist != null) {
                binding.pieChartView.setData(dist.paidCount, dist.partialCount, dist.unpaidCount);
                String text = "• Đã thanh toán đủ: " + dist.paidCount + " hóa đơn\n"
                        + "• Đang nộp một phần: " + dist.partialCount + " hóa đơn\n"
                        + "• Chưa thanh toán: " + dist.unpaidCount + " hóa đơn\n\n"
                        + "💰 Tổng tiền đã thu thực tế: " + CurrencyUtils.formatVnd(dist.paidTotal) + "\n"
                        + "⚠️ Tổng công nợ còn phải thu: " + CurrencyUtils.formatVnd(dist.debtTotal);
                binding.tvTuitionBreakdown.setText(text);
            }
        });

        // Attendance summary
        statisticsViewModel.getAttendanceSummaryLiveData().observe(this, att -> {
            if (att != null && att.totalCount > 0) {
                String text = String.format(java.util.Locale.US,
                        "• Tỷ lệ có mặt: %.1f%% (%d lượt)\n• Tỷ lệ vắng mặt: %.1f%% (%d lượt)\n• Tỷ lệ đi muộn: %.1f%% (%d lượt)",
                        att.getPresentRate(), att.presentCount,
                        att.getAbsentRate(), att.absentCount,
                        att.getLateRate(), att.lateCount);
                binding.tvAttendanceBreakdown.setText(text);
            }
        });

        // Grade distribution
        statisticsViewModel.getGradeDistributionLiveData().observe(this, map -> {
            if (map != null) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    sb.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" học sinh\n");
                }
                binding.tvGradeBreakdown.setText(sb.toString().trim());
            }
        });
    }

    private void renderRevenueBars(List<StatisticsRepository.MonthlyRevenue> list) {
        binding.barChartView.setData(list);
        binding.layoutRevenueBars.removeAllViews();
        if (list == null || list.isEmpty()) return;

        double maxRev = 1.0;
        for (StatisticsRepository.MonthlyRevenue item : list) {
            if (item.revenue > maxRev) maxRev = item.revenue;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (StatisticsRepository.MonthlyRevenue item : list) {
            View row = inflater.inflate(android.R.layout.simple_list_item_2, binding.layoutRevenueBars, false);
            TextView text1 = row.findViewById(android.R.id.text1);
            TextView text2 = row.findViewById(android.R.id.text2);

            text1.setText("Tháng " + item.fullMonthYear + ": " + CurrencyUtils.formatVnd(item.revenue));
            text1.setTextColor(ContextCompat.getColor(this, R.color.text_main));
            text1.setTextSize(13);

            text2.setText(item.revenue > 0 ? "Tiến độ thu: " + (int)((item.revenue / maxRev) * 100) + "%" : "Chưa có phát sinh thu");
            text2.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            text2.setTextSize(11);

            binding.layoutRevenueBars.addView(row);
        }
    }
}
