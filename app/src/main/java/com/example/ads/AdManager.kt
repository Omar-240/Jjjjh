package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem

// Safe context extension to traverse context wrappers and find active Activity
fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

object AdManager {
    private const val TAG = "AdManager"

    // AdMob Unit IDs provided by the user
    const val APP_ID = "ca-app-pub-6764749512085863~497110600"
    const val BANNER_DASHBOARD = "ca-app-pub-6764749512085863/7009201401"
    const val BANNER_GENERIC = "ca-app-pub-6764749512085863/9573831168"
    const val INTERSTITIAL_NAVIGATION = "ca-app-pub-6764749512085863/5947401173"
    const val REWARDED_GAMES = "ca-app-pub-6764749512085863/9663697591"
    const val INTERSTITIAL_SAVE = "ca-app-pub-6764749512085863/5755829488"
    const val APP_OPEN_AD = "ca-app-pub-6764749512085863/4513076175"

    // Preloaded Ad Instances
    private var interstitialNavAd: InterstitialAd? = null
    private var interstitialSaveAd: InterstitialAd? = null
    private var rewardedGamesAd: RewardedAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var isInitializing = false
    private var isInitialized = false

    // Initialize the AdMob SDK
    fun initialize(context: Context) {
        if (isInitialized || isInitializing) return
        isInitializing = true
        Log.d(TAG, "Initializing Google Mobile Ads SDK...")
        
        try {
            MobileAds.initialize(context) { initializationStatus ->
                isInitialized = true
                isInitializing = false
                Log.d(TAG, "AdMob SDK Initialized: $initializationStatus")
                
                // Preload ads once initialized
                loadInterstitialNavigation(context)
                loadInterstitialSave(context)
                loadRewardedGames(context)
                loadAppOpenAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob SDK", e)
            isInitializing = false
        }
    }

    // Load Interstitial Navigation Ad
    fun loadInterstitialNavigation(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_NAVIGATION,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialNavAd = ad
                    Log.d(TAG, "Interstitial Navigation Ad Loaded successfully.")
                    
                    interstitialNavAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialNavAd = null
                            Log.d(TAG, "Interstitial Navigation Ad Dismissed. Reloading...")
                            loadInterstitialNavigation(context)
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            interstitialNavAd = null
                            Log.e(TAG, "Failed to show Interstitial Navigation: ${error.message}")
                            loadInterstitialNavigation(context)
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load Interstitial Navigation: ${error.message}")
                    interstitialNavAd = null
                }
            }
        )
    }

    // Show Interstitial Navigation Ad (Accepts Context safely)
    fun showInterstitialNavigation(context: Context, onAdDismissed: () -> Unit) {
        val activity = context.findActivity()
        if (activity != null && interstitialNavAd != null) {
            interstitialNavAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialNavAd = null
                    Log.d(TAG, "Interstitial Navigation Ad Dismissed.")
                    onAdDismissed()
                    loadInterstitialNavigation(activity)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialNavAd = null
                    Log.e(TAG, "Failed to show Interstitial Navigation: ${error.message}")
                    onAdDismissed()
                    loadInterstitialNavigation(activity)
                }
            }
            interstitialNavAd?.show(activity)
        } else {
            Log.d(TAG, "Interstitial Navigation ad not ready or context is not activity.")
            onAdDismissed()
            if (activity != null) loadInterstitialNavigation(activity)
        }
    }

    // Load Interstitial Save Ad
    fun loadInterstitialSave(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_SAVE,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialSaveAd = ad
                    Log.d(TAG, "Interstitial Save Ad Loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load Interstitial Save: ${error.message}")
                    interstitialSaveAd = null
                }
            }
        )
    }

    // Show Interstitial Save Ad (Accepts Context safely)
    fun showInterstitialSave(context: Context, onAdDismissed: () -> Unit) {
        val activity = context.findActivity()
        if (activity != null && interstitialSaveAd != null) {
            interstitialSaveAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialSaveAd = null
                    Log.d(TAG, "Interstitial Save Ad Dismissed.")
                    onAdDismissed()
                    loadInterstitialSave(activity)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialSaveAd = null
                    Log.e(TAG, "Failed to show Interstitial Save: ${error.message}")
                    onAdDismissed()
                    loadInterstitialSave(activity)
                }
            }
            interstitialSaveAd?.show(activity)
        } else {
            Log.d(TAG, "Interstitial Save ad not ready or context is not activity.")
            onAdDismissed()
            if (activity != null) loadInterstitialSave(activity)
        }
    }

    // Load Rewarded Games Ad
    fun loadRewardedGames(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_GAMES,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedGamesAd = ad
                    Log.d(TAG, "Rewarded Games Ad Loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load Rewarded Games Ad: ${error.message}")
                    rewardedGamesAd = null
                }
            }
        )
    }

    // Show Rewarded Games Ad (Accepts Context safely)
    fun showRewardedGames(context: Context, onRewarded: (RewardItem) -> Unit, onAdClosed: () -> Unit) {
        val activity = context.findActivity()
        if (activity != null && rewardedGamesAd != null) {
            rewardedGamesAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedGamesAd = null
                    Log.d(TAG, "Rewarded Games Ad Dismissed.")
                    onAdClosed()
                    loadRewardedGames(activity)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedGamesAd = null
                    Log.e(TAG, "Failed to show Rewarded Games Ad: ${error.message}")
                    onAdClosed()
                    loadRewardedGames(activity)
                }
            }
            rewardedGamesAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewarded(rewardItem)
            }
        } else {
            Log.d(TAG, "Rewarded Games Ad not ready or context is not activity.")
            onAdClosed()
            if (activity != null) loadRewardedGames(activity)
        }
    }

    // Load App Open Ad
    fun loadAppOpenAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            APP_OPEN_AD,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    Log.d(TAG, "App Open Ad Loaded.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load App Open Ad: ${error.message}")
                    appOpenAd = null
                }
            }
        )
    }

    // Show App Open Ad
    fun showAppOpenAd(context: Context) {
        val activity = context.findActivity()
        if (activity != null && appOpenAd != null) {
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    Log.d(TAG, "App Open Ad Dismissed.")
                    loadAppOpenAd(activity)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    appOpenAd = null
                    Log.e(TAG, "Failed to show App Open Ad: ${error.message}")
                    loadAppOpenAd(activity)
                }
            }
            appOpenAd?.show(activity)
        } else {
            if (activity != null) loadAppOpenAd(activity)
        }
    }
}

@Composable
fun AdBannerView(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        factory = { context ->
            FrameLayout(context).apply {
                val adView = AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    val adRequest = AdRequest.Builder().build()
                    loadAd(adRequest)
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("AdBannerView", "Ad banner loaded successfully: $adUnitId")
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("AdBannerView", "Ad banner failed to load: ${error.message}")
                        }
                    }
                }
                addView(adView)
            }
        }
    )
}
