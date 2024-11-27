package com.example.fittr_app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
//import com.example.fittr_app.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    //private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val homeViewModel =
//            ViewModelProvider(this).get(HomeViewModel::class.java)
//
//        _binding = FragmentHomeBinding.inflate(inflater, container, false)
//        val root: View = binding.root
//
//        val textView: Button = binding.homeNextButton
//
//        return root
//    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

//    private fun navigateToAuth(){
//        lifecycleScope.launch {
//            Navigation.findNavController(
//                requireActivity(),
//                R.id.fragment_container
//            ).navigate(
//                R.id.action_navigation_to_auth
//        }
//    }
}