package generalknowledge.mywings.com.smartdustbinsystem.process

import android.os.AsyncTask
import org.json.JSONArray

class GetDustbinAsync : AsyncTask<Int, Void, JSONArray>() {

    private val httpConnectionUtil = HttpConnectionUtil()

    private lateinit var onDustbinListener: OnDustbinListener

    override fun doInBackground(vararg params: Int?): JSONArray {
        var response =
            httpConnectionUtil.requestGet(ConstantUtils.URL + ConstantUtils.GET_DUST_BIN + "?vid=${params[0]}")
        //response = "[{\"Id\":1,\"Latitude\":\"18.523793\",\"LocalArea\":\"Near PMC\",\"Longitude\":\"73.853267\",\"Moisture\":\"true\",\"Name\":\"PMC corpo\",\"VId\":1,\"Weight\":\"60\"},{\"Id\":2,\"Latitude\":\"18.525802\",\"LocalArea\":\"Near JM Road\",\"Longitude\":\"73.852592\",\"Moisture\":\"true\",\"Name\":\"Hotel Xiong\",\"VId\":1,\"Weight\":\"70\"}]"

        return JSONArray(response)
    }

    override fun onPostExecute(result: JSONArray?) {
        super.onPostExecute(result)
        onDustbinListener.onDustbinSuccess(result!!)
    }

    fun setOnDustbinListener(onDustbinListener: OnDustbinListener, vid: Int) {
        this.onDustbinListener = onDustbinListener
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, vid)
    }


}