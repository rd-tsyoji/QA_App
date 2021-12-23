package jp.techacademy.takafumi.shouji.qa_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import jp.techacademy.takafumi.shouji.qa_app.databinding.ActivityAnswerSendBinding
import java.util.ArrayList

class AnswerSendActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAnswerSendBinding
    private lateinit var mQuestion: Question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnswerSendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        // UIの準備
        binding.sendButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        // キーボードが出てたら閉じる
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        // Preferenceから名前を取る
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")

        // 回答を取得する
        val answer = binding.answerEditText.text.toString()
        if (answer.isEmpty()) {
            // 回答が入力されていない時はエラーを表示するだけ
            Snackbar.make(v, getString(R.string.answer_error_message), Snackbar.LENGTH_LONG).show()
            return
        }

        val newAnswer = FirestoreAnswer()
        newAnswer.body = answer
        newAnswer.name = name!!
        newAnswer.uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        newAnswer.answerUid = ""

        val newAnswers = ArrayList<FirestoreAnswer>()
        mQuestion.answers.forEach {
            newAnswers.add(fireStoreAnswerFromAnswer(it))
        }
        newAnswers.add(newAnswer)

        val data = hashMapOf(
            "answers" to newAnswers
        )

        binding.progressBar.visibility = View.VISIBLE
        FirebaseFirestore.getInstance()
            .collection(ContentsPATH).document(mQuestion.questionUid)
            .set(data, SetOptions.mergeFields(AnswersPATH))
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                finish()
            }
            .addOnFailureListener {
                it.printStackTrace()
                binding.progressBar.visibility = View.GONE
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.question_send_error_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }
}