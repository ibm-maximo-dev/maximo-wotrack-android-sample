package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import javax.json.JsonObject

/**
 * Created by silvinoneto on 02/01/2018.
 */
class WorkOrderList {

    companion object {
        lateinit var mWorkOrderSet: WorkOrderViewModel.WorkOrderSet
    }

    enum class Operation {PREVIOUS, NEXT}

    constructor(context: Context, listView: ListView, add_button: FloatingActionButton) {
        listView.adapter = WorkOrderCustomAdapter(context, mWorkOrderSet)
        listView.onItemClickListener = WorkOrderItemSelectedListener(context)
        var scrollListener = WorkOrderScrollerListener(5, context, listView)
        listView.setOnScrollListener (scrollListener)

        add_button.setOnClickListener(View.OnClickListener() {
            var workOrderFormIntent = Intent(context, WorkOrderFormActivity::class.java)
            ContextCompat.startActivity(context, workOrderFormIntent, null)
        })
    }

    private class WorkOrderItemSelectedListener(context : Context) : AdapterView.OnItemClickListener {
        private val mContext: Context

        init {
            mContext = context
        }

        override fun onItemClick(parent : AdapterView<*>, view: View, position : Int, id : Long) {
            var workOrder = mWorkOrderSet.workOrderList.get(position)
            WorkOrderViewModel.INSTANCE.mWorkOrder = workOrder
            var workOrderFormIntent = Intent(mContext, WorkOrderFormActivity::class.java)
            ContextCompat.startActivity(mContext, workOrderFormIntent, null)
        }
    }

    private class WorkOrderScrollerListener(visibleThreshold: Int,
                                            context: Context, listView: ListView) : EndlessScrollListener(visibleThreshold) {
        private val mContext: Context
        private val mListView : ListView
        private val uiHandler = Handler(Looper.getMainLooper())

        init {
            mContext = context
            mListView = listView
        }

        /**
         * Method for loading data in listview
         * @param number
         */
        private fun loadPage(operation: Operation, onOk: ()->Unit, onError: (t: Throwable)->Unit) {
            AsyncTask.execute({
                try {
                    val resultList = mutableListOf<JsonObject>()
                    resultList.addAll(mWorkOrderSet.workOrderList)

                    if (operation == Operation.PREVIOUS)
                        mWorkOrderSet.resourceSet.previousPage()
                    else
                        mWorkOrderSet.resourceSet.nextPage()
                    var i = 0
                    while (i.toInt() < MaximoAPI.PAGE_SIZE) {
                        val resource = mWorkOrderSet.resourceSet.member(i)
                        i = i.inc()
                        resultList.add(resource.toJSON())
                    }
                    mWorkOrderSet = WorkOrderViewModel.WorkOrderSet(mWorkOrderSet.resourceSet,
                            mWorkOrderSet.totalCount, mWorkOrderSet.pageSize, resultList)

                    uiHandler.post({onOk()})
                } catch (t: Throwable) {
                    uiHandler.post({onError(t)})
                }
            })
        }

        override fun onLoadMore(page: Int, totalItemsCount: Int): Boolean {
            loadPage(Operation.NEXT, {
                (mListView.adapter as WorkOrderCustomAdapter).updateWorkOrderSet(mWorkOrderSet)

            }, { t ->
                Toast.makeText(mContext, t.message, Toast.LENGTH_SHORT).show()
            })

            return true
        }
    }

    private class WorkOrderCustomAdapter(context : Context, workOrderSet: WorkOrderViewModel.WorkOrderSet) : BaseAdapter() {

        private val mContext: Context
        private var mWorkOrderSet: WorkOrderViewModel.WorkOrderSet

        init {
            mContext = context
            mWorkOrderSet = workOrderSet
        }

        public fun updateWorkOrderSet(workOrderSet: WorkOrderViewModel.WorkOrderSet) {
            mWorkOrderSet = workOrderSet
            notifyDataSetChanged()
        }

        // Fetch the row count for the list
        override fun getCount(): Int {
            return mWorkOrderSet.workOrderList.count()
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return ""
        }

        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val layoutInflator = LayoutInflater.from(mContext)
            val listRow = layoutInflator.inflate(R.layout.list_row, viewGroup, false)

            //val workOrder = mWorkOrderList.get(position)
            val workOrder = mWorkOrderSet.workOrderList.get(position)
            val description = listRow.findViewById<TextView>(R.id.description)
            val workOrderNumber = listRow.findViewById<TextView>(R.id.wonum)

            description.text = workOrder.getString("description")
            workOrderNumber.text = "Work Order: " + workOrder.getString("wonum")

            return listRow
        }
    }
}