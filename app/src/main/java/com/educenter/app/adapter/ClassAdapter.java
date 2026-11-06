package com.educenter.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.educenter.app.R;
import com.educenter.app.databinding.ItemClassBinding;
import com.educenter.app.model.ClassRoom;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.CurrencyUtils;

/**
 * RecyclerView Adapter with DiffUtil for Class management.
 */
public class ClassAdapter extends ListAdapter<ClassRoom, ClassAdapter.ClassViewHolder> {

    public interface OnClassClickListener {
        void onClassClick(ClassRoom classRoom);
    }

    private final OnClassClickListener listener;

    private static final DiffUtil.ItemCallback<ClassRoom> DIFF_CALLBACK = new DiffUtil.ItemCallback<ClassRoom>() {
        @Override
        public boolean areItemsTheSame(@NonNull ClassRoom oldItem, @NonNull ClassRoom newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ClassRoom oldItem, @NonNull ClassRoom newItem) {
            return oldItem.getCode().equals(newItem.getCode())
                    && oldItem.getCurrentStudents() == newItem.getCurrentStudents()
                    && oldItem.getMaxStudents() == newItem.getMaxStudents()
                    && oldItem.getStatus().equals(newItem.getStatus())
                    && oldItem.getTuitionFee() == newItem.getTuitionFee();
        }
    };

    public ClassAdapter(OnClassClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemClassBinding binding = ItemClassBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ClassViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ClassViewHolder extends RecyclerView.ViewHolder {
        private final ItemClassBinding binding;

        ClassViewHolder(ItemClassBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassRoom classRoom) {
            binding.tvClassName.setText(classRoom.getName());
            binding.tvClassCodeAndSubject.setText("Mã: " + classRoom.getCode() + "  •  Môn: " + classRoom.getSubjectName());
            binding.tvClassTeacher.setText("GV: " + classRoom.getTeacherName());
            binding.tvClassRoom.setText("Phòng: " + classRoom.getRoomName());

            // Status Badge
            String status = classRoom.getStatus();
            if (Constants.STATUS_CLASS_OPEN.equalsIgnoreCase(status)) {
                binding.tvClassStatus.setText(R.string.status_open);
                binding.tvClassStatus.setBackgroundResource(R.drawable.bg_badge_success);
                binding.tvClassStatus.setTextColor(binding.getRoot().getContext().getColor(R.color.success));
            } else if (Constants.STATUS_CLASS_UPCOMING.equalsIgnoreCase(status)) {
                binding.tvClassStatus.setText(R.string.status_upcoming);
                binding.tvClassStatus.setBackgroundResource(R.drawable.bg_badge_warning);
                binding.tvClassStatus.setTextColor(binding.getRoot().getContext().getColor(R.color.warning));
            } else {
                binding.tvClassStatus.setText(R.string.status_finished);
                binding.tvClassStatus.setBackgroundResource(R.drawable.bg_badge_error);
                binding.tvClassStatus.setTextColor(binding.getRoot().getContext().getColor(R.color.error));
            }

            // Sĩ số & Progress Bar
            int cur = classRoom.getCurrentStudents();
            int max = classRoom.getMaxStudents();
            binding.tvCapacityLabel.setText("Sĩ số: " + cur + " / " + max + (classRoom.isFull() ? " (ĐÃ ĐẦY)" : " học sinh"));
            int percent = max > 0 ? (int) ((cur * 100.0f) / max) : 0;
            binding.progressCapacity.setProgress(Math.min(percent, 100));

            if (classRoom.isFull()) {
                binding.progressCapacity.setProgressTintList(binding.getRoot().getContext().getColorStateList(R.color.error));
            } else {
                binding.progressCapacity.setProgressTintList(binding.getRoot().getContext().getColorStateList(R.color.primary));
            }

            // Học phí
            binding.tvTuitionFee.setText(CurrencyUtils.formatVnd(classRoom.getTuitionFee()) + "/tháng");

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClassClick(classRoom);
                }
            });
        }
    }
}
