package com.example.criminalintent_

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

private const val ARG_DATE = "date"
private const val ARG_REQUEST_DATE = "requestDateKey"

class DatePickerFragment : DialogFragment() {

    companion object {
        fun newInstance(date: Date, requestDate: String): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
                putString(ARG_REQUEST_DATE, requestDate)
            }
            return DatePickerFragment().apply { arguments = args }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_DATE, Date::class.java)
        } else {
            arguments?.getSerializable(ARG_DATE) as Date
        }

        val calendar = Calendar.getInstance()
        calendar.time = date!!

        val dateListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->

            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            val resultDate = calendar.time
            val resultRequestCode = requireArguments().getString(ARG_REQUEST_DATE, "")
            val result = Bundle().apply {
                putSerializable(resultRequestCode, resultDate)
            }

            this@DatePickerFragment.parentFragmentManager.setFragmentResult(resultRequestCode, result)
        }


        val initialYear = calendar[Calendar.YEAR]
        val initialMonth = calendar[Calendar.MONTH]
        val initialDay = calendar[Calendar.DAY_OF_MONTH]

        return DatePickerDialog(requireContext(), dateListener, initialYear, initialMonth, initialDay)
    }

}