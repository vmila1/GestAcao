package com.example.appgest

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.appgest.databinding.ActivityMainBinding
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.appgest.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val searchView = binding.searchView

        // 1. Configurar o queryHint
        searchView.queryHint = "Pesquisar..."

        // 2. Manter o SearchView expandido (não iconificado)
        searchView.isIconified = false

        // 3. Alterar o ícone de busca (lupa)
        try {
            val searchIconId = androidx.appcompat.R.id.search_mag_icon
            val searchIcon: ImageView = searchView.findViewById(searchIconId)

            // Criar uma cópia do ícone e alterar a sua cor
            val searchIconDrawable: Drawable? = ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_search_api_material)
            searchIconDrawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it).mutate()
                DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.rose)) // Referência correta para a cor rose do colors.xml
                searchIcon.setImageDrawable(wrappedDrawable)
            }

            // 4. Alterar o ícone de limpar texto (X)
            val closeButtonId = androidx.appcompat.R.id.search_close_btn
            val closeButton: ImageView = searchView.findViewById(closeButtonId)

            // Criar uma cópia do ícone e alterar a sua cor
            val closeButtonDrawable: Drawable? = ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_clear_material)
            closeButtonDrawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it).mutate()
                DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.your_color_name)) // Defina a cor aqui
                closeButton.setImageDrawable(wrappedDrawable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }
}