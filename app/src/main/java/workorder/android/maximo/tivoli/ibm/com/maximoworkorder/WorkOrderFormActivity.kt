package workorder.android.maximo.tivoli.ibm.com.maximoworkorder

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import kotlinx.android.synthetic.main.content_workorder_form.*
import kotlinx.android.synthetic.main.workorder_form_bar.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.json.Json
import javax.json.JsonObject


class WorkOrderFormActivity : AppCompatActivity() {

    companion object {
        const val SCHEDULE_START_DATE_DIALOG_ID = 0
        const val SCHEDULE_FINISH_DATE_DIALOG_ID = 1
        const val SCHEDULE_START_TIME_DIALOG_ID = 2
        const val SCHEDULE_FINISH_TIME_DIALOG_ID = 3
    }

    data class Date(val day: Int, val month: Int, val year: Int)
    data class Time(val hours: Int, val minutes: Int)

    lateinit var scheduleStartDate : Date
    lateinit var scheduleFinishDate : Date

    lateinit var scheduleStartTime : Time
    lateinit var scheduleFinishTime : Time

    var selectedDatePickerID : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workorder_form)
        setSupportActionBar(workorder_toolbar)

        var mWorkOrder = WorkOrderViewModel.INSTANCE.mWorkOrder

        var calendar = Calendar.getInstance()
        if (mWorkOrder != null) {
            wonum.setEnabled(false)
            wonum.setText(mWorkOrder.getString("wonum"))
            description.setText(mWorkOrder.getString("description"))
            duration.setText(mWorkOrder.getJsonNumber("estdur").toString())
            if (mWorkOrder.containsKey("schedstart")) {
                setScheduleDates(calendar, mWorkOrder.getString("schedstart"), {scheduleDate, scheduleTime ->
                    scheduleStartDate = scheduleDate
                    scheduleStartTime = scheduleTime
                }, schedule_start_date, schedule_start_time)
            } else {
                scheduleStartDate = Date(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.YEAR))
                scheduleStartTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            }

            if (mWorkOrder.containsKey("schedfinish")) {
                setScheduleDates(calendar, mWorkOrder.getString("schedfinish"), {scheduleDate, scheduleTime ->
                    scheduleFinishDate = scheduleDate
                    scheduleFinishTime = scheduleTime
                }, schedule_finish_date, schedule_finish_time)
            } else {
                scheduleFinishDate = Date(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.YEAR))
                scheduleFinishTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            }

            save_button.setOnClickListener(View.OnClickListener() {
                updateWorkOrder()
            })
        }
        else {
            scheduleStartDate = Date(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.YEAR))
            scheduleStartTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            scheduleFinishDate = Date(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.YEAR))
            scheduleFinishTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

            wonum.setEnabled(true)
            wonum.text.clear()
            description.text.clear()
            schedule_start_date.text.clear()
            schedule_start_time.text.clear()
            schedule_finish_date.text.clear()
            schedule_finish_time.text.clear()
            duration.text.clear()

            save_button.setOnClickListener(View.OnClickListener() {
                createWorkOrder()
            })
        }

        schedule_start_date.setOnClickListener(View.OnClickListener() {
             @Suppress("DEPRECATION")
             showDialog(SCHEDULE_START_DATE_DIALOG_ID)
        })

        schedule_finish_date.setOnClickListener(View.OnClickListener() {
            @Suppress("DEPRECATION")
            showDialog(SCHEDULE_FINISH_DATE_DIALOG_ID)
        })

        schedule_start_time.setOnClickListener(View.OnClickListener() {
            @Suppress("DEPRECATION")
            showDialog(SCHEDULE_START_TIME_DIALOG_ID)
        })

        schedule_finish_time.setOnClickListener(View.OnClickListener() {
            @Suppress("DEPRECATION")
            showDialog(SCHEDULE_FINISH_TIME_DIALOG_ID)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_change_status -> {
                var statusListIntent = Intent(this@WorkOrderFormActivity.baseContext, WorkOrderStatusActivity::class.java)
                var currentStatus = WorkOrderViewModel.INSTANCE.mWorkOrder?.getString("status")
                statusListIntent.putExtra("CurrentStatus", currentStatus)
                startActivity(statusListIntent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.status, menu)
        return true
    }

    override fun onBackPressed() {
        WorkOrderViewModel.INSTANCE.mWorkOrder = null
        super.onBackPressed()
    }

    private fun updateWorkOrder() {
        var updatedJsonObject = buildWorkOrder()
        MaximoAPI.INSTANCE.updatetWorkOrder(updatedJsonObject, {
            var toast = Toast.makeText(this, "Work Order successfully updated.", Toast.LENGTH_LONG)
            toast.show()
        }, { t ->
            Log.d("APP", "Error", t)
            var toast = Toast.makeText(this, t.message, Toast.LENGTH_LONG)
            toast.show()
        })
    }

    private fun createWorkOrder() {
        var newJsonObject = buildWorkOrder()
        MaximoAPI.INSTANCE.createWorkOrder(newJsonObject, {
            var toast = Toast.makeText(this, "Work Order successfully created.", Toast.LENGTH_LONG)
            toast.show()
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
        else {
            objectBuilder.add("wonum", wonum.text.toString())
            objectBuilder.add("siteid", MaximoAPI.INSTANCE.loggedUser.getString("locationsite"))
            objectBuilder.add("orgid", MaximoAPI.INSTANCE.loggedUser.getString("locationorg"))
        }

        objectBuilder.add("description", description.text.toString())

        // toDouble will cause NPE on empty string
        if (!duration.text.isBlank())
            objectBuilder.add("estdur", duration.text.toString().toDouble())

        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        if (!TextUtils.isEmpty(schedule_start_date.text.toString())) {
            var schedStartDateTime = Calendar.getInstance()
            schedStartDateTime.set(Calendar.YEAR, scheduleStartDate.year)
            schedStartDateTime.set(Calendar.MONTH, scheduleStartDate.month)
            schedStartDateTime.set(Calendar.DATE, scheduleStartDate.day)

            schedStartDateTime.set(Calendar.HOUR_OF_DAY, 0)
            schedStartDateTime.set(Calendar.MINUTE, 0)
            schedStartDateTime.set(Calendar.SECOND, 0)
            schedStartDateTime.set(Calendar.MILLISECOND, 0)

            if (!TextUtils.isEmpty(schedule_start_time.text.toString())) {
                schedStartDateTime.set(Calendar.HOUR_OF_DAY, scheduleStartTime.hours)
                schedStartDateTime.set(Calendar.MINUTE, scheduleStartTime.minutes)
            }

            objectBuilder.add("schedstart", simpleDateFormat.format(schedStartDateTime.time))
        }

        if (!TextUtils.isEmpty(schedule_finish_date.text.toString())) {
            var schedFinishDateTime = Calendar.getInstance()
            schedFinishDateTime.set(Calendar.YEAR, scheduleFinishDate.year)
            schedFinishDateTime.set(Calendar.MONTH, scheduleFinishDate.month)
            schedFinishDateTime.set(Calendar.DATE, scheduleFinishDate.day)

            schedFinishDateTime.set(Calendar.HOUR_OF_DAY, 0)
            schedFinishDateTime.set(Calendar.MINUTE, 0)
            schedFinishDateTime.set(Calendar.SECOND, 0)
            schedFinishDateTime.set(Calendar.MILLISECOND, 0)

            if (!TextUtils.isEmpty(schedule_start_time.text.toString())) {
                schedFinishDateTime.set(Calendar.HOUR_OF_DAY, scheduleFinishTime.hours)
                schedFinishDateTime.set(Calendar.MINUTE, scheduleFinishTime.minutes)
            }

            objectBuilder.add("schedfinish", simpleDateFormat.format(schedFinishDateTime.time))
        }
        return objectBuilder.build()
    }

    private fun setScheduleDates(calendar: Calendar, strScheduleDate: String,
                                 onSuccess: (scheduleDate: Date, scheduleTime: Time)->Unit,
                                 dateTextView: TextView, timeTextView: TextView) {
        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        var decimalFormatter = DecimalFormat("00")

        var date = simpleDateFormat.parse(strScheduleDate)
        calendar.time = date

        var scheduleDate = Date(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR))
        var scheduleTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        var strHour = decimalFormatter.format(scheduleTime.hours)
        var strMinute = decimalFormatter.format(scheduleTime.minutes)

        dateTextView.setText("" + (scheduleDate.month + 1) + "/" + scheduleDate.day + "/" + scheduleDate.year)
        timeTextView.setText("" + strHour + ":" + strMinute)

        onSuccess(scheduleDate, scheduleTime)
    }

    override fun onCreateDialog(id: Int): Dialog {
        var datePicker = false
        var date : Date?
        var time : Time?
        date = null
        time = null
        if (id == SCHEDULE_START_DATE_DIALOG_ID) {
            date = scheduleStartDate
            selectedDatePickerID = R.id.schedule_start_date
            datePicker = true
        }
        else if (id == SCHEDULE_FINISH_DATE_DIALOG_ID) {
            date = scheduleFinishDate
            selectedDatePickerID = R.id.schedule_finish_date
            datePicker = true
        }
        else if (id == SCHEDULE_START_TIME_DIALOG_ID) {
            time = scheduleStartTime
            selectedDatePickerID = R.id.schedule_start_time
            datePicker = false
        }
        else if (id == SCHEDULE_FINISH_TIME_DIALOG_ID) {
            time = scheduleFinishTime
            selectedDatePickerID = R.id.schedule_finish_time
            datePicker = false
        }

        if (datePicker) {
            return DatePickerDialog(this, datePickerListener, date!!.year, date.month, date.day)
        }
        else {
            return TimePickerDialog(this, timePickerListener, time!!.hours, time.minutes, true)
        }
    }

    private val datePickerListener = object : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int) {
            if (selectedDatePickerID == R.id.schedule_start_date) {
                scheduleStartDate = WorkOrderFormActivity.Date(selectedDay, selectedMonth, selectedYear)
                schedule_start_date.setText("" + (selectedMonth + 1) + "/" + selectedDay  + "/" + selectedYear)
            }
            else {
                scheduleFinishDate = WorkOrderFormActivity.Date(selectedDay, selectedMonth, selectedYear)
                schedule_finish_date.setText("" + (selectedMonth + 1) + "/" + selectedDay  + "/" + selectedYear)
            }
        }
    }

    private val timePickerListener = object : TimePickerDialog.OnTimeSetListener {
        override fun onTimeSet(view: TimePicker, selectedHour: Int, selectedMinute: Int) {
            var decimalFormatter = DecimalFormat("00")
            if (selectedDatePickerID == R.id.schedule_start_time) {
                scheduleStartTime = Time(selectedHour, selectedMinute)
                var strHour = decimalFormatter.format(selectedHour)
                var strMinute = decimalFormatter.format(selectedMinute)

                schedule_start_time.setText("" + strHour + ":" + strMinute)
            }
            else {
                scheduleFinishTime = Time(selectedHour, selectedMinute)
                var strHour = decimalFormatter.format(selectedHour)
                var strMinute = decimalFormatter.format(selectedMinute)

                schedule_finish_time.setText("" + strHour + ":" + strMinute)
            }
        }
    }
}
