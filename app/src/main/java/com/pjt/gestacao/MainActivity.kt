package com.pjt.gestacao

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
// Importações necessárias para a solução
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pjt.gestacao.databinding.ActivityMainBinding
import com.pjt.gestacao.ui.UserViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val userViewModel: UserViewModel by viewModels()

    // É uma boa prática declarar o NavController aqui para fácil acesso
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Encontrar o NavHostFragment no layout pelo seu ID
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        // Obter o NavController a partir do NavHostFragment
        navController = navHostFragment.navController

        val navView: BottomNavigationView = binding.navView

        // Configurar a BottomNavigationView com o NavController obtido da forma correta
        navView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_chat_actual
            )
        )

        // TRECHO PARA CONTROLAR A VISIBILIDADE DA BOTTOMNAVIGATIONVIEW
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_chat_actual, R.id.entryFragment, R.id.onboardingFragment -> {
                    binding.navView.visibility = View.GONE
                }
                else -> {
                    binding.navView.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Controla a visibilidade do ProgressBar principal da Activity.
     */
    fun setProgressBar(isVisible: Boolean) {
        val progressBar = findViewById<ProgressBar>(R.id.main_progress_bar)
        progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}