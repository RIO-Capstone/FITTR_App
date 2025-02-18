package com.example.fittr_app.ui.registration

import RegistrationViewModel
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.fittr_app.R
import com.example.fittr_app.databinding.RegistrationPageThreeBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RegistrationPageThree : Fragment() {
    private lateinit var viewModel: RegistrationViewModel
    private var _binding: RegistrationPageThreeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val imageList by lazy { // Use lazy initialization
        listOf(
            requireContext().getDrawable(R.drawable.icons8_curls_with_dumbbells_48), // TODO: Replace with correct images
            requireContext().getDrawable(R.drawable.icons8_squat_64),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegistrationPageThreeBinding.inflate(inflater, container, false)
        val view = binding.root
        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]

        viewPager = binding.imageViewPager
        tabLayout = binding.imageTabLayout
        val adapter = ImagePagerAdapter(imageList)
        viewPager.adapter = adapter

        updateImageIndexLabel(1)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageIndexLabel(position)
            }
        })

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // You can customize the tabs here if needed (e.g., set icons)
            // tab.setIcon(R.drawable.your_icon)
        }.attach()

        return view;
    }

    fun updateImageIndexLabel(position: Int){
        val imageIndexLabelText = binding.registrationThreeImageIndexLabel
        imageIndexLabelText.text = "${position+1}/${imageList.size}"
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
