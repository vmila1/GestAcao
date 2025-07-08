package com.pjt.gestacao

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
// import androidx.navigation.ui.setupActionBarWithNavController // Já estava comentado
import androidx.navigation.ui.setupWithNavController
import com.pjt.gestacao.databinding.ActivityMainBinding
import androidx.activity.viewModels
import com.pjt.gestacao.ui.UserViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    // Instancia o ViewModel que será compartilhado por todos os fragments desta activity
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_chat_actual
            )
        )
        //setupActionBarWithNavController(navController, appBarConfiguration) // Mantém comentado
        navView.setupWithNavController(navController)

        // TRECHO PARA CONTROLAR A VISIBILIDADE DA BOTTOMNAVIGATIONVIEW
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_chat_actual) {
                binding.navView.visibility = View.GONE
            } else {
                binding.navView.visibility = View.VISIBLE
            }
        }
    }
}