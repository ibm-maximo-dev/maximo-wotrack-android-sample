package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.ibm.maximo.oslc.MaximoConnector
import com.ibm.maximo.oslc.Options
import javax.json.JsonObject

/**
 * Created by sls on 11/9/17.
 */
class MaximoAPI {
    companion object {
        @JvmField
        val INSTANCE = MaximoAPI()
        const val PAGE_SIZE = 5
    }

    lateinit var options: Options
    lateinit var connector: MaximoConnector
    lateinit var loggedUser: JsonObject

    // note: this is a UI thread handler
    private val uiHandler = Handler(Looper.getMainLooper())

    fun login(userName: String, password: String, host: String, port: Int, onOk: ()->Unit, onError: (t: Throwable)->Unit) {
        AsyncTask.execute({
            try {
                options = Options().user(userName).password(password).auth("maxauth")
                options = options.host(host).port(port).lean(true)
                connector = MaximoConnector(options).debug(true)
                connector.connect()

                val personSet = connector.resourceSet("mxperson")
                personSet.where("spi:personid=\"" + userName.toUpperCase() + "\"")
                personSet.fetch()
                val person = personSet.member(0)
                loggedUser = person.toJSON()

                uiHandler.post({onOk()})
            } catch (t: Throwable) {
                uiHandler.post({onError(t)})
            }
        })
    }

    fun listWorkOrders(onOk: (workOrderSet: WorkOrderViewModel.WorkOrderSet)->Unit, onError: (t: Throwable)->Unit) {
        AsyncTask.execute({
            try {
                val workOrderSet = connector.resourceSet("mxwo")
                val resultList = mutableListOf<JsonObject>()
                workOrderSet.pageSize(PAGE_SIZE)
                workOrderSet.where("spi:istask=0")
                workOrderSet.paging(true)
                workOrderSet.fetch()
                var totalCount = workOrderSet.totalCount()
                var i = 0
                while (i.toInt() < PAGE_SIZE) {
                    val resource = workOrderSet.member(i)
                    i = i.inc()
                    resultList.add(resource.toJSON())
                }

                var result = WorkOrderViewModel.WorkOrderSet(workOrderSet, totalCount, PAGE_SIZE, resultList)
                uiHandler.post({onOk(result)})
            } catch (t: Throwable) {
                uiHandler.post({onError(t)})
            }
        })
    }

    fun listWorkOrderStatuses(onOk: (statusSet: List<JsonObject>)->Unit, onError: (t: Throwable)->Unit) {
        AsyncTask.execute({
            try {
                val statusSet = connector.resourceSet("mxdomain")
                val resultList = mutableListOf<JsonObject>()
                statusSet.where("spi:domainid=\"WOSTATUS\"")
                statusSet.fetch()
                val woStatusDomain = statusSet.member(0)
                var woStatusJSON = woStatusDomain.toJSON()
                var values = woStatusJSON.getJsonArray("synonymdomain")
                var i = 0
                while (i.toInt() < values.size) {
                    val domainValue = values[i]
                    i = i.inc()
                    resultList.add(domainValue as JsonObject)
                }

                uiHandler.post({onOk(resultList)})
            } catch (t: Throwable) {
                uiHandler.post({onError(t)})
            }
        })
    }

    fun updatetWorkOrder(workOrder: JsonObject, onOk: ()->Unit, onError: (t: Throwable)->Unit) {
        AsyncTask.execute({
            try {
                var uri = connector.currentURI + "/os/mxwo/" + workOrder.getJsonNumber ("workorderid")
                connector.update(uri, workOrder)
                uiHandler.post({onOk()})
            } catch (t: Throwable) {
                uiHandler.post({onError(t)})
            }
        })
    }

    fun createWorkOrder(workOrder: JsonObject, onOk: ()->Unit, onError: (t: Throwable)->Unit) {
        AsyncTask.execute({
            try {
                var uri = connector.currentURI + "/os/mxwo"
                connector.create(uri, workOrder)
                uiHandler.post({onOk()})
            } catch (t: Throwable) {
                uiHandler.post({onError(t)})
            }
        })
    }
}