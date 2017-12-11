package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_status.*
import javax.json.Json
import javax.json.JsonObject

/**
 * Created by silvinoneto on 11/12/2017.
 */
class WorkOrderStatusActivity : Activity() {

    companion object {
        lateinit var mWorkOrderStatusList: List<JsonObject>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)
        var currentStatus = intent.getStringExtra("CurrentStatus")

        loadStatusList({ statusList ->
            mWorkOrderStatusList = statusList
            var stringList = mutableListOf<String>()
            var i = 0
            var selectedPosition = 0
            while (i.toInt() < statusList.size) {
                val domainValue = statusList[i]
                stringList.add(domainValue.getString("description"))
                if (currentStatus.equals(domainValue.getString("maxvalue")))
                    selectedPosition = i
                i = i.inc()
            }

            var adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stringList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            status_list.adapter = adapter
            status_list.setSelection(selectedPosition)
        }, { t ->
            Toast.makeText(this, t.message, Toast.LENGTH_SHORT).show()
        })

        change_status_button.setOnClickListener(View.OnClickListener() {
            updateStatusWorkOrder()
        })
    }

    /**
     * Method for loading data in listview
     * @param number
     */
    private fun loadStatusList(onOk: (statusList: List<JsonObject>) -> Unit, onError: (t: Throwable) -> Unit) {
        MaximoAPI.INSTANCE.listWorkOrderStatuses(onOk, onError)
    }

    private fun updateStatusWorkOrder() {
        var updatedJsonObject = buildWorkOrder()
        MaximoAPI.INSTANCE.updatetWorkOrder(updatedJsonObject, {
            WorkOrderViewModel.INSTANCE.mWorkOrder = updatedJsonObject
            var toast = Toast.makeText(this, "Work Order Status successfully updated.", Toast.LENGTH_LONG)
            toast.show()
            finish()
        }, { t ->
            Log.d("APP", "Error", t)
            var toast = Toast.makeText(this, t.message, Toast.LENGTH_LONG)
            toast.show()
        })
    }

    private fun buildWorkOrder(): JsonObject {
        var objectBuilder = Json.createObjectBuilder()
        var mWorkOrder = WorkOrderViewModel.INSTANCE.mWorkOrder
        if (mWorkOrder != null) {
            for (entry in mWorkOrder) {
                objectBuilder.add(entry.key, entry.value)
            }
        }

        var position = status_list.selectedItemPosition

        objectBuilder.add("status_description", mWorkOrderStatusList[position].getString("description"))
        objectBuilder.add("status", mWorkOrderStatusList[position].getString("maxvalue"))

        return objectBuilder.build()
    }
}