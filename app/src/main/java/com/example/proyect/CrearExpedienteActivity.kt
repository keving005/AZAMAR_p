package com.example.proyect

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CrearExpedienteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var pacienteId: String = ""
    private var pacienteNombre: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_expediente)

        pacienteId = intent.getStringExtra("PACIENTE_ID") ?: ""
        pacienteNombre = intent.getStringExtra("PACIENTE_NOMBRE") ?: ""

        findViewById<TextView>(R.id.tvNombrePacienteExp).text = pacienteNombre

        // Cargar datos existentes si ya tiene expediente
        cargarDatosExistentes()

        findViewById<Button>(R.id.btnGuardarExpediente).setOnClickListener {
            guardarExpediente()
        }
    }

    private fun cargarDatosExistentes() {
        db.collection("expedientes").document(pacienteId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val exp = doc.toObject(Expediente::class.java)
                // Llenar los campos con lo que haya
                findViewById<EditText>(R.id.etPesoExp).setText(exp?.peso)
                findViewById<EditText>(R.id.etAlturaExp).setText(exp?.altura)
                findViewById<EditText>(R.id.etSangreExp).setText(exp?.tipoSangre)
                findViewById<CheckBox>(R.id.cbDiabetesExp).isChecked = exp?.tieneDiabetes ?: false
                findViewById<CheckBox>(R.id.cbHipertensionExp).isChecked = exp?.esHipertenso ?: false
                findViewById<EditText>(R.id.etPadecimientoActualExp).setText(exp?.padecimientoActual)
                findViewById<EditText>(R.id.etAlergiasExp).setText(exp?.alergias)
                findViewById<EditText>(R.id.etContactoNombreExp).setText(exp?.contactoEmergenciaNombre)
                findViewById<EditText>(R.id.etContactoTelExp).setText(exp?.contactoEmergenciaTelefono)
            }
        }
    }

    private fun guardarExpediente() {
        // Recolectar datos
        val nuevoExpediente = Expediente(
            id = pacienteId, // El ID del expediente es el mismo del paciente
            pacienteId = pacienteId,
            pacienteNombre = pacienteNombre,
            peso = findViewById<EditText>(R.id.etPesoExp).text.toString(),
            altura = findViewById<EditText>(R.id.etAlturaExp).text.toString(),
            tipoSangre = findViewById<EditText>(R.id.etSangreExp).text.toString(),
            tieneDiabetes = findViewById<CheckBox>(R.id.cbDiabetesExp).isChecked,
            esHipertenso = findViewById<CheckBox>(R.id.cbHipertensionExp).isChecked,
            padecimientoActual = findViewById<EditText>(R.id.etPadecimientoActualExp).text.toString(),
            alergias = findViewById<EditText>(R.id.etAlergiasExp).text.toString(),
            contactoEmergenciaNombre = findViewById<EditText>(R.id.etContactoNombreExp).text.toString(),
            contactoEmergenciaTelefono = findViewById<EditText>(R.id.etContactoTelExp).text.toString(),

            // Metadatos
            ultimoHospital = "Hospital (ID: ${auth.currentUser?.uid})",
            ultimaActualizacion = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        )

        db.collection("expedientes").document(pacienteId).set(nuevoExpediente)
            .addOnSuccessListener {
                Toast.makeText(this, "Expediente guardado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}