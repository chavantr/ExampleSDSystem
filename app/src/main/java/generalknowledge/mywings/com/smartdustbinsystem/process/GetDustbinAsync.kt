package generalknowledge.mywings.com.smartdustbinsystem.process

import android.os.AsyncTask
import org.json.JSONArray

class GetDustbinAsync : AsyncTask<Void, Void, JSONArray>() {

    private val httpConnectionUtil = HttpConnectionUtil()

    private lateinit var onDustbinListener: OnDustbinListener

    override fun doInBackground(vararg p0: Void?): JSONArray {
        val response = httpConnectionUtil.requestGet("")
        return JSONArray(response)
    }

    override fun onPostExecute(result: JSONArray?) {
        super.onPostExecute(result)
        onDustbinListener.onDustbinSuccess(result!!)
    }

    fun setOnDustbinListener(onDustbinListener: OnDustbinListener) {
        this.onDustbinListener = onDustbinListener
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }


}