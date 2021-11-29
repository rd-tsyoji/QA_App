package jp.techacademy.takafumi.shouji.qa_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import jp.techacademy.takafumi.shouji.qa_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    private var mGenre = 0

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

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if(mGenre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
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
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}