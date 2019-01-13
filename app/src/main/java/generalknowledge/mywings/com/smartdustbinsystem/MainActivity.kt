package generalknowledge.mywings.com.smartdustbinsystem

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSignIn.setOnClickListener {


            if (txtUserName.text!!.isNotEmpty() && txtPassword.text!!.isNotEmpty()) {
                val intent = Intent(this@MainActivity, RouteScreenActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this@MainActivity, "Please enter username and password", Toast.LENGTH_LONG).show()
            }



        }
    }
}
