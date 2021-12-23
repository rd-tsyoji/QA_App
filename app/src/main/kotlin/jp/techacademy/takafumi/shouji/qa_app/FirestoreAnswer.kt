package jp.techacademy.takafumi.shouji.qa_app

class FirestoreAnswer {
    var body = ""
    var name = ""
    var uid = ""
    var answerUid = ""
}

fun fireStoreAnswerFromAnswer(answer: Answer): FirestoreAnswer {
    val ret = FirestoreAnswer()
    ret.body = answer.body
    ret.name = answer.name
    ret.uid = answer.uid
    ret.answerUid = answer.answerUid
    return ret
}