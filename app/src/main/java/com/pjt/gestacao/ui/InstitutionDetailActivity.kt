package com.pjt.gestacao.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pjt.gestacao.R
import com.pjt.gestacao.model.InstitutionsData

class InstitutionDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Esconde a ActionBar
        setContentView(R.layout.activity_institution_detail)

        val institutionId = intent.getIntExtra("INST_ID", -1)

        if (institutionId != -1) {
            val institution = InstitutionsData.institutions.find { it.id == institutionId }

            if (institution != null) {
                val imgLogoDetail: ImageView = findViewById(R.id.imgLogoDetail)
                val tvNameDetail: TextView = findViewById(R.id.tvNameDetail)
                val tvTypeDetail: TextView = findViewById(R.id.tvTypeDetail)
                val tvDescriptionDetail: TextView = findViewById(R.id.tvDescriptionDetail)
                val tvAddressDetail: TextView = findViewById(R.id.tvAddressDetail)
                val tvPhoneDetail: TextView = findViewById(R.id.tvPhoneDetail)
                val tvSiteDetail: TextView = findViewById(R.id.tvSiteDetail)

                imgLogoDetail.setImageResource(institution.image)
                tvNameDetail.text = institution.name
                tvTypeDetail.text = institution.type
                tvDescriptionDetail.text = institution.description
                tvAddressDetail.text = institution.address
                tvPhoneDetail.text = institution.phone
                tvSiteDetail.text = institution.site
            }
        }
    }
}