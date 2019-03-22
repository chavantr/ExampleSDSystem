package generalknowledge.mywings.com.smartdustbinsystem

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import generalknowledge.mywings.com.smartdustbinsystem.models.User
import generalknowledge.mywings.com.smartdustbinsystem.models.UserInfoHolder
import generalknowledge.mywings.com.smartdustbinsystem.process.LoginAsync
import generalknowledge.mywings.com.smartdustbinsystem.process.OnLoginListener
import generalknowledge.mywings.com.smartdustbinsystem.process.ProgressDialogUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), OnLoginListener {

    private lateinit var progressDialogUtil: ProgressDialogUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressDialogUtil = ProgressDialogUtil(this)
        btnSignIn.setOnClickListener {
            if (validate()) {
                initLogin()
            } else {
                Toast.makeText(this@MainActivity, "Please enter username and password", Toast.LENGTH_LONG).show()
            }
        }


    }


    private fun validate(): Boolean = txtUserName.text!!.isNotEmpty() && txtPassword.text!!.isNotEmpty()


    override fun onLoginSuccess(result: JSONObject?) {

        progressDialogUtil.hide()

        if (null != result) {

            var user = User()

            user.id = result.getInt("Id")

            user.name = result.getString("Name")

            user.username = result.getString("Username")

            user.password = result.getString("Password")

            user.number = result.getString("Number")

            UserInfoHolder.getInstance().user = user

            val intent = Intent(this@MainActivity, RouteScreenActivityWithNavigation::class.java)
            startActivity(intent)

        } else {
            Toast.makeText(this@MainActivity, "Enter correct username or password", Toast.LENGTH_LONG).show()
        }

    }

    private fun initLogin() {
        progressDialogUtil.show()
        var loginAsync = LoginAsync()
        var request = JSONObject()
        var param = JSONObject()
        param.put("Username", txtUserName.text)
        param.put("Password", txtPassword.text)
        request.put("login", param)
        loginAsync.setOnLoginListener(this, request)
    }
}
