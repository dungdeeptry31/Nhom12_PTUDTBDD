package com.educenter.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.educenter.app.R;
import com.educenter.app.databinding.ItemStudentBinding;
import com.educenter.app.model.Student;

/**
 * Modern RecyclerView Adapter with DiffUtil for Students list.
 */
public class StudentAdapter extends ListAdapter<Student, StudentAdapter.StudentViewHolder> {

    public interface OnStudentClickListener {
        void onStudentClick(Student student);
    }

    private final OnStudentClickListener listener;

    private static final DiffUtil.ItemCallback<Student> DIFF_CALLBACK = new DiffUtil.ItemCallback<Student>() {
        @Override
        public boolean areItemsTheSame(@NonNull Student oldItem, @NonNull Student newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Student oldItem, @NonNull Student newItem) {
            return oldItem.getStudentCode().equals(newItem.getStudentCode())
                    && oldItem.getFullName().equals(newItem.getFullName())
                    && oldItem.getActive() == newItem.getActive()
                    && oldItem.getEnrolledClassesCount() == newItem.getEnrolledClassesCount();
        }
    };

    public StudentAdapter(OnStudentClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentBinding binding = ItemStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding binding;

        StudentViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Student student) {
            binding.tvStudentName.setText(student.getFullName());
            binding.tvStudentCode.setText("Mã: " + student.getStudentCode());
            binding.tvStudentGrade.setText("•  Khối " + student.getGradeLevel());
            binding.tvStudentPhone.setText("SĐT: " + student.getPhone() + " • " + student.getSchool());

            // Avatar initial
            String name = student.getFullName().trim();
            if (!name.isEmpty()) {
                char initial = name.charAt(name.lastIndexOf(' ') + 1);
                binding.tvAvatarInitial.setText(String.valueOf(initial).toUpperCase());
            }

            // Status Badge
            if (student.isActive()) {
                binding.tvStudentStatus.setText(R.string.status_active);
                binding.tvStudentStatus.setBackgroundResource(R.drawable.bg_badge_success);
                binding.tvStudentStatus.setTextColor(binding.getRoot().getContext().getColor(R.color.success));
            } else {
                binding.tvStudentStatus.setText(R.string.status_inactive);
                binding.tvStudentStatus.setBackgroundResource(R.drawable.bg_badge_error);
                binding.tvStudentStatus.setTextColor(binding.getRoot().getContext().getColor(R.color.error));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentClick(student);
                }
            });
        }
    }
}
