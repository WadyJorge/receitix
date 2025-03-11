package com.wadyjorge.receitix.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.wadyjorge.receitix.R;
import com.wadyjorge.receitix.database.ReceitasDatabase;
import com.wadyjorge.receitix.model.Receita;
import com.wadyjorge.receitix.utils.DialogUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CadastroReceitasActivity extends AppCompatActivity {

    // Constantes para as chaves de intent
    public static final String KEY_ID   = "ID";
    public static final String KEY_MODO = "MODO";
    public static final String KEY_SUGERIR_CATEGORIA = "SUGERIR_CATEGORIA";
    public static final String KEY_ULTIMA_CATEGORIA = "ULTIMA_CATEGORIA";

    // Constantes para os modos de operação
    public static final int MODO_CADASTRAR = 0;
    public static final int MODO_EDITAR = 1;

    // Componentes da interface
    private EditText editTextNomeReceita, editTextIngredientes, editTextModoPreparo;
    private RadioGroup radioGroupTempoPreparo;
    private RadioButton radioButtonMenosDe30Min, radioButtonDe30a60Min, radioButtonMaisDe1h;
    private Spinner spinnerCategoria;
    private CheckBox checkBoxFavorita;

    // Variáveis de estado
    private int modo;
    private Receita receitaOriginal;

    private boolean sugerirCategoria = false;
    private int ultimaCategoria = 0;

    // Executor (tarefas assíncronas)
    private Executor executor;

    // Ciclo de vida da Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_receitas);

        executor = Executors.newSingleThreadExecutor();
        inicializarComponentes();
        lerPreferencias();
        carregarDadosReceita();
    }

    // Inicializa os componentes da interface
    private void inicializarComponentes() {
        editTextNomeReceita = findViewById(R.id.editTextNomeReceita);
        editTextIngredientes = findViewById(R.id.editTextIngredientes);
        editTextModoPreparo = findViewById(R.id.editTextModoPreparo);
        radioGroupTempoPreparo = findViewById(R.id.radioGroupTempoPreparo);
        radioButtonMenosDe30Min = findViewById(R.id.radioButtonMenosDe30Min);
        radioButtonDe30a60Min = findViewById(R.id.radioButtonDe30a60Min);
        radioButtonMaisDe1h = findViewById(R.id.radioButtonMaisDe1h);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        checkBoxFavorita = findViewById(R.id.checkBoxFavorita);
    }

    // Lê as preferências armazenadas
    private void lerPreferencias() {
        SharedPreferences sharedPref = getSharedPreferences(ListaReceitasActivity.ARQUIVO_PREFERENCIAS, Context.MODE_PRIVATE);
        sugerirCategoria = sharedPref.getBoolean(KEY_SUGERIR_CATEGORIA, sugerirCategoria);
        ultimaCategoria = sharedPref.getInt(KEY_ULTIMA_CATEGORIA, ultimaCategoria);
    }

    // Carrega os dados da receita, se estiver no modo de edição
    private void carregarDadosReceita() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            modo = bundle.getInt(KEY_MODO);

            if (modo == MODO_CADASTRAR) {
                setTitle(R.string.cadastrar_receita);

                if (sugerirCategoria) {
                    spinnerCategoria.setSelection(ultimaCategoria);
                }
            } else {
                setTitle(R.string.editar_receita);

                long id = bundle.getLong(KEY_ID);

                executor.execute(() -> {
                    ReceitasDatabase database = ReceitasDatabase.getInstance(this);
                    receitaOriginal = database.receitaDao().queryForId(id);

                    if (receitaOriginal != null) {
                        runOnUiThread(() -> preencherCampos(receitaOriginal));
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, R.string.erro_receita_nao_encontrada, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
            }
        }
    }

    // Preenche os campos com os dados da receita
    private void preencherCampos(Receita receita) {
        editTextNomeReceita.setText(receitaOriginal.getNome());
        editTextIngredientes.setText(receitaOriginal.getIngredientes());
        editTextModoPreparo.setText(receitaOriginal.getModoPreparo());
        spinnerCategoria.setSelection(receitaOriginal.getCategoria());
        checkBoxFavorita.setChecked(receitaOriginal.isFavorita());

        String tempoPreparo = receitaOriginal.getTempoPreparo();

        if (tempoPreparo.equals(getString(R.string.menos_de_30_min))) {
            radioButtonMenosDe30Min.setChecked(true);
        } else if (tempoPreparo.equals(getString(R.string.de_30_min_1h))) {
            radioButtonDe30a60Min.setChecked(true);
        } else if (tempoPreparo.equals(getString(R.string.mais_de_1h))) {
            radioButtonMaisDe1h.setChecked(true);
        }
    }

    // Cria o menu de opções na ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cadastro_receitas_opcoes, menu);
        return true;
    }

    // Prepara o menu de opções
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menuItemSugerirCategoria);

        if (item != null) {
            item.setChecked(sugerirCategoria);
        }
        return true;
    }

    // Trata a seleção de itens do menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuItemSalvar) {
            salvarReceita();
            return true;
        } else if (item.getItemId() == R.id.menuItemLimpar) {
            limparCampos();
            return true;
        } else if (item.getItemId() == R.id.menuItemSugerirCategoria) {

            boolean valor = !item.isChecked();

            salvarSugerirCategoria(valor);
            item.setChecked(valor);

            if (sugerirCategoria) {
                spinnerCategoria.setSelection(ultimaCategoria);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Salva a receita
    private void salvarReceita() {

        if (!validarCampos()) {
            return;
        }

        Receita receitaAtual = criarReceitaAtual();

        if (modo == MODO_EDITAR && receitaAtual.equals(receitaOriginal)) {
            Toast.makeText(this, R.string.nenhuma_alteracao, Toast.LENGTH_SHORT).show();

            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        int categoria = spinnerCategoria.getSelectedItemPosition();
        salvarUltimaCategoria(categoria);

        Intent intent = new Intent();

        executor.execute(() -> {
            ReceitasDatabase database = ReceitasDatabase.getInstance(this);

            if (modo == MODO_CADASTRAR) {
                long novoId = database.receitaDao().insert(receitaAtual);

                if (novoId <= 0) {
                    runOnUiThread(() -> DialogUtils.mostrarAviso(this, R.string.erro_ao_cadastrar));
                    return;
                }

                receitaAtual.setId(novoId);
            } else {
                receitaAtual.setId(receitaOriginal.getId());

                int quantidadeAlterada = database.receitaDao().update(receitaAtual);

                if (quantidadeAlterada != 1) {
                    runOnUiThread(() -> DialogUtils.mostrarAviso(this, R.string.erro_ao_editar));
                    return;
                }
            }

            intent.putExtra(KEY_ID, receitaAtual.getId());
            runOnUiThread(() -> {
                setResult(RESULT_OK, intent);
                finish();
            });
        });
    }

    // Valida os campos do formulário
    private boolean validarCampos() {

        if (editTextNomeReceita.getText().toString().trim().isEmpty()) {
            DialogUtils.mostrarAviso(this, R.string.nome_obrigatorio);
            editTextNomeReceita.requestFocus();
            return false;
        }

        if (editTextIngredientes.getText().toString().trim().isEmpty()) {
            DialogUtils.mostrarAviso(this, R.string.ingredientes_obrigatorios);
            editTextIngredientes.requestFocus();
            return false;
        }

        if (editTextModoPreparo.getText().toString().trim().isEmpty()) {
            DialogUtils.mostrarAviso(this, R.string.modo_preparo_obrigatorio);
            editTextModoPreparo.requestFocus();
            return false;
        }

        if (radioGroupTempoPreparo.getCheckedRadioButtonId() == -1) {
            DialogUtils.mostrarAviso(this, R.string.tempo_preparo_obrigatorio);
            radioGroupTempoPreparo.requestFocus();
            return false;
        }

        if (spinnerCategoria.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            DialogUtils.mostrarAviso(this, R.string.categoria_obrigatoria);
            spinnerCategoria.requestFocus();
            return false;
        }

        return true;
    }

    // Cria um objeto Receita com os dados atuais do formulário
    private Receita criarReceitaAtual() {
        String nome = editTextNomeReceita.getText().toString().trim();
        String ingredientes = editTextIngredientes.getText().toString().trim();
        String modoPreparo = editTextModoPreparo.getText().toString().trim();
        int categoria = spinnerCategoria.getSelectedItemPosition();
        boolean favorita = checkBoxFavorita.isChecked();

        int radioId = radioGroupTempoPreparo.getCheckedRadioButtonId();
        String tempoPreparo = radioId == R.id.radioButtonMenosDe30Min ? getString(R.string.menos_de_30_min)
                : radioId == R.id.radioButtonDe30a60Min ? getString(R.string.de_30_min_1h)
                : getString(R.string.mais_de_1h);

        return new Receita(nome, ingredientes, modoPreparo, tempoPreparo, categoria, favorita);
    }

    // Limpa os campos do formulário
    private void limparCampos() {
        final String nome = editTextNomeReceita.getText().toString();
        final String ingredientes = editTextIngredientes.getText().toString();
        final String modoPreparo = editTextModoPreparo.getText().toString();
        final int tempoPreparo = radioGroupTempoPreparo.getCheckedRadioButtonId();
        final int categoria = spinnerCategoria.getSelectedItemPosition();
        final boolean favorita = checkBoxFavorita.isChecked();

        final ScrollView scrollView = findViewById(R.id.main);
        final View viewFoco = scrollView.findFocus();

        editTextNomeReceita.setText("");
        editTextIngredientes.setText("");
        editTextModoPreparo.setText("");
        radioGroupTempoPreparo.clearCheck();
        spinnerCategoria.setSelection(0);
        checkBoxFavorita.setChecked(false);
        editTextNomeReceita.requestFocus();

        Snackbar snackbar = Snackbar.make(scrollView, R.string.entradas_apagadas, Snackbar.LENGTH_LONG);

        snackbar.setAction(R.string.desfazer, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextNomeReceita.setText(nome);
                editTextIngredientes.setText(ingredientes);
                editTextModoPreparo.setText(modoPreparo);
                radioGroupTempoPreparo.check(tempoPreparo);
                spinnerCategoria.setSelection(categoria);
                checkBoxFavorita.setChecked(favorita);

                if (viewFoco != null) {
                    viewFoco.requestFocus();
                }
            }
        });

        snackbar.show();
    }

    // Salva a preferência de sugerir categoria
    private void salvarSugerirCategoria(boolean novoValor) {
        SharedPreferences shared = getSharedPreferences(ListaReceitasActivity.ARQUIVO_PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(KEY_SUGERIR_CATEGORIA, novoValor);
        editor.apply();
        sugerirCategoria = novoValor;
    }

    // Salva a última categoria selecionada
    private void salvarUltimaCategoria(int novoValor) {
        SharedPreferences shared = getSharedPreferences(ListaReceitasActivity.ARQUIVO_PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt(KEY_ULTIMA_CATEGORIA, novoValor);
        editor.apply();
        ultimaCategoria = novoValor;
    }
}
