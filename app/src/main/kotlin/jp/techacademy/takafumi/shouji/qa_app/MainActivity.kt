package jp.techacademy.takafumi.shouji.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import jp.techacademy.takafumi.shouji.qa_app.databinding.ActivityMainBinding
import java.util.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenre = 0
    private var snapshotListener: ListenerRegistration? = null

    // 注意
    // FirebaseDatabase.getInstance()を使用する際は引数にDBのURLを入れる

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // idがtoolbarがインポート宣言により取得されているので
        // id名でActionBarのサポートを依頼
        setSupportActionBar(binding.appBar.toolbar)

        // fabにClickリスナーを登録
        binding.appBar.fab.setOnClickListener { view ->
            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(
                    view,
                    getString(R.string.question_no_select_genre),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {

            }

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }

        // ナビゲーションドロワーの設定
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBar.toolbar,
            R.string.app_name,
            R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // ListViewの準備
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        binding.appBar.contents.listView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // 1:趣味を既定の選択とする
        if (mGenre == 0) {
            onNavigationItemSelected(binding.navView.menu.getItem(0))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_hobby -> {
                binding.appBar.toolbar.title = getString(R.string.menu_hobby_label)
                mGenre = 1
            }
            R.id.nav_life -> {
                binding.appBar.toolbar.title = getString(R.string.menu_life_label)
                mGenre = 2
            }
            R.id.nav_health -> {
                binding.appBar.toolbar.title = getString(R.string.menu_health_label)
                mGenre = 3
            }
            R.id.nav_computer -> {
                binding.appBar.toolbar.title = getString(R.string.menu_computer_label)
                mGenre = 4
            }
            R.id.nav_favorite -> {
                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                // ログインしていなければログイン画面に遷移させる
                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    // ユーザーIDを渡してお気に入り一覧画面を起動する
                    val intent = Intent(applicationContext, FavoriteListActivity::class.java)
                    intent.putExtra("userId", user.uid)
                    startActivity(intent)
                }
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        binding.appBar.contents.listView.adapter = mAdapter

        // 一つ前のリスナーを消す
        snapshotListener?.remove()

        // 選択したジャンルにリスナーを登録する
        snapshotListener = FirebaseFirestore.getInstance()
            .collection(ContentsPATH)
            .whereEqualTo("genre", mGenre)
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
                            } as ArrayList<Answer>
                        )
                    }
                }
                mQuestionArrayList.clear()
                mQuestionArrayList.addAll(questions)
                mAdapter.notifyDataSetChanged()
            }

        return true
    }
}