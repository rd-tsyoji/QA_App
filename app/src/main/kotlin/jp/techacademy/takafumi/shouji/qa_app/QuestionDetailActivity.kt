package jp.techacademy.takafumi.shouji.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import jp.techacademy.takafumi.shouji.qa_app.databinding.ActivityQuestionDetailBinding

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionDetailBinding
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private var user = FirebaseAuth.getInstance().currentUser

    private var snapshotListener: ListenerRegistration? = null

    private var isFavorited = false
    private var userFavoriteQuestionIds: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        binding.favoriteImageView.visibility = if (user == null) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        binding.listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        binding.fab.setOnClickListener {
            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        checkFavorite(user)

        binding.favoriteImageView.apply {
            // 星マークをお気に入り状態によって変更
            setImageResource(if (isFavorited) R.drawable.ic_star else R.drawable.ic_star_border)
            setOnClickListener {
                // ログインしていなければログイン画面に遷移させる
                if (user == null) {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                    return@setOnClickListener
                } else {
                    // ログイン状態の場合はお気に入り追加・削除
                    val newFavorites = FirestoreFavorite()
                    newFavorites.uid = user!!.uid
                    newFavorites.questionIds = userFavoriteQuestionIds
                    if (isFavorited) {
                        newFavorites.questionIds.remove(mQuestion.questionUid)
                        setImageResource(R.drawable.ic_star_border)
                    } else {
                        newFavorites.questionIds.add(mQuestion.questionUid)
                        setImageResource(R.drawable.ic_star)
                    }
                    FirebaseFirestore.getInstance()
                        .collection(FavoritesPATH).document(user!!.uid)
                        .set(newFavorites).addOnFailureListener {
                            it.printStackTrace()
                        }
                    isFavorited = !isFavorited
                }
            }
        }

        // 一つ前のリスナーを消す
        snapshotListener?.remove()

        snapshotListener = FirebaseFirestore.getInstance().collection(ContentsPATH)
            .whereEqualTo("id", mQuestion.questionUid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    // 取得エラー
                    return@addSnapshotListener
                }
                var answers = listOf<FirestoreAnswer>()
                val results = querySnapshot?.toObjects(FireStoreQuestion::class.java)
                results?.also { answers = it[0].answers.toList() }
                mQuestion.answers.clear()
                answers.forEach { mQuestion.answers.add(answerFromFireStoreAnswer(it)) }
                mAdapter.notifyDataSetChanged()
            }
    }

    override fun onResume() {
        super.onResume()
        checkFavorite(user)
    }

    /**
     * お気に入り登録状況を確認し、登録されていれば星アイコンを替える
     */
    private fun checkFavorite(user: FirebaseUser?) {
        if (user != null) {
            // お気に入りに登録されているか確認
            FirebaseFirestore.getInstance().collection(FavoritesPATH)
                .whereEqualTo("uid", user.uid).get().addOnSuccessListener { querySnapshot ->
                    val results = querySnapshot.toObjects(FirestoreFavorite::class.java)
                    results.also {
                        if (it.isEmpty()) return@also
                        userFavoriteQuestionIds = it[0].questionIds
                        userFavoriteQuestionIds.forEach { questionId ->
                            if (questionId == mQuestion.questionUid) {
                                isFavorited = true
                                binding.favoriteImageView.setImageResource(R.drawable.ic_star)
                                return@also
                            }
                        }
                    }
                }
        }
    }
}