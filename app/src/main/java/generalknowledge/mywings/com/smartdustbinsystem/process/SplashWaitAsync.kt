package generalknowledge.mywings.com.smartdustbinsystem.process

import android.os.AsyncTask

class SplashWaitAsync : AsyncTask<Int, Void, Int>() {

    private lateinit var onSplashCompleteListener: OnSplashCompleteListener

    override fun doInBackground(vararg param: Int?): Int {
        Thread.sleep(15 * 1000)
        return 0
    }

    override fun onPostExecute(result: Int?) {
        super.onPostExecute(result)
        onSplashCompleteListener.onSplashComplete(result!!)
    }

    fun setOnFlashListener(onSplashCompleteListener: OnSplashCompleteListener, request: Int) {
        this.onSplashCompleteListener = onSplashCompleteListener
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request)
    }


}