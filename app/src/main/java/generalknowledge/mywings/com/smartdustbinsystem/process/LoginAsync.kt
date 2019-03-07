package generalknowledge.mywings.com.smartdustbinsystem.process

import android.os.AsyncTask
import org.json.JSONObject

class LoginAsync : AsyncTask<JSONObject, Void, JSONObject>() {

    private val httpConnectionUtil = HttpConnectionUtil()

    private lateinit var onLoginListener: OnLoginListener

    override fun doInBackground(vararg params: JSONObject?): JSONObject? {

        val response = httpConnectionUtil.requestPost(ConstantUtils.URL + ConstantUtils.LOGIN, params[0])

        return if (response.isNullOrBlank()) null else JSONObject(response)
    }

    override fun onPostExecute(result: JSONObject?) {
        super.onPostExecute(result)

        onLoginListener.onLoginSuccess(result)
    }

    fun setOnLoginListener(onLoginListener: OnLoginListener, request: JSONObject) {
        this.onLoginListener = onLoginListener
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request)

    }
}