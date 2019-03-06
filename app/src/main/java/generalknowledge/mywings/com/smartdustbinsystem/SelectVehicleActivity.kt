package generalknowledge.mywings.com.smartdustbinsystem

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import generalknowledge.mywings.com.smartdustbinsystem.joint.JointAdapter
import generalknowledge.mywings.com.smartdustbinsystem.joint.OnVehicleSelectedListener
import generalknowledge.mywings.com.smartdustbinsystem.models.Vehicle
import generalknowledge.mywings.com.smartdustbinsystem.process.GetVehicleAsync
import generalknowledge.mywings.com.smartdustbinsystem.process.OnVehicleListener
import generalknowledge.mywings.com.smartdustbinsystem.process.ProgressDialogUtil
import kotlinx.android.synthetic.main.activity_select_vehicle.*

class SelectVehicleActivity : AppCompatActivity(), OnVehicleListener, OnVehicleSelectedListener {

    private lateinit var jointAdapter: JointAdapter
    private lateinit var progressDialogUtil: ProgressDialogUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_vehicle)
        lstVehicles.layoutManager = LinearLayoutManager(this)
        progressDialogUtil = ProgressDialogUtil(this)
        initGetVehicles()
    }

    override fun onVehicleSuccess(vehicle: List<Vehicle>) {
        progressDialogUtil.hide()
        if (vehicle.isNotEmpty()) {
            jointAdapter = JointAdapter(vehicle)
            jointAdapter.setOnVehicleSelectedListener(this@SelectVehicleActivity)
            lstVehicles.adapter = jointAdapter
        }
    }

    override fun onVehicleSelected(vehicle: Vehicle) {
        val intent = Intent()
        intent.putExtra("id", vehicle.id)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun initGetVehicles() {
        progressDialogUtil.show()
        val getVehicleAsync = GetVehicleAsync()
        getVehicleAsync.setOnVehicleListener(this)
    }


}
