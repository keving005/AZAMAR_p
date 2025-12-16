package com.example.proyect

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class DetalleRecetaPacienteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_receta_paciente)

        val idReceta = intent.getStringExtra("RECETA_ID") ?: ""

        if (idReceta.isNotEmpty()) {
            generarQR(idReceta)
            cargarDatos(idReceta)
        }
    }

    private fun generarQR(contenido: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(contenido, BarcodeFormat.QR_CODE, 600, 600)
            findViewById<ImageView>(R.id.ivQrDetalle).setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cargarDatos(id: String) {
        db.collection("recetas").document(id).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val receta = doc.toObject(Receta::class.java)

                findViewById<TextView>(R.id.tvDetalleDoc).text = "Dr. ${receta?.nombreDoctor}"
                findViewById<TextView>(R.id.tvDetalleInstrucciones).text = "Indicaciones:\n${receta?.instrucciones}"

                // Formatear lista de medicamentos en texto
                val sb = StringBuilder()
                receta?.medicamentos?.forEach { item ->
                    val nombre = item["nombre"] as? String ?: ""
                    val cant = item["cantidad"]
                    sb.append("• $nombre (Cant: $cant)\n")
                }
                findViewById<TextView>(R.id.tvListaMedicamentosTexto).text = sb.toString()
            }
        }
    }
}