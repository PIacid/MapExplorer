package com.devplacid.mapexplorer.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.devplacid.mapexplorer.R
import com.devplacid.mapexplorer.api.Category
import kotlinx.android.synthetic.main.activity_selection.*

class SelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        val prev = intent.getStringExtra("category")

        val adapter = CategoryAdapter(prev)

        recyclerCategorySelection.layoutManager = LinearLayoutManager(this)
        recyclerCategorySelection.hasFixedSize()

        recyclerCategorySelection.adapter = adapter
    }



    fun setItemSelected(apiName: String) {
        setResult(RESULT_OK, Intent().putExtra("apiName", apiName))
        finish()
    }


}