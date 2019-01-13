package generalknowledge.mywings.com.smartdustbinsystem.process

import org.json.JSONArray

interface OnDustbinListener {
    fun onDustbinSuccess(result: JSONArray)
}