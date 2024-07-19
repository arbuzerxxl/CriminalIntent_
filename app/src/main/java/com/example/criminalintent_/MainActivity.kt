package com.example.criminalintent_

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // supportFragmentManager need to control list of fragments using transactions (add, remove, swap...)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            val fragment = CrimeListFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
        }
    }

    override fun onCrimeSelected(crimeId: UUID) {
        Log.d(TAG, "MainActivity.onCrimeSelected: $crimeId")
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()

    }
}