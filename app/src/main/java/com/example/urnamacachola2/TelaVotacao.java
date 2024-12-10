package com.example.urnamacachola2;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

public class TelaVotacao extends AppCompatActivity {
    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    public Integer idCategoria = 1;
    public String votandoAgora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_votacao);
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

        // Declarações
        ImageButton btnConfirmar = findViewById(R.id.btnConfirmar);
        ImageButton btnReiniciar = findViewById(R.id.btnReiniciar);
        ImageButton btnBranco    = findViewById(R.id.btnBranco);

        TextView txtVotandoAgora = findViewById(R.id.txtVotandoAgora);
        TextView txtCategoria    = findViewById(R.id.txtCategoria);
        TextView txtSelecionado  = findViewById(R.id.txtSelecionado);
        TextView txtNum1         = findViewById(R.id.txtNum1);
        TextView txtNum2         = findViewById(R.id.txtNum2);

        DatabaseReference votanteAtivo = referencia.child("votanteAtivo");
        DatabaseReference categorias   = referencia.child("categorias");
        DatabaseReference votantes     = referencia.child("votantes");

        // Controle de votante ativo
        votanteAtivo.child("nome").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("")) {
//                    Toast.makeText(TelaVotacao.this, "Ei ei! Alguém mexeu no banco???",
//                            Toast.LENGTH_LONG).show();

                    txtVotandoAgora.setText("");

                    Intent intent = new Intent(TelaVotacao.this, TelaInicial.class);
                } else {
                    txtVotandoAgora.setText("VOTANDO AGORA: " + dataSnapshot.getValue().toString());
                    votandoAgora = dataSnapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors here
                System.out.println("Error updating something hehe: " + databaseError.getMessage());
            }
        });

        // Carrega a primeira categoria
        categorias.child(String.valueOf(idCategoria))
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Obtém o nome da categoria
                        String nomeCategoria = dataSnapshot.child("nome").getValue(String.class);
                        txtCategoria.setText(nomeCategoria); // Atualiza o TextView
                    } else {
                        txtCategoria.setText("Nenhuma categoria configurada.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    txtCategoria.setText("Erro ao buscar categoria");
                    Log.e("FirebaseError", databaseError.getMessage());
                }
            });

        // Listener para o 2ª número - carrega o indicado
        txtNum2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Ação antes de o texto mudar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Ação durante a alteração do texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Ação após o texto mudar
                if (!s.toString().isEmpty()) {
                    // Recupera o número digitado
                    String numIndicado = txtNum1.getText().toString() + txtNum2.getText().toString();

                    // Busca o indicado no banco
                    categorias.child(String.valueOf(idCategoria)).child("indicados").child(numIndicado)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Obtém o nome do indicado
                                    String nomeIndicado = dataSnapshot.child("nome").getValue(String.class);

                                    // Mostra o selecionado
                                    txtSelecionado.setText(nomeIndicado);
                                } else {
                                    // Não encontrou o indicado
                                    txtSelecionado.setText("Número inválido.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                txtCategoria.setText("Erro ao buscar categoria");
                                Log.e("FirebaseError", databaseError.getMessage());
                            }
                        });
                }
            }
        });

        /*** Botões ***/
        // Botão Confirmar - registra o voto e passa para próxima categoria
        btnConfirmar.setOnClickListener(view -> {
            // Verifica se foi seleciona um indicado válido
            if (txtSelecionado.getText().toString().equals("") || txtSelecionado.getText().toString().equals("Número inválido.")) {
                Toast.makeText(TelaVotacao.this, "Selecione um indicado válido.",
                        Toast.LENGTH_LONG).show();
            } else {
                // Verifica se é voto em branco
                if (!(txtSelecionado.getText().toString().equals("VOTO EM BRANCO"))) {
                    // Recupera o número digitado
                    String numIndicado = txtNum1.getText().toString() + txtNum2.getText().toString();

                    // Registra o voto
                    DatabaseReference votosRef = categorias.child(String.valueOf(idCategoria)).child("indicados").child(numIndicado).child("qtdeVotos");
                    votosRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer currentVotes = currentData.getValue(Integer.class);
                            if (currentVotes == null) {
                                currentData.setValue(1); // Inicia com 1 se ainda não houver votos
                            } else {
                                currentData.setValue(currentVotes + 1); // Incrementa os votos
                            }
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                        }
                    });
                }

                // Vai para próxima categoria - incrementa o id
                idCategoria++;

                // Busca próxima categoria no banco
                categorias.child(String.valueOf(idCategoria))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Obtém o nome da categoria
                                String nomeCategoria = dataSnapshot.child("nome").getValue(String.class);
                                txtCategoria.setText(nomeCategoria); // Atualiza o TextView

                                // Limpa campos
                                txtNum1.setText("");
                                txtNum2.setText("");

                                txtSelecionado.setText("");
                            // Se não existe o id será considerado que acabou a votação
                            } else {
                                // Registra que votou
                                votantes.child(votandoAgora).child("votou").setValue(true);

                                // Vai para tela final
                                Intent intent = new Intent(TelaVotacao.this, TelaFim.class);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            txtCategoria.setText("Erro ao buscar categoria");
                            Log.e("FirebaseError", databaseError.getMessage());
                        }
                    });
            }
        });

        // Botão Branco - não registra voto
        btnBranco.setOnClickListener(view -> {
            txtNum1.setText("");
            txtNum2.setText("");

            txtSelecionado.setText("VOTO EM BRANCO");
        });

        // Botão Reiniciar - limpa os números
        btnReiniciar.setOnClickListener(view -> {
            txtNum1.setText("");
            txtNum2.setText("");

            txtSelecionado.setText("");
        });
    }

    // Numpad
    public void onNumpadClick(View view) {
        TextView txtNum1 = findViewById(R.id.txtNum1);
        TextView txtNum2 = findViewById(R.id.txtNum2);

        // Verifica o ID do botão clicado
        if (view.getId() == R.id.btn0) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("0");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("0");
            }
        } else if (view.getId() == R.id.btn1) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("1");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("1");
            }
        } else if (view.getId() == R.id.btn2) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("2");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("2");
            }
        } else if (view.getId() == R.id.btn3) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("3");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("3");
            }
        } else if (view.getId() == R.id.btn4) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("4");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("4");
            }
        } else if (view.getId() == R.id.btn5) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("5");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("5");
            }
        } else if (view.getId() == R.id.btn6) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("6");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("6");
            }
        } else if (view.getId() == R.id.btn7) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("7");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("7");
            }
        } else if (view.getId() == R.id.btn8) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("8");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("8");
            }
        } else if (view.getId() == R.id.btn9) {
            // Verifica se o primeiro número está preenchido
            if (txtNum1.getText().toString().equals("")) {
                txtNum1.setText("9");
                // Verifica se o segundo número está preenchido
            } else if (txtNum2.getText().toString().equals("")){
                txtNum2.setText("9");
            }
        }
    }
}