package jp.techacademy.takafumi.shouji.qa_app

import java.util.UUID
import java.util.ArrayList

class FireStoreQuestion {
    var id = UUID.randomUUID().toString()
    var title = ""
    var body = ""
    var name = ""
    var uid = ""
    var image = ""
    var genre = 0
    var answers: ArrayList<FirestoreAnswer> = arrayListOf()
}