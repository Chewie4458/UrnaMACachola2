package com.example.urnamacachola2;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.widget.PopupWindow;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TelaInicial extends AppCompatActivity {
    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_inicial);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* Trecho de código para deixar em tela cheia */
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        /* Trecho de código para deixar em tela cheia */

        // Declarações
        ImageButton btnConfirmar = findViewById(R.id.btnConfirmar);

        DatabaseReference votanteAtivo = referencia.child("votanteAtivo");

        // Post para garantir que o PopupWindow seja exibido após a janela estar pronta
        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                //mostraAviso(votanteAtivo);
                showModalDialog();
            }
        });

        // Ações
        btnConfirmar.setOnClickListener(view -> {
            Toast.makeText(TelaInicial.this, "Calma que não tem nada ainda",
                    Toast.LENGTH_LONG).show();
        });
    }

    public void mostraAviso(DatabaseReference votanteAtivo) {
        // Passo 1
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.aviso_bloqueio, null);

        TextView txtVotandoAgora = findViewById(R.id.txtVotandoAgora);

        // Passo 2
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = false; // Permitir que o popup seja fechado ao clicar fora
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.setOutsideTouchable(false);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Remove o fundo padrão

        // Controle de votante ativo
        votanteAtivo.child("nome").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("")) {
                    txtVotandoAgora.setText("");
                    popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
                } else {
                    txtVotandoAgora.setText("VOTANDO AGORA: " + dataSnapshot.getValue().toString());
                    popupWindow.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors here
                System.out.println("Error updating something hehe: " + databaseError.getMessage());
            }
        });
    }

    public void showModalDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.aviso_bloqueio);
        dialog.setCancelable(false); // Impede o fechamento ao clicar fora
        dialog.show();
    }
}