package com.educenter.app.view.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.educenter.app.data.local.SessionManager;
import com.educenter.app.databinding.DialogPaymentSheetBinding;
import com.educenter.app.model.TuitionInvoice;
import com.educenter.app.model.TuitionPayment;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.CurrencyUtils;
import com.educenter.app.utils.DateUtils;
import com.educenter.app.viewmodel.TuitionViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * BottomSheet Dialog for collecting tuition installments or full settlements.
 * Enforces TC19, TC20 (overflow check), and TC21 (receipt confirmation).
 */
public class PaymentBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_INVOICE = "arg_invoice";

    private DialogPaymentSheetBinding binding;
    private TuitionViewModel tuitionViewModel;
    private SessionManager sessionManager;
    private TuitionInvoice invoice;

    public static PaymentBottomSheet newInstance(TuitionInvoice invoice) {
        PaymentBottomSheet sheet = new PaymentBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_INVOICE, invoice);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            invoice = (TuitionInvoice) getArguments().getSerializable(ARG_INVOICE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogPaymentSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tuitionViewModel = new ViewModelProvider(requireActivity()).get(TuitionViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());

        populateInvoiceData();

        binding.btnPaymentClose.setOnClickListener(v -> dismiss());
        binding.tvFillFullDebt.setOnClickListener(v -> {
            if (invoice != null) {
                binding.etPaymentAmount.setText(String.valueOf((long) invoice.getRemainingAmount()));
                binding.tilPaymentAmount.setError(null);
            }
        });
        binding.btnConfirmPayment.setOnClickListener(v -> processPayment());
    }

    private void populateInvoiceData() {
        if (invoice == null) return;

        binding.tvPaymentInvoiceCode.setText(invoice.getInvoiceCode());
        binding.tvPaymentMonthYear.setText("Tháng: " + invoice.getMonthYear());
        binding.tvPaymentStudentInfo.setText("Học sinh: " + invoice.getStudentName() + " (" + invoice.getStudentCode() + ")");
        binding.tvPaymentClassInfo.setText("Lớp: " + invoice.getClassName());

        binding.tvPaymentTotalAmount.setText(CurrencyUtils.formatVnd(invoice.getAmount()));
        binding.tvPaymentPaidAmount.setText(CurrencyUtils.formatVnd(invoice.getPaidAmount()));
        binding.tvPaymentRemainingDebt.setText(CurrencyUtils.formatVnd(invoice.getRemainingAmount()));

        // Default suggest the remaining debt
        if (invoice.getRemainingAmount() > 0) {
            binding.etPaymentAmount.setText(String.valueOf((long) invoice.getRemainingAmount()));
        } else {
            binding.etPaymentAmount.setText("0");
            binding.btnConfirmPayment.setEnabled(false);
            binding.btnConfirmPayment.setText("Hóa đơn đã thanh toán đủ");
        }
    }

    private void processPayment() {
        if (invoice == null) {
            dismiss();
            return;
        }

        String amountStr = binding.etPaymentAmount.getText() != null ? binding.etPaymentAmount.getText().toString().trim() : "";
        String note = binding.etPaymentNote.getText() != null ? binding.etPaymentNote.getText().toString().trim() : "";
        String method = binding.rbTransfer.isChecked() ? Constants.PAYMENT_METHOD_TRANSFER : Constants.PAYMENT_METHOD_CASH;

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            binding.tilPaymentAmount.setError("Số tiền không hợp lệ!");
            return;
        }

        if (amount <= 0) {
            binding.tilPaymentAmount.setError("Số tiền thanh toán phải lớn hơn 0!");
            return;
        }

        // TC20: Overflow check
        if (amount > invoice.getRemainingAmount() + 0.01) {
            binding.tilPaymentAmount.setError("Số tiền thanh toán không được lớn hơn số tiền còn nợ ("
                    + CurrencyUtils.formatVnd(invoice.getRemainingAmount()) + ")!");
            return;
        }

        binding.tilPaymentAmount.setError(null);

        long receiverId = sessionManager.getUserId() > 0 ? sessionManager.getUserId() : 1;
        String todayDb = DateUtils.getTodayDb();

        TuitionPayment payment = new TuitionPayment(0, invoice.getId(), amount, todayDb, method, note, receiverId);
        tuitionViewModel.recordPayment(payment);

        // Show receipt confirmation dialog (TC21)
        showReceiptDialog(invoice, amount, method, sessionManager.getFullName());
        dismiss();
    }

    private void showReceiptDialog(TuitionInvoice inv, double amountPaid, String method, String receiver) {
        String receiptInfo = "🧾 BIÊN LAI THU HỌC PHÍ\n"
                + "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "• Mã hóa đơn: " + inv.getInvoiceCode() + "\n"
                + "• Học sinh: " + inv.getStudentName() + " (" + inv.getStudentCode() + ")\n"
                + "• Lớp học: " + inv.getClassName() + "\n"
                + "• Số tiền thu: " + CurrencyUtils.formatVnd(amountPaid) + "\n"
                + "• Hình thức: " + (Constants.PAYMENT_METHOD_TRANSFER.equals(method) ? "Chuyển khoản" : "Tiền mặt") + "\n"
                + "• Người thu: " + receiver + "\n"
                + "• Ngày thu: " + DateUtils.toDisplayDate(DateUtils.getTodayDb()) + "\n"
                + "━━━━━━━━━━━━━━━━━━━━━━\n"
                + "Ghi nhận thanh toán thành công!";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Biên lai thu tiền thành công")
                .setMessage(receiptInfo)
                .setPositiveButton("In / Lưu biên lai", null)
                .setNegativeButton("Đóng", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
