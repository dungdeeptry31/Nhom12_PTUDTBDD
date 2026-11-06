package com.educenter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.educenter.app.repository.DataCallback;
import com.educenter.app.repository.StatisticsRepository;

import java.util.List;

/**
 * ViewModel for Dashboard overview, metrics cards, and recent activities.
 */
public class DashboardViewModel extends AndroidViewModel {

    private final StatisticsRepository statisticsRepository;

    private final MutableLiveData<StatisticsRepository.DashboardOverview> overviewLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<StatisticsRepository.MonthlyRevenue>> revenueListLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<StatisticsRepository.RecentActivity>> activitiesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.statisticsRepository = new StatisticsRepository(application);
    }

    public LiveData<StatisticsRepository.DashboardOverview> getOverviewLiveData() {
        return overviewLiveData;
    }

    public LiveData<List<StatisticsRepository.MonthlyRevenue>> getRevenueListLiveData() {
        return revenueListLiveData;
    }

    public LiveData<List<StatisticsRepository.RecentActivity>> getActivitiesLiveData() {
        return activitiesLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void loadDashboardData() {
        isLoadingLiveData.setValue(true);

        statisticsRepository.getDashboardOverview(new DataCallback<StatisticsRepository.DashboardOverview>() {
            @Override
            public void onSuccess(StatisticsRepository.DashboardOverview result) {
                overviewLiveData.setValue(result);
                isLoadingLiveData.setValue(false);
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
                revenueListLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                // Ignore chart non-fatal error
            }
        });

        statisticsRepository.getRecentActivities(new DataCallback<List<StatisticsRepository.RecentActivity>>() {
            @Override
            public void onSuccess(List<StatisticsRepository.RecentActivity> result) {
                activitiesLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                // Ignore
            }
        });
    }
}
