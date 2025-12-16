package com.example.proyect

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExpedientePacienteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expediente_paciente)

        // Botón Regresar
        findViewById<ImageView>(R.id.btnBackExp).setOnClickListener { finish() }

        cargarExpediente()
    }

    private fun cargarExpediente() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("expedientes").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val exp = doc.toObject(Expediente::class.java)
                    mostrarDatos(exp)
                } else {
                    mostrarVacio()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar expediente", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDatos(exp: Expediente?) {
        if (exp == null) return

        // 1. Datos Biométricos (LOS TUYOS)
        findViewById<TextView>(R.id.tvSangre).text = exp.tipoSangre.ifEmpty { "--" }
        findViewById<TextView>(R.id.tvPeso).text = if (exp.peso.isNotEmpty()) "${exp.peso} kg" else "--"
        findViewById<TextView>(R.id.tvAltura).text = if (exp.altura.isNotEmpty()) "${exp.altura} cm" else "--"

        // 2. Padecimiento Actual (NUEVO)
        findViewById<TextView>(R.id.tvPadecimientoActual).text = exp.padecimientoActual.ifEmpty { "Ninguno activo" }

        // 3. Enfermedades Crónicas (Lógica de Colores)
        val tvHiper = findViewById<TextView>(R.id.tvEsHipertenso)
        if (exp.esHipertenso) {
            tvHiper.text = "SÍ"
            tvHiper.setTextColor(Color.RED)
        } else {
            tvHiper.text = "NO"
            tvHiper.setTextColor(Color.parseColor("#4CAF50")) // Verde
        }

        val tvDiab = findViewById<TextView>(R.id.tvTieneDiabetes)
        if (exp.tieneDiabetes) {
            tvDiab.text = "SÍ"
            tvDiab.setTextColor(Color.RED)
        } else {
            tvDiab.text = "NO"
            tvDiab.setTextColor(Color.parseColor("#4CAF50")) // Verde
        }

        // Otras Crónicas (TEXTO GENERAL RECUPERADO)
        findViewById<TextView>(R.id.tvOtrasCronicas).text = exp.padecimientosCronicos.ifEmpty { "Ninguna otra" }

        // 4. Otros Antecedentes
        findViewById<TextView>(R.id.tvAlergias).text = exp.alergias.ifEmpty { "Negadas" }
        findViewById<TextView>(R.id.tvCirugias).text = exp.cirugiasPrevias.ifEmpty { "Negadas" }
        findViewById<TextView>(R.id.tvHereditarios).text = exp.antecedentesHereditarios.ifEmpty { "--" }

        // 5. Contacto Emergencia (NUEVO)
        findViewById<TextView>(R.id.tvEmergenciaNombre).text = exp.contactoEmergenciaNombre.ifEmpty { "No registrado" }
        findViewById<TextView>(R.id.tvEmergenciaTel).text = exp.contactoEmergenciaTelefono.ifEmpty { "--" }

        // 6. Footer
        findViewById<TextView>(R.id.tvUltimaActualizacion).text = "Última actualización: ${exp.ultimaActualizacion}"
    }

    private fun mostrarVacio() {
        findViewById<TextView>(R.id.tvSangre).text = "?"
        findViewById<TextView>(R.id.tvPadecimientoActual).text = "Tu expediente aún no ha sido creado."
        Toast.makeText(this, "Tu expediente está vacío.", Toast.LENGTH_LONG).show()
    }
}