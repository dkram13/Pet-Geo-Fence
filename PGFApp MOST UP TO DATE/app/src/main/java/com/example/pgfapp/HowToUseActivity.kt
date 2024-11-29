package com.example.pgfapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfapp.databinding.ActivityHowToUseBinding

class HowToUseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHowToUseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHowToUseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = title
    }

    fun backToHub(view: View?) {
        startActivity(Intent(this@HowToUseActivity, HubActivity::class.java))
    }
}