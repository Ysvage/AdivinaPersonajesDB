package com.example.advart

import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.get
import androidx.core.view.marginTop
import com.airbnb.lottie.LottieAnimationView
import com.github.javafaker.Faker
import com.google.android.flexbox.FlexboxLayout
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var sp: SoundPool? = null
    private var click = 0
    private var victoria = 0
    private var derrota = 0

    private lateinit var txtPregunta: TextView
    private var respuesta: String = ""
    private lateinit var flexAlfabeto: FlexboxLayout
    private lateinit var flexResponse: FlexboxLayout
    private var indicesOcupados: ArrayList<Int> = arrayListOf()
    private var intentosPermitidos: Int = 0
    private var intentosHechos: Int = 0
    private lateinit var txtCantIntentos: TextView
    private lateinit var txtMsjIntentos: TextView
    private var finalizado: Boolean = false
    private lateinit var lottieResult: LottieAnimationView
    private lateinit var lotieAnimThinking: LottieAnimationView
    private lateinit var txtMsjResultado: TextView
    private lateinit var txtMsjRespuestaCorrecta: TextView

    private lateinit var btnReinicio: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //carga los sonidos desde la carpeta raw
       sp = SoundPool(1, AudioManager.STREAM_MUSIC, 1)
        click = sp?.load(this, R.raw.click, 1)!!
        victoria = sp?.load(this, R.raw.victoria, 1)!!
        derrota = sp?.load(this, R.raw.derrota, 1)!!

        installSplashScreen()

        setContentView(R.layout.activity_main)

        txtPregunta = findViewById(R.id.txtPregunta)
        lotieAnimThinking = findViewById(R.id.animation_view_thik)
        flexResponse = findViewById(R.id.edt)
        flexAlfabeto = findViewById(R.id.flexboxLayout)
        txtCantIntentos = findViewById(R.id.txtCantIntentos)
        txtMsjIntentos = findViewById(R.id.txtMsjIntentos)
        lottieResult = findViewById(R.id.animation_view_resultado)
        txtMsjResultado = findViewById(R.id.txtMsjResultado)
        txtMsjRespuestaCorrecta = findViewById(R.id.txtMsjRespuestaCorrecta)
        btnReinicio = findViewById(R.id.btnReinicio)


        respuesta = obtenerPalabraAleatoria().uppercase()
        intentosPermitidos = respuesta.length + 2
        txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"
        val alfabeto = generarAlfabeto(respuesta)
        val alfabetoDesorden = desordenar(alfabeto)
        mostrarEspacioRespuesta(respuesta.length, flexResponse)
        mostarAlfabeto(alfabetoDesorden.uppercase(), flexAlfabeto)


    }
    fun generarAlfabeto(semilla: String): String {
        val randomValues = List(5) { Random.nextInt(65, 90).toChar() }
        return "$semilla${randomValues.joinToString(separator = "")}"
    }
    fun desordenar(theWord: String): String {

        val theTempWord = theWord.toMutableList()

        for (item in 0..Random.nextInt(1, theTempWord.count() - 1)) {
            val indexA = Random.nextInt(theTempWord.count() - 1)
            val indexB = Random.nextInt(theTempWord.count() - 1)
            val temp = theTempWord[indexA]
            theTempWord[indexA] = theTempWord[indexB]
            theTempWord[indexB] = temp
        }
        return theTempWord.joinToString(separator = "")
    }
    fun obtenerPalabraAleatoria(): String {
        val faker = Faker()
        val palabra = faker.dragonBall().character()

        return palabra.split(' ').get(0)
    }
    fun mostrarEspacioRespuesta(cantidad: Int, vista: FlexboxLayout) {
        for (letter in 1..cantidad) {
            val btnLetra = EditText(this)
            btnLetra.isEnabled = false
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView(btnLetra)
        }
    }
    fun mostarAlfabeto(alfabeto: String, vista: FlexboxLayout) {
        for (letter in alfabeto) {
            val btnLetra = Button(this)
            btnLetra.text = letter.toString()
            btnLetra.textSize = 12f
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView((btnLetra))

            btnLetra.setOnClickListener {
                clickLetra(it as Button)
            }
        }
    }
    fun clickLetra(btnClicked: Button) {
        if (!finalizado) {
            var starIndex = 0
            var resIndex = respuesta.indexOf(btnClicked.text.toString())
            while (indicesOcupados.contains(resIndex)) {
                starIndex = resIndex + 1
                resIndex = respuesta.indexOf(btnClicked.text.toString(), starIndex)
            }
            if (resIndex != -1) {
                val flexRow = flexResponse.get(resIndex) as EditText
                flexRow.setText(respuesta.get(resIndex).toString())
                indicesOcupados.add(resIndex)
                btnClicked.setBackgroundColor(Color.GREEN)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(Color.WHITE)
            } else {
                Toast.makeText(
                    applicationContext, "No es una letra valida",
                    Toast.LENGTH_SHORT
                ).show()
                btnClicked.setBackgroundColor(Color.RED)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(Color.WHITE)
            }

            reproduceSoundPool()
            intentosHechos++

            txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"
            verificarResultado()
        }
    }
    fun verificarResultado() {

        if (intentosHechos == intentosPermitidos || indicesOcupados.size == respuesta.length) {
            finalizado = true

            if (indicesOcupados.size == respuesta.length) {
                lottieResult.setAnimation(R.raw.winner)
                txtMsjResultado.text = "Felicidades!"
               sonidoVictoria()
            } else {
                lottieResult.setAnimation(R.raw.lost)
                txtMsjResultado.text = "Perdiste :("
               sonidoDerrota()
            }

            txtMsjRespuestaCorrecta.setText("La respuesta correcta es: $respuesta")



            btnReinicio.setOnClickListener {

                val intent: Intent = Intent(this, MainActivity:: class.java)
                startActivity(intent)
            }

            txtMsjResultado.visibility = View.VISIBLE
            lottieResult.visibility = View.VISIBLE
            txtMsjRespuestaCorrecta.visibility = View.VISIBLE

           btnReinicio.visibility = Button.VISIBLE

            flexResponse.visibility = View.GONE
            txtCantIntentos.visibility = View.GONE
            flexAlfabeto.visibility = View.GONE
            txtMsjIntentos.visibility = View.GONE
            txtPregunta.visibility = View.GONE
            lotieAnimThinking.visibility = View.GONE
        }
    }

   fun reproduceSoundPool() {
        sp?.play(click, 1f, 1f, 1, 0, 1f)
    }

    fun sonidoVictoria() {
        sp?.play(victoria, 1f, 1f, 1, 0, 1f)
    }

    fun sonidoDerrota() {
        sp?.play(derrota, 1f, 1f, 1, 0, 1f)
    }
}

