package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var workOrderListDisplayed = false
    lateinit var workOrderListIntent : Intent
    lateinit var personName : String
    lateinit var personEmail : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        /*
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        */

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        personName = intent.getStringExtra("PersonName")
        personEmail = intent.getStringExtra("PersonEmail")

        val header = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
        var personNameField = header.findViewById<TextView>(R.id.personname);
        var personEmailField = header.findViewById<TextView>(R.id.personemail);
        personNameField.setText(personName)
        personEmailField.setText(personEmail)

        loadMainWorkOrders()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        Log.d("APP", "Item selected")
        when (item.itemId) {
            /*
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
            */
            R.id.nav_workorders -> {
                Log.d("APP", "Loading Work Orders list")
                loadWorkOrders()
            }
            R.id.nav_logout -> {
                Log.d("APP", "Logging out")
                logout()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadMainWorkOrders() {
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true)

        MaximoAPI.INSTANCE.listWorkOrders({ workOrderSet ->
            Log.d("APP", "Data retrieved successfully")
            var listView = findViewById<ListView>(R.id.workorder_list_main)
            var addButton = findViewById<FloatingActionButton>(R.id.add_button_main)
            WorkOrderList.mWorkOrderSet = workOrderSet
            WorkOrderList(this, listView, addButton)
            showProgress(false)
        }, { t ->
            Log.d("APP", "Error", t)
            showProgress(false)
            Toast.makeText(this, t.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun loadWorkOrders() {
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true)

        if (workOrderListDisplayed) {
            startActivity(workOrderListIntent)
            return
        }

        MaximoAPI.INSTANCE.listWorkOrders({ workOrderSet ->
            showProgress(false)
            Log.d("APP", "Data retrieved successfully")
            workOrderListIntent = Intent(this@MainActivity.baseContext, WorkOrderListActivity::class.java)
            WorkOrderListActivity.mWorkOrderSet = workOrderSet
            workOrderListDisplayed = true
            startActivity(workOrderListIntent)
        }, { t ->
            Log.d("APP", "Error", t)
            showProgress(false)
            Toast.makeText(this, t.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun logout() {
        workOrderListDisplayed = false
        var loginActivity = Intent(this@MainActivity.baseContext, LoginActivity::class.java)
        startActivity(loginActivity)
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

            list_progress.visibility = if (show) View.VISIBLE else View.GONE
            list_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            list_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            list_progress.visibility = if (show) View.VISIBLE else View.GONE
        }
    }
}
