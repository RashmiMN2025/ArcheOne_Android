package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.archeGlobal.one.model.Job
import com.archeGlobal.one.model.SocialArticle
import com.archeGlobal.one.model.SocialContent
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Singleton that preloads and caches social content data for the Connect page
 * to improve loading performance.
 */
class SocialDataProvider private constructor(private val applicationContext: Context) {

    // Cached data
    private var _socialContent = SocialContent()
    val socialContent: SocialContent get() = _socialContent

    // Status flags
    private var _isLoading = false
    private var _isLoaded = false
    val isLoaded: Boolean get() = _isLoaded

    // Cached processed data
    private var _blogs: List<SocialArticle> = emptyList()
    private var _caseStudies: List<SocialArticle> = emptyList()
    private var _jobs: List<Job> = emptyList()

    val blogs: List<SocialArticle> get() = _blogs
    val caseStudies: List<SocialArticle> get() = _caseStudies
    val jobs: List<Job> get() = _jobs

    /**
     * Preload data if not already loaded
     */
    fun preloadData() {
        if (_isLoaded || _isLoading) return

        _isLoading = true
        Log.d(TAG, "Preloading social content data...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.apiService
                val response = apiService.getSocialContent()

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        withContext(Dispatchers.Main) {
                            _socialContent = data
                            processData()
                            _isLoaded = true
                            _isLoading = false
                            Log.d(TAG, "Social content preloaded successfully")
                        }
                    } else {
                        Log.e(TAG, "Social content response was null")
                        _isLoading = false
                    }
                } else {
                    Log.e(TAG, "Failed to fetch social data: ${response.code()}")
                    _isLoading = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching social data", e)
                _isLoading = false
            }
        }
    }

    /**
     * Process the raw data into usable formats
     */
    private fun processData() {
        // Process blogs
        _blogs = _socialContent.blogs.map { blog ->
            SocialArticle(
                id = blog.Slug,
                title = blog.Title,
                description = blog.Description,
                imageUrl = blog.Image,
                content = blog.Content
            )
        }

        // Process case studies
        _caseStudies = if (_socialContent.caseStudies.isNotEmpty()) {
            _socialContent.caseStudies.map { caseStudy ->
                SocialArticle(
                    id = caseStudy.Slug,
                    title = caseStudy.Title,
                    description = caseStudy.Description,
                    imageUrl = caseStudy.Image,
                    content = caseStudy.Content
                )
            }
        } else {
            // Fallback to filtering jobs if case studies aren't available
            _socialContent.jobs.filter {
                it.Title.contains("Guide") || it.Title.contains("Strategy") ||
                    it.Slug.contains("guide") ||
                    it.Slug.contains("strategy")
            }.map { job ->
                SocialArticle(
                    id = job.Slug,
                    title = job.Title,
                    description = job.Description,
                    imageUrl = job.Image,
                    content = job.Content
                )
            }
        }

        // Process jobs
        _jobs = _socialContent.jobs.filter { job ->
            // Jobs have specific characteristics like experience requirements
            job.Description.contains("Experience") ||
                job.Description.contains("yrs") ||
                job.Title.contains("Manager") ||
                job.Title.contains("Engineer") ||
                job.Title.contains("Lead") ||
                job.Title.contains("L1") ||
                job.Title.contains("L2") ||
                job.Title.contains("L3") ||
                job.Title.contains("SME") ||
                job.Title.contains("Sales") ||
                job.Title.contains("Presales") ||
                job.Title.contains("Security") ||
                job.Title.contains("Practice")
        }

        Log.d(TAG, "Processed ${_blogs.size} blogs, ${_caseStudies.size} case studies, ${_jobs.size} jobs")

        // Preload images in the background
        preloadImages()
    }

    /**
     * Preload images to improve loading performance
     */
    private fun preloadImages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting image preloading...")

                // Create a list of all image URLs
                val imageUrls = mutableListOf<String>()

                // Add blog images
                _blogs.forEach { blog ->
                    blog.imageUrl.takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
                }

                // Add case study images
                _caseStudies.forEach { caseStudy ->
                    caseStudy.imageUrl.takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
                }

                // Add job images
                _jobs.forEach { job ->
                    job.Image.takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
                }

                Log.d(TAG, "Found ${imageUrls.size} images to preload")

                // Start preloading images
                imageUrls.forEach { url ->
                    try {
                        // Use Coil library to preload the image
                        val request = ImageRequest.Builder(applicationContext)
                            .data(url)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build()

                        ImageLoader(applicationContext).enqueue(request)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error preloading image: $url", e)
                    }
                }

                Log.d(TAG, "Image preloading initiated")
            } catch (e: Exception) {
                Log.e(TAG, "Error during image preloading", e)
            }
        }
    }

    /**
     * Force reload of data
     */
    fun refreshData() {
        _isLoaded = false
        _isLoading = false
        preloadData()
    }

    companion object {
        private const val TAG = "SocialDataProvider"
        private var INSTANCE: SocialDataProvider? = null

        fun getInstance(context: Context): SocialDataProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocialDataProvider(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
