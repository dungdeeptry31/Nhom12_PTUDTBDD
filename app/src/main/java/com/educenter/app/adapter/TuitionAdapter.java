package com.educenter.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.educenter.app.R;
import com.educenter.app.databinding.ItemTuitionBinding;
import com.educenter.app.model.TuitionInvoice;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.CurrencyUtils;

/**
 * RecyclerView Adapter with DiffUtil for Tuition Invoices and Debt Tracking.
 */
public class TuitionAdapter extends ListAdapter<TuitionInvoice, TuitionAdapter.TuitionViewHolder> {

    public interface OnInvoiceClickListener {
        void onInvoiceClick(TuitionInvoice invoice);
    }

    private final OnInvoiceClickListener listener;

    private static final DiffUtil.ItemCallback<TuitionInvoice> DIFF_CALLBACK = new DiffUtil.ItemCallback<TuitionInvoice>() {
        @Override
        public boolean areItemsTheSame(@NonNull TuitionInvoice oldItem, @NonNull TuitionInvoice newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TuitionInvoice oldItem, @NonNull TuitionInvoice newItem) {
            return oldItem.getInvoiceCode().equals(newItem.getInvoiceCode())
                    && oldItem.getAmount() == newItem.getAmount()
                    && oldItem.getPaidAmount() == newItem.getPaidAmount()
                    && oldItem.getMonthYear().equals(newItem.getMonthYear());
        }
    };

    public TuitionAdapter(OnInvoiceClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TuitionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTuitionBinding binding = ItemTuitionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TuitionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TuitionViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TuitionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTuitionBinding binding;

        TuitionViewHolder(ItemTuitionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TuitionInvoice invoice) {
            binding.tvInvoiceCode.setText(invoice.getInvoiceCode());
            binding.tvInvoiceStudent.setText("Học sinh: " + invoice.getStudentName() + " (" + invoice.getStudentCode() + ")");
            binding.tvInvoiceClass.setText("Lớp: " + invoice.getClassName() + "  •  Tháng " + invoice.getMonthYear());

            binding.tvTotalAmount.setText(CurrencyUtils.formatVnd(invoice.getAmount()));
            binding.tvPaidAmount.setText(CurrencyUtils.formatVnd(invoice.getPaidAmount()));
            binding.tvDebtAmount.setText(CurrencyUtils.formatVnd(invoice.getRemainingAmount()));

            // Status Badge
            String status = invoice.getStatus();
            if (Constants.TUITION_PAID.equalsIgnoreCase(status)) {
                binding.tvInvoiceStatus.setText(R.string.status_paid);
                binding.tvInvoiceStatus.setBackgroundResource(R.drawable.bg_badge_success);
                binding.tvInvoiceStatus.setTextColor(binding.getRoot().getContext().getColor(R.color.success));
            } else if (Constants.TUITION_PARTIAL.equalsIgnoreCase(status)) {
                binding.tvInvoiceStatus.setText(R.string.status_partially_paid);
                binding.tvInvoiceStatus.setBackgroundResource(R.drawable.bg_badge_warning);
                binding.tvInvoiceStatus.setTextColor(binding.getRoot().getContext().getColor(R.color.warning));
            } else {
                binding.tvInvoiceStatus.setText(R.string.status_unpaid);
                binding.tvInvoiceStatus.setBackgroundResource(R.drawable.bg_badge_error);
                binding.tvInvoiceStatus.setTextColor(binding.getRoot().getContext().getColor(R.color.error));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInvoiceClick(invoice);
                }
            });
        }
    }
}
