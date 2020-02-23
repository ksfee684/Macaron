package org.ksfee.android.macaron.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_detail.*
import org.ksfee.android.macaron.R
import org.ksfee.android.macaron.sample.model.User
import org.ksfee.android.macaron.sample.model.UserQuery
import org.ksfee.android.macaron.sample.model.UserUpdater
import org.ksfee.android.rx_bind.rx

class UserDetailActivity : AppCompatActivity() {

    private val documentPath: String
        get() = intent.getStringExtra(EXTRA_DOCUMENT_PATH)

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        update_button.setOnClickListener { updateUser() }

        fetchUser()
    }

    @SuppressLint("CheckResult")
    private fun fetchUser() {
        UserQuery
            .document
            .rx
            .getAsSingle(documentPath)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::setupView) {
                Log.e(TAG, "Couldn't fetch document", it)
            }
    }

    private fun setupView(user: User) {
        Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
        this.user = user
        name_edit_text.setText(user.name)
        age_edit_text.setText(user.age.toString())
        description_edit_text.setText(user.description ?: "")
        created_at_edit_text.setText(user.createdAt.toString())
    }

    @SuppressLint("CheckResult")
    private fun updateUser() {
        UserUpdater(user)
            .updateName(name_edit_text.text.toString())
            .updateAge(age_edit_text.text.toString().toLong())
            .updateDescription(description_edit_text.text.toString())
            .updateCreatedAt(created_at_edit_text.text.toString().toLong())
            .rx
            .updateAsSingle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::setupView) {
                Log.e(TAG, "Couldn't update document", it)
            }
    }

    companion object {
        const val EXTRA_DOCUMENT_PATH = "extra_document_path"

        private val TAG = UserDetailActivity::class.java.simpleName
    }
}
