package niwigh.com.smartchat.Notifications;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import niwigh.com.smartchat.Model.Token;

public class FirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String refreshtoken = FirebaseInstanceId.getInstance().getToken();

        if(firebaseUser != null){
            updateToken(refreshtoken);
        }
    }

    private void updateToken(String refreshtoken) {
        try {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
            Token token = new Token(refreshtoken);
            if (firebaseUser != null) {
                reference.child(firebaseUser.getUid()).setValue(token);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}