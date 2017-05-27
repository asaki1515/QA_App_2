package jp.techacademy.asaki.minegishi.qa_app_2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoriteQuestionListActivity extends AppCompatActivity {

    //private int mGenre = 1;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef1;
    private DatabaseReference mGenreRef2;
    private DatabaseReference mGenreRef3;
    private DatabaseReference mGenreRef4;
    private DatabaseReference mFavoriteRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private ArrayList<Favorite> mFavoriteArrayList;
    private QuestionsListAdapter mAdapter;


    // データに追加・変化があった時に受け取るリスナー
    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String questionUid = (String) map.get("questionUid");

            Favorite favorite = new Favorite(questionUid, dataSnapshot.getKey());
            mFavoriteArrayList.add(favorite);

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

            for (Favorite favorite: mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getFavoriteUid())){
                    mFavoriteArrayList.remove(favorite);

                    for (Question question:mQuestionArrayList) {
                        if (question.getQuestionUid().equals(favorite.getQuestionUid())) {
                            mQuestionArrayList.remove(question);
                            mAdapter.notifyDataSetChanged();
                            return;
                        }
                    }
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    //////

    // データに追加・変化があった時に受け取るリスナー
    private ChildEventListener mEventListener1 = new ChildEventListener() {
        @Override
        // 要素（質問）が追加されたとき呼ばれるメソッド
        // QuestionSendActivityで質問が追加されると呼ばれる
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            for (Favorite favorite:mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getQuestionUid())) {

                    // 追加されたデータをmapに取得
                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // そのmapから各情報を取得
                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String uid = (String) map.get("uid");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {  // 回答があれば
                        //
                        for (Object key : answerMap.keySet()) {
                            // Firebaseから取得した回答のUIDの回答をtempに取得
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    // QuestionsListAdapterにデータを設定
                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), 1, bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        // 要素（質問）に変化があった時受けるリスナー
        // ここでは質問に対して回答が投稿された時に呼ばれる
        // このメソッドが呼ばれたら変化があった質問に対応するQuestionクラスのインスタンスが保持している回答のArrayListを一旦クリアし、取得した回答を設定
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            for (Favorite favorite:mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getQuestionUid())) {

                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // 変更があったQuestionを探す
                    for (Question question : mQuestionArrayList) {

                        // 投稿された質問のUIDがFirebaseから取得した質問のUIDと一緒なら
                        if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                            // このアプリで変更がある可能性があるのは回答(Answer)のみ
                            question.getAnswers().clear();
                            // もう一度回答を設定
                            HashMap answerMap = (HashMap) map.get("answers");
                            if (answerMap != null) {
                                for (Object key : answerMap.keySet()) {
                                    HashMap temp = (HashMap) answerMap.get((String) key);
                                    String answerBody = (String) temp.get("body");
                                    String answerName = (String) temp.get("name");
                                    String answerUid = (String) temp.get("uid");
                                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                                    question.getAnswers().add(answer);
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
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

    // データに追加・変化があった時に受け取るリスナー
    private ChildEventListener mEventListener2 = new ChildEventListener() {
        @Override
        // 要素（質問）が追加されたとき呼ばれるメソッド
        // QuestionSendActivityで質問が追加されると呼ばれる
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            for (Favorite favorite:mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getQuestionUid())) {

                    // 追加されたデータをmapに取得
                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // そのmapから各情報を取得
                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String uid = (String) map.get("uid");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {  // 回答があれば
                        //
                        for (Object key : answerMap.keySet()) {
                            // Firebaseから取得した回答のUIDの回答をtempに取得
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    // QuestionsListAdapterにデータを設定
                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), 2, bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        // 要素（質問）に変化があった時受けるリスナー
        // ここでは質問に対して回答が投稿された時に呼ばれる
        // このメソッドが呼ばれたら変化があった質問に対応するQuestionクラスのインスタンスが保持している回答のArrayListを一旦クリアし、取得した回答を設定
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            for (Favorite favorite:mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getQuestionUid())) {
                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // 変更があったQuestionを探す
                    for (Question question : mQuestionArrayList) {

                        // 投稿された質問のUIDがFirebaseから取得した質問のUIDと一緒なら
                        if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                            // このアプリで変更がある可能性があるのは回答(Answer)のみ
                            question.getAnswers().clear();
                            // もう一度回答を設定
                            HashMap answerMap = (HashMap) map.get("answers");
                            if (answerMap != null) {
                                for (Object key : answerMap.keySet()) {
                                    HashMap temp = (HashMap) answerMap.get((String) key);
                                    String answerBody = (String) temp.get("body");
                                    String answerName = (String) temp.get("name");
                                    String answerUid = (String) temp.get("uid");
                                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                                    question.getAnswers().add(answer);
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
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

    // データに追加・変化があった時に受け取るリスナー
    private ChildEventListener mEventListener3 = new ChildEventListener() {
        @Override
        // 要素（質問）が追加されたとき呼ばれるメソッド
        // QuestionSendActivityで質問が追加されると呼ばれる
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            for (Favorite favorite : mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getQuestionUid())) {

                    // 追加されたデータをmapに取得
                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // そのmapから各情報を取得
                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String uid = (String) map.get("uid");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {  // 回答があれば
                        //
                        for (Object key : answerMap.keySet()) {
                            // Firebaseから取得した回答のUIDの回答をtempに取得
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    // QuestionsListAdapterにデータを設定
                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), 3, bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        // 要素（質問）に変化があった時受けるリスナー
        // ここでは質問に対して回答が投稿された時に呼ばれる
        // このメソッドが呼ばれたら変化があった質問に対応するQuestionクラスのインスタンスが保持している回答のArrayListを一旦クリアし、取得した回答を設定
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            for (Favorite favorite:mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getQuestionUid())) {

                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // 変更があったQuestionを探す
                    for (Question question : mQuestionArrayList) {

                        // 投稿された質問のUIDがFirebaseから取得した質問のUIDと一緒なら
                        if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                            // このアプリで変更がある可能性があるのは回答(Answer)のみ
                            question.getAnswers().clear();
                            // もう一度回答を設定
                            HashMap answerMap = (HashMap) map.get("answers");
                            if (answerMap != null) {
                                for (Object key : answerMap.keySet()) {
                                    HashMap temp = (HashMap) answerMap.get((String) key);
                                    String answerBody = (String) temp.get("body");
                                    String answerName = (String) temp.get("name");
                                    String answerUid = (String) temp.get("uid");
                                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                                    question.getAnswers().add(answer);
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
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

    // データに追加・変化があった時に受け取るリスナー
    private ChildEventListener mEventListener4 = new ChildEventListener() {
        @Override
        // 要素（質問）が追加されたとき呼ばれるメソッド
        // QuestionSendActivityで質問が追加されると呼ばれる
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            for (Favorite favorite:mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getQuestionUid())) {

                    // 追加されたデータをmapに取得
                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // そのmapから各情報を取得
                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String uid = (String) map.get("uid");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {  // 回答があれば
                        //
                        for (Object key : answerMap.keySet()) {
                            // Firebaseから取得した回答のUIDの回答をtempに取得
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    // QuestionsListAdapterにデータを設定
                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), 4, bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        // 要素（質問）に変化があった時受けるリスナー
        // ここでは質問に対して回答が投稿された時に呼ばれる
        // このメソッドが呼ばれたら変化があった質問に対応するQuestionクラスのインスタンスが保持している回答のArrayListを一旦クリアし、取得した回答を設定
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            for (Favorite favorite:mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getQuestionUid())) {

                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // 変更があったQuestionを探す
                    for (Question question : mQuestionArrayList) {

                        // 投稿された質問のUIDがFirebaseから取得した質問のUIDと一緒なら
                        if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                            // このアプリで変更がある可能性があるのは回答(Answer)のみ
                            question.getAnswers().clear();
                            // もう一度回答を設定
                            HashMap answerMap = (HashMap) map.get("answers");
                            if (answerMap != null) {
                                for (Object key : answerMap.keySet()) {
                                    HashMap temp = (HashMap) answerMap.get((String) key);
                                    String answerBody = (String) temp.get("body");
                                    String answerName = (String) temp.get("name");
                                    String answerUid = (String) temp.get("uid");
                                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                                    question.getAnswers().add(answer);
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
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
        setContentView(R.layout.activity_favorite_question_list);
        setTitle("お気に入り");

        // Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mGenreRef1 = mDatabaseReference.child(Const.ContentsPATH).child("1");
        mGenreRef2 = mDatabaseReference.child(Const.ContentsPATH).child("2");
        mGenreRef3 = mDatabaseReference.child(Const.ContentsPATH).child("3");
        mGenreRef4 = mDatabaseReference.child(Const.ContentsPATH).child("4");

        mGenreRef1.addChildEventListener(mEventListener1);
        mGenreRef2.addChildEventListener(mEventListener2);
        mGenreRef3.addChildEventListener(mEventListener3);
        mGenreRef4.addChildEventListener(mEventListener4);

        mFavoriteRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid());
        mFavoriteRef.addChildEventListener(mFavoriteEventListener);

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mFavoriteArrayList = new ArrayList<Favorite>();
        mAdapter.notifyDataSetChanged();

        mQuestionArrayList.clear();
        mFavoriteArrayList.clear();
        // アダプターにmQuestionArrayList（データ）をセットする
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        // 質問のmListView用のアダプタに渡す
        mListView.setAdapter(mAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                ///////
                intent.putExtra("favorite", mFavoriteArrayList);
                ///////
                startActivity(intent);
            }
        });
    }
}