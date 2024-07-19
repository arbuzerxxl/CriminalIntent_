package com.example.criminalintent_

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateFormat as DF
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

private const val TAG = "CrimeListFragment"
private const val DATE_FORMAT = "EEEE, d MMMM, y, HH:mm"

class CrimeListFragment : Fragment() {

    fun interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null

    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var addCrimeButton: Button
    private lateinit var emptyListTextView: TextView

    private var adapter: CrimeAdapter? = CrimeAdapter()

    private val crimeListViewModel by lazy { ViewModelProvider(this)[CrimeListViewModel::class.java] }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks? // callbacks is host-activity
    }


    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.fragment_crime_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_crime -> {
                        val crime = Crime()
                        crimeListViewModel.addCrime(crime)
                        callbacks?.onCrimeSelected(crime.id)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner, Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            })
    }


    private fun updateUI(crimes: List<Crime>) {
        addCrimeButton = requireView().findViewById(R.id.add_crime)
        emptyListTextView = requireView().findViewById(R.id.list_empty)
        if (crimes.isNotEmpty()) {
            (crimeRecyclerView.adapter as CrimeAdapter).submitList(crimes)
            addCrimeButton.visibility = View.INVISIBLE
            emptyListTextView.visibility = View.INVISIBLE
        }
        else {
            addCrimeButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    val crime = Crime()
                    crimeListViewModel.addCrime(crime)
                    callbacks?.onCrimeSelected(crime.id)
                }
            }
            emptyListTextView.visibility = View.VISIBLE
        }
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var crime: Crime       // for makeToast message

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView? = itemView.findViewById(R.id.crime_solved)
        private val isRequireToPoliceImageView: ImageView? = itemView.findViewById(R.id.crime_is_require_to_police)


        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            val date = DF.getMediumDateFormat(context).format(crime.date)
            val time = DF.getTimeFormat(context).format(crime.date)
            dateTextView.text = buildString {
                append(date)
                append(" ")
                append(time)
            }
            solvedImageView?.visibility = if (crime.isSolved) ImageView.VISIBLE else ImageView.GONE
            isRequireToPoliceImageView?.visibility = if (crime.requiresPolice) ImageView.VISIBLE else ImageView.GONE
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }

    }

    private inner class CrimeAdapter() : ListAdapter<Crime, CrimeHolder>(CrimeComparator()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            var view: View? = null
            if (viewType == 0) {
                view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            } else if (viewType == 1) {
                view = layoutInflater.inflate(R.layout.list_item_crime_is_require_to_police, parent, false)
            }
            return view?.let { CrimeHolder(it) } ?: throw IllegalStateException("ViewType never be used")
        }

        override fun getItemViewType(position: Int): Int {
            return if (currentList[position].requiresPolice) 1 else 0
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = currentList[position]
            holder.bind(crime)
        }
    }

    private inner class CrimeComparator : DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }

    }


}