package jp.techacademy.asaki.minegishi.qa_app_2;

import java.io.Serializable;

public class Favorite implements Serializable{

    private String mQuestionUid;   //Firebaseから取得した質問のUID
    private String mFavoriteUid;

    public String getQuestionUid() {
        return mQuestionUid;
    }
    public String getFavoriteUid() {
        return mFavoriteUid;
    }

    public Favorite(String questionUid, String favoriteUid) {
        mQuestionUid = questionUid;
        mFavoriteUid = favoriteUid;
    }
}
