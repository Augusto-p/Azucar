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
import com.augustop.azucar.database.AppDatabase
import com.augustop.azucar.database.MeasurementDao
import com.augustop.azucar.databinding.FragmentFirstBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirstFragment : Fragment() {
    private var db: AppDatabase? = null
    private var measurementDao: MeasurementDao? = null
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        measurementDao = db?.MeasurementDao()

        val webView: WebView = binding.webView
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
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
                loadMeasurementsIntoWebView(webView)
            }
        }

        class WebAppInterface(private val fragment: FirstFragment) {
            @android.webkit.JavascriptInterface
            fun delete(id: String) {
                val idInt = id.toInt()
                CoroutineScope(Dispatchers.IO).launch {
                    val id = fragment.measurementDao?.deleteById(idInt)
                    withContext(Dispatchers.Main) {
                        if (id != null) {
                            Toast.makeText(fragment.requireContext(), "Eliminado con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(fragment.requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            @android.webkit.JavascriptInterface
            fun edit(id: String) {
                val idInt = id.toInt()
                val action = FirstFragmentDirections.actionFirstFragmentToEditFragment(id = idInt.toString())
                findNavController().navigate(action)
            }

        }
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")
        // Cargar el archivo local
        webView.loadUrl("file:///android_asset/index.html")

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        return binding.root
    }

    private fun loadMeasurementsIntoWebView(webView: WebView) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val data = measurementDao?.getAll() ?: emptyList()
            val jsonData = Gson().toJson(data)
            withContext(Dispatchers.Main) {
                webView.evaluateJavascript("javascript:receiveData('${jsonData}')", null)

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
