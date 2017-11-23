package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import javax.json.JsonObject

/**
 * Created by silvinoneto on 21/11/2017.
 */
public class WorkOrderViewModel(workOrderList: List<JsonObject>) {

    private val mWorkOrderList: List<JsonObject>

    init {
        mWorkOrderList = workOrderList
    }

    private class WorkOrderRepository(val userApi: UserApi) {

        fun getUsers(): Observable<List<User>> {
            return userApi.getUsers()
        }
    }
}
