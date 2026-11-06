package com.educenter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.educenter.app.repository.DataCallback;
import com.educenter.app.repository.StatisticsRepository;

import java.util.List;
import java.util.Map;

/**
 * ViewModel for Comprehensive Analytics and Visual Reporting.
 */
public class StatisticsViewModel extends AndroidViewModel {

    private final StatisticsRepository statisticsRepository;

    private final MutableLiveData<StatisticsRepository.DashboardOverview> overviewLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<StatisticsRepository.MonthlyRevenue>> revenue6MonthsLiveData = new MutableLiveData<>();
    private final MutableLiveData<StatisticsRepository.TuitionDistribution> tuitionDistributionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> gradeDistributionLiveData = new MutableLiveData<>();
    private final MutableLiveData<StatisticsRepository.AttendanceSummary> attendanceSummaryLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        this.statisticsRepository = new StatisticsRepository(application);
    }

    public LiveData<StatisticsRepository.DashboardOverview> getOverviewLiveData() {
        return overviewLiveData;
    }

    public LiveData<List<StatisticsRepository.MonthlyRevenue>> getRevenue6MonthsLiveData() {
        return revenue6MonthsLiveData;
    }

    public LiveData<StatisticsRepository.TuitionDistribution> getTuitionDistributionLiveData() {
        return tuitionDistributionLiveData;
    }

    public LiveData<Map<String, Integer>> getGradeDistributionLiveData() {
        return gradeDistributionLiveData;
    }

    public LiveData<StatisticsRepository.AttendanceSummary> getAttendanceSummaryLiveData() {
        return attendanceSummaryLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void loadAllStatistics() {
        isLoadingLiveData.setValue(true);

        statisticsRepository.getDashboardOverview(new DataCallback<StatisticsRepository.DashboardOverview>() {
            @Override
            public void onSuccess(StatisticsRepository.DashboardOverview result) {
                isLoadingLiveData.setValue(false);
                overviewLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });

        statisticsRepository.getRevenueLast6Months(new DataCallback<List<StatisticsRepository.MonthlyRevenue>>() {
            @Override
            public void onSuccess(List<StatisticsRepository.MonthlyRevenue> result) {
                revenue6MonthsLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                // Ignore
            }
        });

        statisticsRepository.getTuitionDistribution(new DataCallback<StatisticsRepository.TuitionDistribution>() {
            @Override
            public void onSuccess(StatisticsRepository.TuitionDistribution result) {
                tuitionDistributionLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                // Ignore
            }
        });

        statisticsRepository.getStudentsByGrade(new DataCallback<Map<String, Integer>>() {
            @Override
            public void onSuccess(Map<String, Integer> result) {
                gradeDistributionLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                // Ignore
            }
        });

        statisticsRepository.getAttendanceSummary(new DataCallback<StatisticsRepository.AttendanceSummary>() {
            @Override
            public void onSuccess(StatisticsRepository.AttendanceSummary result) {
                attendanceSummaryLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                // Ignore
            }
        });
    }
}
