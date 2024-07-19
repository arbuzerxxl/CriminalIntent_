package com.example.criminalintent_

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date


private const val ARG_TIME = "time"
private const val ARG_REQUEST_TIME = "requestTimeKey"

class TimePickerFragment: DialogFragment() {

    companion object {
        fun newInstance(date: Date, requestTime: String): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME, date)
                putString(ARG_REQUEST_TIME, requestTime)
            }
            return TimePickerFragment().apply { arguments = args }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_TIME, Date::class.java)
        } else {
            arguments?.getSerializable(ARG_TIME) as Date
        }

        val calendar = Calendar.getInstance()
        calendar.time = date!!

        val timeListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val resultTime = calendar.time
            val resultRequestCode = requireArguments().getString(ARG_REQUEST_TIME, "")
            val result = Bundle().apply {
                putSerializable(resultRequestCode, resultTime)
            }

            this@TimePickerFragment.parentFragmentManager.setFragmentResult(resultRequestCode, result)
        }

        val initialHour = calendar[Calendar.HOUR_OF_DAY]
        val initialMinute = calendar[Calendar.MINUTE]
        return TimePickerDialog(requireContext(), timeListener, initialHour, initialMinute, true)
    }


}