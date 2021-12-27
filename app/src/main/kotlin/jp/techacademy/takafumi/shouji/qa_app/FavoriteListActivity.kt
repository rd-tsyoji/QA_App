package jp.techacademy.takafumi.shouji.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import jp.techacademy.takafumi.shouji.qa_app.databinding.ActivityFavoriteListBinding

class FavoriteListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteListBinding
    private lateinit var mFavoriteQuestionList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter
    private lateinit var userId: String

    private var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        userId = extras!!.get("userId") as String

        title = getString(R.string.menu_favorite_label)

        // ListViewの準備
        mAdapter = QuestionsListAdapter(this)
        mFavoriteQuestionList = java.util.ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        binding.listView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mFavoriteQuestionList[position])
            startActivity(intent)
        }

        updateFavoriteList(userId)
    }

    override fun onResume() {
        super.onResume()
        updateFavoriteList(userId)
    }

    /**
     * お気に入り登録されている質問IDを取得し反映
     */
    private fun updateFavoriteList(userId: String) {
        mFavoriteQuestionList.clear()
        mAdapter.setQuestionArrayList(mFavoriteQuestionList)
        binding.listView.adapter = mAdapter

        var userQuestionIds: ArrayList<String> = ArrayList()

        // お気に入り質問ID一覧を取得
        FirebaseFirestore.getInstance().collection(FavoritesPATH)
            .whereEqualTo("uid", userId).get().addOnSuccessListener { querySnapshot ->
                val results = querySnapshot.toObjects(FirestoreFavorite::class.java)
                results.also {
                    if (it.isEmpty()) return@also
                    userQuestionIds = it[0].questionIds
                }
                if (userQuestionIds.isNotEmpty()) {
                    updateQuestionList(userQuestionIds)
                }
            }
    }

    /**
     * お気に入り質問IDから質問情報を取得
     */
    private fun updateQuestionList(ids: ArrayList<String>) {
        snapshotListener = FirebaseFirestore.getInstance()
            .collection(ContentsPATH)
            .whereIn("id", ids)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    // 取得エラー
                    return@addSnapshotListener
                }
                var questions = listOf<Question>()
                val results = querySnapshot?.toObjects(FireStoreQuestion::class.java)
                results?.also {
                    questions = it.map { firestoreQuestion ->
                        val bytes =
                            if (firestoreQuestion.image.isNotEmpty()) {
                                Base64.decode(firestoreQuestion.image, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }
                        Question(
                            firestoreQuestion.title,
                            firestoreQuestion.body,
                            firestoreQuestion.name,
                            firestoreQuestion.uid,
                            firestoreQuestion.id,
                            firestoreQuestion.genre,
                            bytes,
                            firestoreQuestion.answers.map { answer ->
                                Answer(
                                    answer.body,
                                    answer.name,
                                    answer.uid,
                                    answer.answerUid
                                )
                            } as java.util.ArrayList<Answer>
                        )
                    }
                }
                mFavoriteQuestionList.clear()
                mFavoriteQuestionList.addAll(questions)
                mAdapter.notifyDataSetChanged()
            }
    }
}