package generalknowledge.mywings.com.smartdustbinsystem.process

import android.os.AsyncTask
import generalknowledge.mywings.com.smartdustbinsystem.models.Vehicle
import org.json.JSONArray

class GetVehicleAsync : AsyncTask<Void, Void, List<Vehicle>>() {

    private val httpConnectionUtil = HttpConnectionUtil()

    private lateinit var onVehicleListener: OnVehicleListener

    override fun doInBackground(vararg param: Void?): List<Vehicle>? {
        var response = httpConnectionUtil.requestGet(ConstantUtils.URL + ConstantUtils.GET_VEHICLE)
        // response ="[{\"Id\":1,\"Name\":\"Puna Gavthan\",\"Number\":\"MH 12 FE1287\"},{\"Id\":2,\"Name\":\"Puna Gavthan\",\"Number\":\"MH 12 GW 0289\"}]"
        if (response.isNotEmpty()) {
            var vehicles = ArrayList<Vehicle>()
            var jVehicle = JSONArray(response)

            for (i in 0 until (jVehicle.length())) {
                var vehicle = Vehicle()
                val jNode = jVehicle.getJSONObject(i)
                vehicle.id = jNode.getInt("Id")
                vehicle.name = jNode.getString("Name")
                vehicle.number = jNode.getString("Number")
                vehicles.add(vehicle)
            }

            return vehicles
        }

        return null
    }

    override fun onPostExecute(result: List<Vehicle>?) {
        super.onPostExecute(result)

        onVehicleListener.onVehicleSuccess(result!!)
    }

    fun setOnVehicleListener(onVehicleListener: OnVehicleListener) {
        this.onVehicleListener = onVehicleListener
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
}