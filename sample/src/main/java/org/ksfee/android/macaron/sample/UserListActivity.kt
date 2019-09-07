package org.ksfee.android.macaron.sample

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_list.*
import kotlinx.android.synthetic.main.item_user.view.*
import org.ksfee.android.macaron.R
import org.ksfee.android.macaron.sample.model.User
import org.ksfee.android.macaron.sample.model.UserCreator
import org.ksfee.android.macaron.sample.model.UserQuery
import java.util.*

class UserListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        FirebaseApp.initializeApp(this)

        fab.setOnClickListener {
            UserCreator
                .createAsSingle(
                    User(name = "Mike", age = 25, description = null, createdAt = Date().time)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    fetchUsers()
                }, {
                    Log.e(TAG, "Couldn't create user", it)
                })
        }

        fetchUsers()
    }

    @SuppressLint("CheckResult")
    private fun fetchUsers() {
        UserQuery
            .collection
            .getAsSingle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                user_list.adapter = UserAdapter(this@UserListActivity, it)
                user_list.setOnItemLongClickListener { _, _, position, _ ->
                    (user_list.adapter.getItem(position) as User)
                        .deleteAsCompletable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe {
                            Toast.makeText(this@UserListActivity, "Deleted!", Toast.LENGTH_SHORT).show()
                            fetchUsers()
                        }
                    true
                }
                user_list.setOnItemClickListener { _, _, position, _ ->
                    navigateUserDetail(
                        (user_list.adapter.getItem(position) as User).documentReference!!.id
                    )
                }
                Toast.makeText(this@UserListActivity, "Fetched!", Toast.LENGTH_SHORT).show()
            }, {
                Log.e(TAG, "Couldn't get users", it)
            })
    }

    private fun navigateUserDetail(documentPath: String) {
        startActivity(
            Intent(this, UserDetailActivity::class.java).apply {
                putExtra(UserDetailActivity.EXTRA_DOCUMENT_PATH, documentPath)
            }
        )
    }

    companion object {
        private val TAG = UserListActivity::class.java.simpleName
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
