package com.example.fittr_app.ui.registration

import RegistrationViewModel
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
    private lateinit var apiClient: ApiClient
    private val imageList by lazy { // Use lazy initialization
        listOf(
            R.mipmap.improve_shape_foreground,
            R.mipmap.lean_and_tone_foreground,
            R.mipmap.lose_fat_foreground,
        )
    }
    private val personaList by lazy {
        listOf("Strength Seeker","Muscle Sculptor","Lean Machine")
    }
    private val personaToDescription by lazy {
        mapOf(personaList[0] to "Build upper body strength",
            personaList[1] to "Focused on hypertrophy and strength",
            personaList[2] to "Lose fat and gain muscle mass")
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
        val resizedDrawables = getResizedDrawables()
        val adapter = ImagePagerAdapter(resizedDrawables)
        viewPager.adapter = adapter

        updatePage(1)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePage(position)
            }
        })

        completeRegistrationButton.setOnClickListener{
            viewLifecycleOwner.lifecycleScope.launch {
                completeRegistration()
            }
        }

        return view;
    }

    fun updatePage(position: Int){
        val imageIndexLabelText = binding.registrationThreeImageIndexLabel
        imageIndexLabelText.text = "${position+1}/${imageList.size}"
        val personaText = binding.registrationThreePersona
        personaText.text = personaList[position]
        viewModel.setFitnessGoal(personaList[position])
        val personaDescriptionText = binding.registrationThreePersonaDescription
        personaDescriptionText.text = personaToDescription[personaList[position]]
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
            Log.e("RegistrationPageThree","Error response: ${result.getOrNull()}")
        }
    }

    private fun getResizedDrawables(): List<Drawable> {
        val resizedDrawables = mutableListOf<Drawable>()

        for (imageId in imageList) {
            val drawable = ContextCompat.getDrawable(requireContext(), imageId)
            val resizedDrawable = resizeDrawable(drawable)
            resizedDrawables.add(resizedDrawable)
        }

        return resizedDrawables
    }

    private fun resizeDrawable(drawable: Drawable?): Drawable {
        if (drawable == null) return drawable!!

        val bitmap = (drawable as BitmapDrawable).bitmap

        val newWidth = bitmap.width * 3
        val newHeight = bitmap.height * 3

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        return BitmapDrawable(resources, resizedBitmap)
    }

    private inner class ImagePagerAdapter(private val images: List<Drawable?>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val imageView = ImageView(parent.context)
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
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
