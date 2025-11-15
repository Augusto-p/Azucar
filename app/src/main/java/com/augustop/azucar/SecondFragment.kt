package com.augustop.azucar

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.augustop.azucar.database.AppDatabase
import com.augustop.azucar.database.Measurement
import com.augustop.azucar.database.MeasurementDao
import com.augustop.azucar.databinding.FragmentSecondBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecondFragment : Fragment() {
    private var db: AppDatabase? = null
    var measurementDao: MeasurementDao? = null
    private var _binding: FragmentSecondBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = AppDatabase.getDatabase(requireContext())
        measurementDao = db?.MeasurementDao()


        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        val webView: WebView = binding.webView
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true           // <-- importante
            allowUniversalAccessFromFileURLs = true      // <-- importante si haces fetch/ajax
        }

        var theme = "light"
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                theme = "dark"
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.evaluateJavascript("javascript:setTheme('${theme}')", null)
            }
        }

        class WebAppInterface(private val fragment: SecondFragment) {
            @android.webkit.JavascriptInterface
            fun close() {
                fragment.requireActivity().runOnUiThread {
                    fragment.findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }
            }
            @android.webkit.JavascriptInterface
            fun save(valor: String) {
                val valorInt = valor.toInt()
                val measurement = Measurement(value = valorInt, date = System.currentTimeMillis())
                // Ejecutar en corrutina
                CoroutineScope(Dispatchers.IO).launch {
                    val id = fragment.measurementDao?.insert(measurement)
                    withContext(Dispatchers.Main) {
                        if (id != null) {
                            Toast.makeText(fragment.requireContext(), "Guardado con éxito", Toast.LENGTH_SHORT).show()
                            fragment.findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                        } else {
                            Toast.makeText(fragment.requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")
        // Cargar el archivo local
        webView.loadUrl("file:///android_asset/add.html")


        return binding.root

    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}