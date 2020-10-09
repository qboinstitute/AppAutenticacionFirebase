package qbo.com.appautenticacionfirebase

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.math.BigInteger
import java.security.SecureRandom

class MainActivity : AppCompatActivity() {

    private val callbackManager = CallbackManager.Factory.create()
    private val aleatorio: SecureRandom = SecureRandom()
    private val URL_CALLBACK = "qbogit://git.oauth2token"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        validarPreferencia()
        pbautenticacion.visibility = View.GONE
        //Autenticación Email y password.
        btnloginfirebase.setOnClickListener { vista ->
            if(etemail.text?.isNotEmpty()!! &&
                etpassword.text?.isNotEmpty()!!){
                pbautenticacion.visibility = View.VISIBLE
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    etemail.text.toString(), etpassword.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful){
                        guardarPreferenciaIrAlApp(
                            it.result?.user?.email.toString(),
                            TipoAutenticacion.FIREBASE.name, "",
                            ""
                        )
                    }else{
                        enviarMensaje(vista, "Error en la autenticación.")
                    }
                }
            }
        }
        //Autenticación Google
        btnlogingoogle.setOnClickListener {
            pbautenticacion.visibility = View.VISIBLE
            val configlogin = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val cliente : GoogleSignInClient = GoogleSignIn.getClient(
                this, configlogin
            )
            startActivityForResult(cliente.signInIntent, 777)
        }
        //Autenticación Facebook
        btnloginfacebook.setOnClickListener {
            pbautenticacion.visibility = View.GONE
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email")
            )
            LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    result?.let {
                        val token = it.accessToken
                        val credencial : AuthCredential = FacebookAuthProvider.getCredential(
                            token.token)
                        FirebaseAuth.getInstance().signInWithCredential(credencial)
                            .addOnCompleteListener {res->
                                if(res.isSuccessful){
                                    guardarPreferenciaIrAlApp(
                                        res.result?.user?.email.toString(),
                                        TipoAutenticacion.FACEBOOK.name,
                                        res.result?.user?.displayName.toString(),
                                        res.result?.user?.photoUrl.toString()
                                    )
                                }
                            }
                    }
                }

                override fun onCancel() {
                    enviarMensaje(obtenerVista(), "Canceló la autenticación con Facebook")
                }

                override fun onError(error: FacebookException?) {
                    enviarMensaje(obtenerVista(), "Error en la autenticación con Facebook")
                }

            })
        }
        btnlogingit.setOnClickListener {
            pbautenticacion.visibility = View.VISIBLE
            val httpurl = HttpUrl.Builder()
                .scheme("https")
                .host("github.com")
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", "21aec9b4c931ff0dd271")
                .addQueryParameter("redirect_uri", URL_CALLBACK)
                .addQueryParameter("state", obtenerValorAleatorio())
                .addQueryParameter("scope", "user:email")
                .build()
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse(httpurl.toString()))
            startActivity(intent)
        }
        val uri = intent.data
        if(uri != null && uri.toString().startsWith(URL_CALLBACK)){
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            if(code != null && state != null){
                obtenerCredencial(code, state)
            }
        }


    }

    private fun obtenerCredencial(code: String, state: String) {
        val okHttpClient = OkHttpClient()
        val form = FormBody.Builder()
            .add("client_id", "21aec9b4c931ff0dd271")
            .add("client_secret", "25b067debd465c9d7ea251ce122317a9b559dd7c")
            .add("code", code)
            .add("redirect_uri", URL_CALLBACK)
            .add("state", state)
            .build()
        val request = Request.Builder()
            .url("https://github.com/login/oauth/access_token")
            .post(form)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                enviarMensaje(obtenerVista(), "Error en la autenticación GIT")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody : String = response.body!!.string()
                Log.i("TAGTOKEN", responseBody)
                val valorsplit = responseBody.split("=|&".toRegex()).toTypedArray()
                if(valorsplit[0].equals("access_token", ignoreCase = true)){
                    loginGithubConToken(valorsplit[1])
                }else{
                    enviarMensaje(obtenerVista(), "Error en la autenticación GIT-TOKEN")
                }
            }

        })

    }

    private fun loginGithubConToken(token :String){
        val credencial = GithubAuthProvider.getCredential(token)
        FirebaseAuth.getInstance().signInWithCredential(credencial)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    guardarPreferenciaIrAlApp(
                        it.result?.additionalUserInfo?.username.toString(),
                        TipoAutenticacion.GITHUB.name,
                        it.result?.user?.displayName.toString(),
                        it.result?.user?.photoUrl.toString()
                    )
                }
            }
    }

    private fun obtenerValorAleatorio(): String {
        return BigInteger(130, aleatorio).toString(32)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 777){
            val task : Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val cuenta : GoogleSignInAccount? =
                    task.getResult(ApiException::class.java)
                if(cuenta != null){
                    val credencial : AuthCredential = GoogleAuthProvider
                        .getCredential(cuenta.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credencial)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                guardarPreferenciaIrAlApp(
                                    cuenta.email.toString(),
                                    TipoAutenticacion.GOOGLE.name,
                                    cuenta.displayName.toString(),
                                    cuenta.photoUrl.toString()
                                )
                            }else{
                                enviarMensaje(obtenerVista(), "Error en la autenticación GMAIL")
                            }
                        }

                }
            }catch (e : ApiException){
                enviarMensaje(obtenerVista(), "Error en la autenticación GMAIL")
            }
        }
    }


    private fun obtenerVista(): View{
        return findViewById(android.R.id.content)
    }

    private fun guardarPreferenciaIrAlApp(email :String, tipo : String,
                                          nombre:String, urlimagen : String){
        val preferencia : SharedPreferences.Editor =
            getSharedPreferences("appFirebaseQBO", Context.MODE_PRIVATE).edit()
        preferencia.putString("email", email)
        preferencia.putString("tipo", tipo)
        preferencia.putString("nombre", nombre)
        preferencia.putString("urlimg", urlimagen)
        preferencia.apply()
        pbautenticacion.visibility = View.GONE
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun validarPreferencia(){
        val preferencia : SharedPreferences = getSharedPreferences("appFirebaseQBO",
            Context.MODE_PRIVATE)
        val email: String? = preferencia.getString("email", null)
        val tipo: String? = preferencia.getString("tipo", null)
        val nombre: String? = preferencia.getString("nombre", null)
        val urlimagen: String? = preferencia.getString("urlimg", null)
        if(email != null && tipo != null && nombre != null && urlimagen != null){
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun enviarMensaje(vista: View, mensaje: String){
        pbautenticacion.visibility = View.GONE
        Snackbar.make(vista, mensaje, Snackbar.LENGTH_LONG).show()
    }


}