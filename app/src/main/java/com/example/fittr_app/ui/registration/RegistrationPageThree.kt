package com.example.fittr_app.ui.registration

import RegistrationViewModel
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.fittr_app.DashboardActivity
import com.example.fittr_app.R
import com.example.fittr_app.connections.ApiClient
import com.example.fittr_app.connections.ApiPaths
import com.example.fittr_app.databinding.RegistrationPageThreeBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class RegistrationPageThree : Fragment() {
    private lateinit var viewModel: RegistrationViewModel
    private var _binding: RegistrationPageThreeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var apiClient: ApiClient
    private val imageList by lazy { // Use lazy initialization
        listOf(
            requireContext().getDrawable(R.drawable.icons8_curls_with_dumbbells_48), // TODO: Replace with correct images
            requireContext().getDrawable(R.drawable.icons8_squat_64),
        )
    }
    private val personaList by lazy {
        listOf("Strength Seeker","Muscle Sculptor") // Lean Machine
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegistrationPageThreeBinding.inflate(inflater, container, false)
        val view = binding.root
        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        apiClient = ApiClient()
        val completeRegistrationButton = binding.registrationThreeCompleteRegistration
        viewPager = binding.imageViewPager
        tabLayout = binding.imageTabLayout
        val adapter = ImagePagerAdapter(imageList)
        viewPager.adapter = adapter

        updateImageIndexLabel(1)
        updatePersonaText(1)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageIndexLabel(position)
                updatePersonaText(position)
            }
        })

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // You can customize the tabs here if needed (e.g., set icons)
            // tab.setIcon(R.drawable.your_icon)
        }.attach()

        completeRegistrationButton.setOnClickListener{
            viewLifecycleOwner.lifecycleScope.launch {
                completeRegistration()
            }
        }

        return view;
    }

    fun updateImageIndexLabel(position: Int){
        val imageIndexLabelText = binding.registrationThreeImageIndexLabel
        imageIndexLabelText.text = "${position+1}/${imageList.size}"
    }
    fun updatePersonaText(position: Int){
        val personaText = binding.registrationThreePersona
        personaText.text = personaList[position]
        viewModel.setFitnessGoal(personaList[position])
    }

    private suspend fun completeRegistration(){
        val userBackendData = viewModel.getRegistrationData()
        // send to backend using a HTTP request
        val result = apiClient.registerUser(ApiPaths.RegisterUser,userBackendData)
        Log.i("RegistrationPageThree","Registration result: ${result.isSuccess}")
        if(result.isSuccess){
            Toast.makeText(activity,"Registration Complete", Toast.LENGTH_LONG).show()
            val userId = result.getOrNull()?.user_id
            val navToDash = Intent(activity, DashboardActivity::class.java).apply{
                putExtra("user_id", userId)
            }
            startActivity(navToDash)
        }else{
            Toast.makeText(activity,"Registration Unsuccessful. ${result.getOrNull()?.error}",
                Toast.LENGTH_LONG).show()
        }
    }

    private inner class ImagePagerAdapter(private val images: List<Drawable?>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val imageView = ImageView(parent.context)
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            return ImageViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            holder.imageView.setImageDrawable(images[position])
        }

        override fun getItemCount(): Int = images.size

        inner class ImageViewHolder(val imageView: ImageView) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(imageView)
    }
}
