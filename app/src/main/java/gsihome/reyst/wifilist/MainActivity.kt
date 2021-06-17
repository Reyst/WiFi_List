package gsihome.reyst.wifilist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import gsihome.reyst.wifilist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(LayoutInflater.from(this)) }

    private val wifiManager: WifiManager by lazy { applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }

    private val resultCallback: Any? by lazy {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) null
        else obtainScanResultCallback()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun obtainScanResultCallback(): WifiManager.ScanResultsCallback {
        return object : WifiManager.ScanResultsCallback() {
            override fun onScanResultsAvailable() = updateView()
        }
    }

    private val resultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root) // R.layout.activity_main

        initListener()

        @Suppress("DEPRECATION")
        wifiManager.startScan()
    }

    private fun updateView() {
        wifiManager.scanResults
            .joinToString(separator = "\n") { it.SSID ?: "unknown: ${it.timestamp}" }
            .also { binding.text.text = it }
    }

    private fun initListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            registerCallback()
        } else {
            registerBroadcast()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun registerCallback() {
        val callback = (resultCallback as? WifiManager.ScanResultsCallback) ?: return
        wifiManager.registerScanResultsCallback(mainExecutor, callback)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun unregisterCallback() {
        val callback = (resultCallback as? WifiManager.ScanResultsCallback) ?: return
        wifiManager.unregisterScanResultsCallback(callback)
    }

    private fun registerBroadcast() {
        registerReceiver(resultReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    private fun unregisterBroadcast() {
        unregisterReceiver(resultReceiver)
    }

    private fun removeListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            unregisterCallback()
        } else {
            unregisterBroadcast()
        }
    }

    override fun onDestroy() {
        removeListener()
        super.onDestroy()
    }



}