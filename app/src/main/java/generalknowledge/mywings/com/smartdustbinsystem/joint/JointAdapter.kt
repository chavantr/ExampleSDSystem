package generalknowledge.mywings.com.smartdustbinsystem.joint

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import generalknowledge.mywings.com.smartdustbinsystem.R
import generalknowledge.mywings.com.smartdustbinsystem.models.Vehicle
import kotlinx.android.synthetic.main.layout_row.view.*

class JointAdapter(var list: List<Vehicle>) : RecyclerView.Adapter<JointAdapter.JointViewHolder>() {

    var lstVehicle = list;

    private lateinit var onVehicleSelectedListener: OnVehicleSelectedListener

    override fun onCreateViewHolder(parent: ViewGroup, itemType: Int): JointViewHolder {
        return JointViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_row, parent, false))
    }

    override fun getItemCount(): Int = lstVehicle.size

    override fun onBindViewHolder(viewHolder: JointViewHolder, postion: Int) {

        viewHolder.lblName.text = lstVehicle[postion].name + "\n" + lstVehicle[postion].number

        viewHolder.lblName.setOnClickListener {
            onVehicleSelectedListener.onVehicleSelected(lstVehicle[postion])
        }

    }


    inner class JointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lblName = itemView.lblName
    }

    fun setOnVehicleSelectedListener(onVehicleSelectedListener: OnVehicleSelectedListener) {
        this.onVehicleSelectedListener = onVehicleSelectedListener
    }

}