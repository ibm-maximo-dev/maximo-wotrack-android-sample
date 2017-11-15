package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.ibm.maximo.oslc.MaximoConnector
import com.ibm.maximo.oslc.Options

/**
 * Created by sls on 11/9/17.
 */
class MaximoAPI {
    companion object {
        @JvmField
        val INSTANCE = MaximoAPI()
    }

    lateinit var options: Options
    lateinit var connector: MaximoConnector

    // note: this is a UI thread handler
    private val uiHandler = Handler(Looper.getMainLooper())

    fun login(userName: String, password: String, host: String, port: Int, onOk: ()->Unit, onError: (t: Throwable)->Unit) {
        AsyncTask.execute({
            try {
                options = Options().user(userName).password(password).auth("maxauth")
                options = options.host(host).port(port).lean(true)
                connector = MaximoConnector(options).debug(true)
                connector.connect()
                uiHandler.post({onOk()})
            } catch (t: Throwable) {
                uiHandler.post({onError(t)})
            }
        })
    }
}