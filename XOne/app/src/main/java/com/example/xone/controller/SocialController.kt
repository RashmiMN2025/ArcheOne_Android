package com.example.xone.controller

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.xone.model.Job
import com.example.xone.model.SocialArticle
import com.example.xone.model.SocialContent
import com.example.xone.network.ApiService
import com.example.xone.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class SocialController {
    private var _socialState by mutableStateOf(SocialContent())
    val socialContent: SocialContent get() = _socialState
    
    init {
        fetchSocialContent()
    }
    
    private fun fetchSocialContent() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.apiService
                val response = apiService.getSocialContent()
                
                if (response.isSuccessful) {
                    val socialData = response.body()
                    withContext(Dispatchers.Main) {
                        if (socialData != null) {
                            _socialState = socialData as SocialContent
                            Log.d("SocialController", "Fetched ${socialData.jobs.size} jobs")
                        } else {
                            Log.e("SocialController", "Social data response was null")
                        }
                    }
                } else {
                    Log.e("SocialController", "Failed to fetch social data: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SocialController", "Exception fetching social data", e)
            }
        }
    }
    
    fun getJobs(): List<Job> {
        return _socialState.jobs
    }
    
    fun getBlogs(): List<SocialArticle> {
        // Directly map the blogs list to SocialArticle objects
        return _socialState.blogs.map { blog ->
            SocialArticle(
                id = blog.Slug,
                title = blog.Title,
                description = blog.Description,
                imageUrl = blog.Image,
                content = blog.Content
            )
        }
    }
    
    fun getCaseStudies(): List<SocialArticle> {
        // Use actual case studies from the API if available
        if (_socialState.caseStudies.isNotEmpty()) {
            return _socialState.caseStudies.map { caseStudy ->
                SocialArticle(
                    id = caseStudy.Slug,
                    title = caseStudy.Title,
                    description = caseStudy.Description,
                    imageUrl = caseStudy.Image,
                    content = caseStudy.Content
                )
            }
        }
        
        // Fallback to filtering jobs if case studies aren't available
        return _socialState.jobs.filter { 
            it.Title.contains("Guide") || 
            it.Title.contains("Strategy") ||
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
    
    fun getJobPostings(): List<Job> {
        // Get actual job listings
        return _socialState.jobs.filter { job ->
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