package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import java.io.Serializable
import javax.json.JsonObject

class WorkOrderListActivity : AppCompatActivity() {

    companion object {
        lateinit var mWorkOrderList: List<JsonObject>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        //mWorkOrderList = intent.getSerializableExtra("WorkOrderList") as List<JsonObject>

        val listView = findViewById<ListView>(R.id.workorder_list)
        listView.adapter = WorkOrderCustomAdapter(this, mWorkOrderList)
        listView.onItemSelectedListener = WorkOrderItemSelectedListener(this)
    }

    private class WorkOrderItemSelectedListener(context : Context) : AdapterView.OnItemSelectedListener {
        private val mContext: Context

        init {
            mContext = context
        }

        override fun onItemSelected(parent : AdapterView<*>, view: View, position : Int, id : Long) {
            var workOrder = mWorkOrderList.get(position)
            var workOrderFormIntent = Intent(mContext, WorkOrderListActivity::class.java)
            workOrderFormIntent.putExtra("SelectedWorkOrder", workOrder as Serializable)
            startActivity(mContext, workOrderFormIntent, null)
        }

        override fun onNothingSelected(parent : AdapterView<*>) {

        }
    }

    private class WorkOrderCustomAdapter(context : Context, workOrderList: List<JsonObject>) : BaseAdapter() {

        private val mContext: Context
        private val mWorkOrderList: List<JsonObject>

        init {
            mContext = context
            mWorkOrderList = workOrderList
        }

        // Fetch the row count for the list
        override fun getCount(): Int {
            return mWorkOrderList.size
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

            val workOrder = mWorkOrderList.get(position)
            val description = listRow.findViewById<TextView>(R.id.description)
            val workOrderNumber = listRow.findViewById<TextView>(R.id.wonum)

            description.text = workOrder.getString("description")
            workOrderNumber.text = "Work Order Number: " + workOrder.getString("wonum")

            return listRow
        }
    }

}
