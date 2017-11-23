package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import com.ibm.maximo.oslc.ResourceSet
import javax.json.JsonObject

/**
 * Created by silvinoneto on 21/11/2017.
 */
public class WorkOrderViewModel {

    companion object {
        @JvmField
        val INSTANCE = WorkOrderViewModel()
    }

    var mWorkOrder: JsonObject? = null

    data class WorkOrderSet(val resourceSet: ResourceSet, val totalCount: Int,
                            val pageSize: Int, val workOrderList: List<JsonObject>)

    private val mWorkOrderRepository = WorkOrderRepository(MaximoAPI.INSTANCE)

    fun getWorkOrders(onOk: (workOrderList: WorkOrderSet)->Unit, onError: (t: Throwable)->Unit) {
        //Prepare the data for your UI, the users list and maybe some additional data needed as well
        return mWorkOrderRepository.getWorkOrders(onOk, onError)
    }

    class WorkOrderRepository(val maximoAPI: MaximoAPI) {
        private lateinit var mWorkOrderSet : WorkOrderSet

        fun getWorkOrders(onOk: (workOrderSet: WorkOrderSet)->Unit, onError: (t: Throwable)->Unit) {
            var onGetWorkOrders = fun(workOrderSet: WorkOrderSet) {
                mWorkOrderSet = workOrderSet
                onOk(workOrderSet)
            }
            maximoAPI.listWorkOrders(onGetWorkOrders, onError)
        }
    }
}
