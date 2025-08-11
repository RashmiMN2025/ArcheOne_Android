package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.model.Job
import com.archeGlobal.one.model.SocialArticle
import com.archeGlobal.one.model.SocialContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SocialController(private val context: Context) {
    // Get reference to the data provider
    private val dataProvider = SocialDataProvider.getInstance(context)

    // Local state for UI
    private var _socialState by mutableStateOf(SocialContent())
    val socialContent: SocialContent get() = _socialState

    init {
        // If data is already loaded, use it immediately; otherwise fetch it
        if (dataProvider.isLoaded) {
            _socialState = dataProvider.socialContent
            Log.d("SocialController", "Using preloaded social content data")
        } else {
            // Ensure data is being loaded
            dataProvider.preloadData()

            // Also trigger a local fetch to update the UI state when data becomes available
            fetchSocialContent()
        }
    }

    private fun fetchSocialContent() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Wait for data to be available or timeout after 10 seconds
                var attempts = 0
                while (!dataProvider.isLoaded && attempts < 20) {
                    attempts++
                    kotlinx.coroutines.delay(500)
                }

                withContext(Dispatchers.Main) {
                    if (dataProvider.isLoaded) {
                        _socialState = dataProvider.socialContent
                        Log.d("SocialController", "Updated UI with preloaded data")
                    } else {
                        Log.e("SocialController", "Timed out waiting for data")
                    }
                }
            } catch (e: Exception) {
                Log.e("SocialController", "Exception waiting for social data", e)
            }
        }
    }

    fun getJobs(): List<Job> {
        return if (dataProvider.isLoaded) {
            dataProvider.jobs
        } else {
            _socialState.jobs
        }
    }

    fun getBlogs(): List<SocialArticle> {
        return if (dataProvider.isLoaded) {
            dataProvider.blogs
        } else {
            // Fallback to local processing - use Content for longer descriptions like case studies
            _socialState.blogs.map { blog ->
                SocialArticle(
                    id = blog.Slug,
                    title = blog.Title,
                    description = blog.Content?.takeIf { it.isNotEmpty() } ?: blog.Description,
                    imageUrl = blog.Image,
                    content = blog.Content
                )
            }
        }
    }

    fun getCaseStudies(): List<SocialArticle> {
        return if (dataProvider.isLoaded) {
            dataProvider.caseStudies
        } else {
            // Fallback to local processing if data isn't preloaded
            // Use actual case studies from the API if available
            if (_socialState.caseStudies.isNotEmpty()) {
                _socialState.caseStudies.map { caseStudy ->
                    SocialArticle(
                        id = caseStudy.Slug,
                        title = caseStudy.Title,
                        description = caseStudy.Content?.takeIf { it.isNotEmpty() } ?: caseStudy.Description,
                        imageUrl = caseStudy.Image,
                        content = caseStudy.Content
                    )
                }
            } else {
                // Fallback to filtering jobs if case studies aren't available
                _socialState.jobs.filter {
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
        }
    }

    fun getJobPostings(): List<Job> {
        return if (dataProvider.isLoaded) {
            dataProvider.jobs
        } else {
            // Fallback to local processing
            _socialState.jobs.filter { job ->
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
        }
    }

    fun openInBrowser(type: String, slug: String) {
        val baseUrl = "https://arche.global"
        val url = when (type) {
            "Case Studies" -> "$baseUrl/case-studies/$slug"
            "Blogs" -> "$baseUrl/blog/$slug"
            else -> "$baseUrl/jobs/$slug"
        }

        // Use WebViewActivity instead of external browser
        val intent = Intent(context, WebViewActivity::class.java).apply {
            putExtra("fileUrl", url)
            putExtra(
                "title",
                when (type) {
                    "Case Studies" -> "Case Study"
                    "Blogs" -> "Blog"
                    else -> "Job Details"
                }
            )
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        Log.d("SocialController", "Opening $type link in WebViewActivity: $url")
    }
}
