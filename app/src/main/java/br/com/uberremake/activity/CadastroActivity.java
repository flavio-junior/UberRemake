package br.com.uberremake.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.uberremake.R;
import br.com.uberremake.config.ConfiguracaoFirebase;
import br.com.uberremake.helper.UsuarioFirebase;
import br.com.uberremake.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoNome, campoEmail, campoSenha;
    private SwitchMaterial switchTipoUsuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_cadastro);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_voltar);
            getSupportActionBar().setTitle(null);
        }

        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario);
    }

    public void validarCadastroUsuario(View view) {
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoNome.isEmpty()) {//verifica nome
            if (!textoEmail.isEmpty()) {//verifica e-mail
                if (!textoSenha.isEmpty()) {//verifica senha

                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);
                    usuario.setTipo(verificaTipoUsuario());

                    cadastrarUsuario(usuario);

                } else {
                    Toast.makeText(CadastroActivity.this,
                            "Preencha a senha!",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CadastroActivity.this,
                        "Preencha o email!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CadastroActivity.this,
                    "Preencha o nome!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void cadastrarUsuario(final Usuario usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                try {
                    String idUsuario = task.getResult().getUser().getUid();
                    usuario.setId(idUsuario);
                    usuario.salvar();

                    //Atualizar nome no UserProfile
                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                    // Redireciona o usuário com base no seu tipo
                    // Se o usuário for passageiro chama a activity maps
                    // senão chama a activity requisicoes
                    if (verificaTipoUsuario() == "P") {
                        startActivity(new Intent(CadastroActivity.this, PassageiroActivity.class));
                        finish();
                        Toast.makeText(CadastroActivity.this,
                                "Sucesso ao cadastrar Passageiro!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(CadastroActivity.this, RequisicoesActivity.class));
                        finish();
                        Toast.makeText(CadastroActivity.this,
                                "Sucesso ao cadastrar Motorista!",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String excecao = "";
                try {
                    throw task.getException();
                } catch (FirebaseAuthWeakPasswordException e) {
                    excecao = "Digite uma senha mais forte!";
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    excecao = "Por favor, digite um e-mail válido";
                } catch (FirebaseAuthUserCollisionException e) {
                    excecao = "Este conta já foi cadastrada";
                } catch (Exception e) {
                    excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                    e.printStackTrace();
                }
                Toast.makeText(CadastroActivity.this,
                        excecao,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String verificaTipoUsuario() {
        return switchTipoUsuario.isChecked() ? "M" : "P";
    }
}