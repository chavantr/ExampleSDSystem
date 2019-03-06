package generalknowledge.mywings.com.smartdustbinsystem.process

import generalknowledge.mywings.com.smartdustbinsystem.models.Vehicle

interface OnVehicleListener {
    fun onVehicleSuccess(vehicle: List<Vehicle>)
}