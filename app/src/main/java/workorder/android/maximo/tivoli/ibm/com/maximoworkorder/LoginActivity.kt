package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = this.getSharedPreferences("login_prefs", 0)

        setContentView(R.layout.activity_login)
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        val host = prefs!!.getString("hostport", null)
        val user = prefs!!.getString("username", null)

        if (host!=null) hostport.setText(host)
        if (user!=null) username.setText(user)

        sign_in_button.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        // Reset errors.
        hostport.error = null
        username.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val hostportStr = hostport.text.toString()
        val userStr = username.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null


        // Check for a valid host port
        if (TextUtils.isEmpty(hostportStr)) {
            password.error = getString(R.string.error_host_port)
            focusView = hostport
            cancel = true
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(userStr)) {
            username.error = getString(R.string.error_field_required)
            focusView = username
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)

            val portIdx = hostportStr.indexOf(':')
            var host = hostportStr.substring(0, portIdx)
            var port = Integer.parseInt(hostportStr.substring(portIdx+1))
            Log.d("APP", "Connecting to $host:$port")
            MaximoAPI.INSTANCE.login(userStr, passwordStr, host, port, {
                showProgress(false)
                Log.d("APP", "Logged In")

                // store host and user
                prefs!!.edit().putString("hostport", hostportStr).putString("username", userStr).apply()

                val intent = Intent(this@LoginActivity.baseContext, MainActivity::class.java)
                var person = MaximoAPI.INSTANCE.loggedUser
                intent.putExtra("PersonName", person.getString("displayname"))
                intent.putExtra("PersonEmail", person.getString("primaryemail"))
                startActivity(intent)
            }, { t ->
                Log.d("APP", "Error", t)
                showProgress(false)
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            })
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}
