package jp.techacademy.takafumi.shouji.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import jp.techacademy.takafumi.shouji.qa_app.databinding.ListAnswerBinding
import jp.techacademy.takafumi.shouji.qa_app.databinding.ListQuestionDetailBinding

class QuestionDetailListAdapter(context: Context, private val mQustion: Question) : BaseAdapter() {
    companion object {
        private const val TYPE_QUESTION = 0
        private const val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return 1 + mQustion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQustion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        return if (getItemViewType(position) == TYPE_QUESTION) {
            getQuestionView(view, parent)
        } else {
            getAnswerView(position, view, parent)
        }
    }

    private fun getQuestionView(view: View?, parent: ViewGroup): RelativeLayout {
        val binding =
            if (view == null) {
                ListQuestionDetailBinding.inflate(mLayoutInflater, parent, false)
            } else {
                ListQuestionDetailBinding.bind(view)
            }
        val body = mQustion.body
        val name = mQustion.name

        val bodyTextView = binding.bodyTextView
        bodyTextView.text = body

        val nameTextView = binding.nameTextView
        nameTextView.text = name

        val bytes = mQustion.imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            val imageView = binding.imageView
            imageView.setImageBitmap(image)
        }

        return binding.root
    }

    private fun getAnswerView(position: Int, view: View?, parent: ViewGroup): RelativeLayout {
        val binding =
            if (view == null) {
                ListAnswerBinding.inflate(mLayoutInflater, parent, false)
            } else {
                ListAnswerBinding.bind(view)
            }
        val answer = mQustion.answers[position - 1]
        val body = answer.body
        val name = answer.name

        val bodyTextView = binding.bodyTextView
        bodyTextView.text = body

        val nameTextView = binding.nameTextView
        nameTextView.text = name

        return binding.root
    }
}