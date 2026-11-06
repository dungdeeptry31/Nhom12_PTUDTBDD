package com.educenter.app.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.educenter.app.databinding.ItemAttendanceStudentBinding;
import com.educenter.app.model.Attendance;
import com.educenter.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for taking and editing attendance per student.
 */
public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private final List<Attendance> attendanceList = new ArrayList<>();

    public void setList(List<Attendance> list) {
        attendanceList.clear();
        if (list != null) {
            attendanceList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public List<Attendance> getList() {
        return attendanceList;
    }

    public void markAllPresent() {
        for (Attendance a : attendanceList) {
            a.setStatus(Constants.ATTENDANCE_PRESENT);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceStudentBinding binding = ItemAttendanceStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance item = attendanceList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceStudentBinding binding;

        public ViewHolder(@NonNull ItemAttendanceStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Attendance item) {
            binding.tvAttendanceStudentName.setText(item.getStudentName());
            binding.tvAttendanceStudentCode.setText("Mã HS: " + (item.getStudentCode() != null ? item.getStudentCode() : ""));

            binding.rgAttendanceStatus.setOnCheckedChangeListener(null);

            if (Constants.ATTENDANCE_LATE.equalsIgnoreCase(item.getStatus())) {
                binding.rbLate.setChecked(true);
            } else if (Constants.ATTENDANCE_ABSENT.equalsIgnoreCase(item.getStatus())) {
                binding.rbAbsent.setChecked(true);
            } else {
                binding.rbPresent.setChecked(true);
            }

            binding.rgAttendanceStatus.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == binding.rbPresent.getId()) {
                    item.setStatus(Constants.ATTENDANCE_PRESENT);
                } else if (checkedId == binding.rbLate.getId()) {
                    item.setStatus(Constants.ATTENDANCE_LATE);
                } else if (checkedId == binding.rbAbsent.getId()) {
                    item.setStatus(Constants.ATTENDANCE_ABSENT);
                }
            });

            binding.etAttendanceNote.setText(item.getNote());
            binding.etAttendanceNote.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setNote(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
}
