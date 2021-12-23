package jp.techacademy.takafumi.shouji.qa_app

import java.io.Serializable

class Answer(val body: String, val name: String, val uid: String, val answerUid: String) :
    Serializable

fun answerFromFireStoreAnswer(answer: FirestoreAnswer): Answer {
    return Answer(answer.body,answer.name,answer.uid,answer.answerUid)
}