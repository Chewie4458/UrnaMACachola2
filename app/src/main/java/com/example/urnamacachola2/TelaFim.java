package com.example.urnamacachola2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TelaFim extends AppCompatActivity {
    public String status;
    private boolean telaInicialIniciada = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_fim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Mantém a tela ativa
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /* Trecho de código para deixar em tela cheia */
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        /* Trecho de código para deixar em tela cheia */

        // Configurar o OnBackPressedCallback
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Faz nada (bloqueia a ação de voltar)
            }
        };

        // Adiciona o callback ao dispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);

        DatabaseReference presenceRef = FirebaseDatabase.getInstance()
                .getReference("appControle");

        DatabaseReference votanteAtivo = FirebaseDatabase.getInstance()
                .getReference("votanteAtivo");

        // Toca som fim
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.som_fim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mp.start();
            }
        }, 2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                presenceRef.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot snapshot) {
//                        Long lastUpdated = snapshot.child("last_updated").getValue(Long.class);
//                        String status = snapshot.child("status").getValue().toString();
//
//                        if (lastUpdated != null) {
//                            long currentTime = System.currentTimeMillis();
//                            long timeDifference = currentTime - lastUpdated;
//
//                            if (timeDifference > 10000 || status.equals("offline")) { // 10 segundos de tolerância
//                                status = "offline";
//                            } else {
//                                status = "online";
//
//                                if (!telaInicialIniciada) {
//                                    telaInicialIniciada = true; // Evita múltiplas execuções
//                                    Intent intent = new Intent(TelaFim.this, TelaInicial.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//                        System.err.println("Erro: " + error.getMessage());
//                    }
//                });
                votanteAtivo.child("nome").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue().toString().equals("")) {
                            Intent intent = new Intent(TelaFim.this, TelaInicial.class);
                            startActivity(intent);

                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle possible errors here
                        System.out.println("Error updating something hehe: " + databaseError.getMessage());
                    }
                });
            }
        }, 10000);
    }
}