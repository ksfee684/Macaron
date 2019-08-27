package org.ksfee.android.macaron.sample

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import kotlinx.android.synthetic.main.activity_user_list.*
import kotlinx.android.synthetic.main.item_user.view.*
import org.ksfee.android.macaron.R
import org.ksfee.android.macaron.sample.model.User
import org.ksfee.android.macaron.sample.model.UserCreator
import org.ksfee.android.macaron.sample.model.UserQuery
import org.ksfee.android.macaron.sample.model.delete
import java.util.*

class UserListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        FirebaseApp.initializeApp(this)

        fab.setOnClickListener {
            UserCreator().create(
                User(name = "Mike", age = 25, description = null, createdAt = Date().time)
            )
                .addOnSuccessListener(OnSuccessListener {
                    fetchUsers()
                })
        }
        fetchUsers()
    }

    private fun fetchUsers() {
        UserQuery()
            .get()
            .addOnSuccessListener(OnSuccessListener {
                user_list.adapter = UserAdapter(this@UserListActivity, it)
                user_list.setOnItemLongClickListener { _, _, position, _ ->
                    (user_list.adapter.getItem(position) as User)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this@UserListActivity, "Deleted!", Toast.LENGTH_SHORT).show()
                            fetchUsers()
                        }
                    true
                }
                Toast.makeText(this@UserListActivity, "Fetched!", Toast.LENGTH_SHORT).show()
            })
    }
}

class UserAdapter(
    private val context: Context,
    private val userList: List<User>
) : BaseAdapter() {

    override fun getItem(position: Int): User = userList[position]

    override fun getItemId(position: Int): Long = getItem(position).hashCode().toLong()

    override fun getCount(): Int = userList.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.item_user, parent, false).apply {
            user_name.text = getItem(position).documentReference?.path
        }
    }
}
