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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.augustop.azucar.database.AppDatabase
import com.augustop.azucar.database.MeasurementDao
import com.augustop.azucar.databinding.FragmentEditBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditFragment : Fragment() {
    private var db: AppDatabase? = null
    private var measurementDao: MeasurementDao? = null
    private var _binding: FragmentEditBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = AppDatabase.getDatabase(requireContext())
        measurementDao = db?.MeasurementDao()
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val args: EditFragmentArgs by navArgs()
        val id = args.id
        val idInt = id.toInt()

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
                // Cuando la página esté lista, cargamos los datos
                webView.evaluateJavascript("javascript:setTheme('${theme}')", null)
                loadMeasurementIntoWebView(webView, idInt)
            }
        }


        class WebAppInterface(private val fragment: EditFragment) {
            @android.webkit.JavascriptInterface
            fun close() {
                fragment.requireActivity().runOnUiThread {
                    fragment.findNavController().navigate(R.id.action_EditFragment_to_FirstFragment)
                }
            }


            @android.webkit.JavascriptInterface
            fun save(id : String, valor: String) {
                val valorInt = valor.toInt()
                val idInt = id.toInt()
                CoroutineScope(Dispatchers.IO).launch {
                    val measurement = db?.MeasurementDao()?.getById(idInt)
                    if (measurement != null) {
                        measurement.value = valorInt
                        db?.MeasurementDao()?.update(measurement)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(fragment.requireContext(), "Guardado con éxito", Toast.LENGTH_SHORT).show()
                            fragment.findNavController().navigate(R.id.action_EditFragment_to_FirstFragment)
                        }
                    }else{
                        withContext(Dispatchers.Main) {
                            Toast.makeText(fragment.requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")
        // Cargar el archivo local
        webView.loadUrl("file:///android_asset/edit.html")


        return binding.root

    }


    private fun loadMeasurementIntoWebView(webView: WebView, id: Int) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val data = measurementDao?.getById(id)
            val jsonData = Gson().toJson(data)
            withContext(Dispatchers.Main) {
                webView.evaluateJavascript("javascript:loadData('${jsonData}')", null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}