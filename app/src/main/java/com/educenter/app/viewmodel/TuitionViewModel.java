package com.educenter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.educenter.app.model.TuitionInvoice;
import com.educenter.app.model.TuitionPayment;
import com.educenter.app.repository.DataCallback;
import com.educenter.app.repository.TuitionRepository;

import java.util.List;

/**
 * ViewModel for Tuition Invoices and Payment processing.
 */
public class TuitionViewModel extends AndroidViewModel {

    private final TuitionRepository tuitionRepository;

    private final MutableLiveData<List<TuitionInvoice>> invoicesLiveData = new MutableLiveData<>();
    private final MutableLiveData<TuitionInvoice> selectedInvoiceLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TuitionPayment>> paymentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> actionSuccessLiveData = new MutableLiveData<>();

    public TuitionViewModel(@NonNull Application application) {
        super(application);
        this.tuitionRepository = new TuitionRepository(application);
    }

    public LiveData<List<TuitionInvoice>> getInvoicesLiveData() {
        return invoicesLiveData;
    }

    public LiveData<TuitionInvoice> getSelectedInvoiceLiveData() {
        return selectedInvoiceLiveData;
    }

    public LiveData<List<TuitionPayment>> getPaymentsLiveData() {
        return paymentsLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<String> getActionSuccessLiveData() {
        return actionSuccessLiveData;
    }

    public void loadInvoices(String keyword, String statusFilter, String monthFilter, Long classFilter) {
        isLoadingLiveData.setValue(true);
        tuitionRepository.searchInvoices(keyword, statusFilter, monthFilter, classFilter, new DataCallback<List<TuitionInvoice>>() {
            @Override
            public void onSuccess(List<TuitionInvoice> result) {
                isLoadingLiveData.setValue(false);
                invoicesLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void loadInvoiceDetail(long invoiceId) {
        isLoadingLiveData.setValue(true);
        tuitionRepository.getInvoiceById(invoiceId, new DataCallback<TuitionInvoice>() {
            @Override
            public void onSuccess(TuitionInvoice invoice) {
                isLoadingLiveData.setValue(false);
                selectedInvoiceLiveData.setValue(invoice);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });

        tuitionRepository.getPaymentsForInvoice(invoiceId, new DataCallback<List<TuitionPayment>>() {
            @Override
            public void onSuccess(List<TuitionPayment> payments) {
                paymentsLiveData.setValue(payments);
            }

            @Override
            public void onError(String message) {
                // Ignore
            }
        });
    }

    public void createInvoice(TuitionInvoice invoice) {
        isLoadingLiveData.setValue(true);
        tuitionRepository.createInvoice(invoice, new DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Tạo hóa đơn học phí thành công!");
                loadInvoices("", null, null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void recordPayment(TuitionPayment payment) {
        isLoadingLiveData.setValue(true);
        tuitionRepository.recordPayment(payment, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Ghi nhận thanh toán thành công!");
                loadInvoiceDetail(payment.getInvoiceId());
                loadInvoices("", null, null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }
}
