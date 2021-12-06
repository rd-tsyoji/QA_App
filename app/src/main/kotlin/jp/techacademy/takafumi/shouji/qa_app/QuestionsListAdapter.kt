package jp.techacademy.takafumi.shouji.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import jp.techacademy.takafumi.shouji.qa_app.databinding.ListQuestionsBinding

class QuestionsListAdapter(context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mQuestionArrayList = ArrayList<Question>()

    override fun getCount(): Int {
        return mQuestionArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mQuestionArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val binding =
            if (view == null) {
                ListQuestionsBinding.inflate(mLayoutInflater, parent, false)
            } else {
                ListQuestionsBinding.bind(view)
            }

        val titleText = binding.titleTextView
        titleText.text = mQuestionArrayList[position].title

        val nameText = binding.nameTextView
        nameText.text = mQuestionArrayList[position].name

        val resText = binding.resTextView
        val resNum = mQuestionArrayList[position].answers.size
        resText.text = resNum.toString()

        val bytes = mQuestionArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            val imageView = binding.imageView
            imageView.setImageBitmap(image)
        }

        return binding.root
    }

    fun setQuestionArrayList(questionArrayList: ArrayList<Question>) {
        mQuestionArrayList = questionArrayList
    }
}