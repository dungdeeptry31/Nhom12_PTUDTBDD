package com.educenter.app.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.educenter.app.R;
import com.educenter.app.adapter.TuitionAdapter;
import com.educenter.app.databinding.FragmentTuitionBinding;
import com.educenter.app.model.TuitionInvoice;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.CurrencyUtils;
import com.educenter.app.utils.DateUtils;
import com.educenter.app.view.dialog.PaymentBottomSheet;
import com.educenter.app.view.dialog.TuitionBottomSheet;
import com.educenter.app.viewmodel.TuitionViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Fragment displaying Tuition Invoices list, Payment status filters, and Debt details.
 */
public class TuitionFragment extends Fragment {

    private FragmentTuitionBinding binding;
    private TuitionViewModel tuitionViewModel;
    private TuitionAdapter adapter;
    private String selectedStatus = "Tất cả";

    public static TuitionFragment newInstance() {
        return new TuitionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTuitionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tuitionViewModel = new ViewModelProvider(this).get(TuitionViewModel.class);

        setupRecyclerView();
        setupSearchAndFilter();
        observeViewModel();

        binding.fabAddInvoice.setOnClickListener(v -> {
            TuitionBottomSheet.newInstance().show(getChildFragmentManager(), "TuitionBottomSheet");
        });

        tuitionViewModel.loadInvoices("", null, null, null);
    }

    private void setupRecyclerView() {
        adapter = new TuitionAdapter(this::onInvoiceSelected);
        binding.rvTuition.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTuition.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        binding.etSearchTuition.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String kw = s.toString();
                binding.btnClearSearch.setVisibility(kw.isEmpty() ? View.GONE : View.VISIBLE);
                tuitionViewModel.loadInvoices(kw, selectedStatus, null, null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> binding.etSearchTuition.setText(""));

        binding.chipGroupTuition.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipPaid)) {
                selectedStatus = Constants.TUITION_PAID;
            } else if (checkedIds.contains(R.id.chipPartial)) {
                selectedStatus = Constants.TUITION_PARTIAL;
            } else if (checkedIds.contains(R.id.chipUnpaid)) {
                selectedStatus = Constants.TUITION_UNPAID;
            } else {
                selectedStatus = "Tất cả";
            }
            String kw = binding.etSearchTuition.getText() != null ? binding.etSearchTuition.getText().toString() : "";
            tuitionViewModel.loadInvoices(kw, selectedStatus, null, null);
        });
    }

    private void observeViewModel() {
        tuitionViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading != null && isLoading ? View.VISIBLE : View.GONE);
        });

        tuitionViewModel.getInvoicesLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.rvTuition.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvTuition.setVisibility(View.VISIBLE);
                adapter.submitList(list);
            }
        });

        tuitionViewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Thông báo")
                        .setMessage(error)
                        .setPositiveButton("Đồng ý", null)
                        .show();
            }
        });

        tuitionViewModel.getActionSuccessLiveData().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onInvoiceSelected(TuitionInvoice inv) {
        if (inv.getRemainingAmount() > 0) {
            String[] options = {"Thu tiền học phí", "Xem thông tin chi tiết"};
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(inv.getInvoiceCode() + " - Nợ " + CurrencyUtils.formatVnd(inv.getRemainingAmount()))
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            PaymentBottomSheet.newInstance(inv).show(getChildFragmentManager(), "PaymentBottomSheet");
                        } else {
                            showInvoiceDetails(inv);
                        }
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        } else {
            showInvoiceDetails(inv);
        }
    }

    private void showInvoiceDetails(TuitionInvoice inv) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chi tiết hóa đơn: " + inv.getInvoiceCode())
                .setMessage("• Học sinh: " + inv.getStudentName() + " (" + inv.getStudentCode() + ")"
                        + "\n• Lớp học: " + inv.getClassName()
                        + "\n• Tháng thu: " + inv.getMonthYear()
                        + "\n• Hạn đóng: " + DateUtils.toDisplayDate(inv.getDueDate())
                        + "\n• Số tiền phải nộp: " + CurrencyUtils.formatVnd(inv.getAmount())
                        + "\n• Đã nộp: " + CurrencyUtils.formatVnd(inv.getPaidAmount())
                        + "\n• Còn nợ: " + CurrencyUtils.formatVnd(inv.getRemainingAmount())
                        + "\n• Trạng thái: " + (Constants.TUITION_PAID.equals(inv.getStatus()) ? "Đã nộp đủ" :
                                (Constants.TUITION_PARTIAL.equals(inv.getStatus()) ? "Đóng một phần" : "Chưa nộp tiền"))
                        + (inv.getNote() != null && !inv.getNote().isEmpty() ? "\n• Ghi chú: " + inv.getNote() : ""))
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
