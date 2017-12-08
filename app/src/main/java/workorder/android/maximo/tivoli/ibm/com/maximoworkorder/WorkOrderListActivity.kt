package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_list.*
import javax.json.JsonObject


class WorkOrderListActivity : AppCompatActivity() {

    companion object {
        lateinit var mWorkOrderSet: WorkOrderViewModel.WorkOrderSet
    }

    enum class Operation {PREVIOUS, NEXT}

    private var increment = 0
    private var pageCount = 0

    private val uiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val listView = findViewById<ListView>(R.id.workorder_list)
        listView.adapter = WorkOrderCustomAdapter(this, mWorkOrderSet)
        listView.onItemClickListener = WorkOrderItemSelectedListener(this)

        var pageMod = mWorkOrderSet.totalCount % mWorkOrderSet.pageSize
        pageMod = if (pageMod == 0) 0 else 1
        pageCount = mWorkOrderSet.totalCount / mWorkOrderSet.pageSize + pageMod

        pageTitle.setText("Page " + (increment + 1) + " of " + pageCount);

        next.setOnClickListener(View.OnClickListener() {
            increment = increment.inc()
            loadPage(Operation.NEXT, {
                pageTitle.setText("Page " + (increment + 1) + " of " + pageCount)
                listView.adapter = WorkOrderCustomAdapter(this, mWorkOrderSet)
            }, { t ->
                Toast.makeText(this, t.message, Toast.LENGTH_SHORT).show()
            })
            checkPaginationButtonsEnabled()
        })

        prev.setEnabled(false);
        prev.setOnClickListener(View.OnClickListener() {
            increment = increment.dec()
            loadPage(Operation.PREVIOUS, {
                pageTitle.setText("Page " + (increment + 1) + " of " + pageCount)
                listView.adapter = WorkOrderCustomAdapter(this, mWorkOrderSet)
            }, { t ->
                Toast.makeText(this, t.message, Toast.LENGTH_SHORT).show()
            })
            checkPaginationButtonsEnabled()
        })

        add_button.setOnClickListener(View.OnClickListener() {
            var workOrderFormIntent = Intent(this, WorkOrderFormActivity::class.java)
            startActivity(this, workOrderFormIntent, null)
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@WorkOrderListActivity.baseContext, MainActivity::class.java)
        var person = MaximoAPI.INSTANCE.loggedUser
        intent.putExtra("PersonName", person.getString("displayname"))
        intent.putExtra("PersonEmail", person.getString("primaryemail"))
        startActivity(intent)
    }

    /**
     * Method for enabling and disabling pagination buttons
     */
    private fun checkPaginationButtonsEnabled() {
        if (increment + 1 == pageCount) {
            next.setEnabled(false)
        } else if (increment == 0) {
            prev.setEnabled(false)
        } else {
            prev.setEnabled(true)
            next.setEnabled(true)
        }
    }

    /**
     * Method for loading data in listview
     * @param number
     */
    private fun loadPage(operation: Operation, onOk: ()->Unit, onError: (t: Throwable)->Unit) {
        AsyncTask.execute({
            try {
                val resultList = mutableListOf<JsonObject>()

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

    private class WorkOrderItemSelectedListener(context : Context) : AdapterView.OnItemClickListener {
        private val mContext: Context

        init {
            mContext = context
        }

        override fun onItemClick(parent : AdapterView<*>, view: View, position : Int, id : Long) {
            var workOrder = mWorkOrderSet.workOrderList.get(position)
            WorkOrderViewModel.INSTANCE.mWorkOrder = workOrder
            var workOrderFormIntent = Intent(mContext, WorkOrderFormActivity::class.java)
            startActivity(mContext, workOrderFormIntent, null)
        }
    }

    private class WorkOrderCustomAdapter(context : Context, workOrderSet: WorkOrderViewModel.WorkOrderSet) : BaseAdapter() {

        private val mContext: Context
        private val mWorkOrderSet: WorkOrderViewModel.WorkOrderSet

        init {
            mContext = context
            mWorkOrderSet = workOrderSet
        }

        // Fetch the row count for the list
        override fun getCount(): Int {
            return mWorkOrderSet.pageSize
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
