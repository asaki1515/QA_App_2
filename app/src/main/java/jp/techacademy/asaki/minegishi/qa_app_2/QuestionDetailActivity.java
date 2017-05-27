package jp.techacademy.asaki.minegishi.qa_app_2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class QuestionDetailActivity extends AppCompatActivity {


    private ListView mListView;
    private Button mFavoriteButton;
    private Question mQuestion;
    //////
    private ArrayList<Favorite> mFavorite;
    private int Flag = 0;
    //////
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;
    private DatabaseReference FavoriteRef;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        // 要素（回答）が追加されたとき呼ばれるメソッド
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // 追加された回答をmapに取得
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            // 追加された回答が新しいものなら各回答の情報を取得
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            // Answerに各情報をセット
            Answer answer = new Answer(body, name, uid, answerUid);
            // 回答データを質問データに追加
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        mFavorite = (ArrayList<Favorite>) extras.get("favorite");

        setTitle(mQuestion.getTitle());


        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // buttonの準備
        mFavoriteButton = (Button) findViewById(R.id.favorite);

        // お気に入りボタンの表示非表示設定
        // ログイン済みのユーザーを取得する
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();

        if (user == null) {
            // ログインしていなければmFavoriteButton無効
            mFavoriteButton.setVisibility(View.INVISIBLE);
            mFavoriteButton.setEnabled(false);
        }else {
            mFavoriteButton.setVisibility(View.VISIBLE);
            mFavoriteButton.setEnabled(true);

            for (Favorite favorite:mFavorite) {
                if (favorite.getQuestionUid().equals(mQuestion.getQuestionUid())) {
                    mFavoriteButton.setText("★");
                    Flag = 1;
                }
            }
            FavoriteRef = dataBaseReference.child(Const.FavoritePATH).child(user.getUid());
        }



        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Flag == 1) {
                    for (Favorite favorite : mFavorite) {
                        if (favorite.getQuestionUid().equals(mQuestion.getQuestionUid())) {
                            mFavorite.remove(favorite);
                            FavoriteRef.child(favorite.getFavoriteUid()).removeValue();
                            mFavoriteButton.setText("☆");
                            Flag = 0;
                            return;
                        }
                    }
                }else {
                    String key = FavoriteRef.push().getKey();
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(key + "/questionUid/", mQuestion.getQuestionUid());
                    FavoriteRef.updateChildren(data);
                    Favorite favorite1 = new Favorite(mQuestion.getQuestionUid(), key);
                    mFavorite.add(favorite1);
                    mFavoriteButton.setText("★");
                    Flag = 1;
                }
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}