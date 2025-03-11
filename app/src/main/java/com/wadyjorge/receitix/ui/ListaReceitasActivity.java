package com.wadyjorge.receitix.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.wadyjorge.receitix.R;
import com.wadyjorge.receitix.adapter.ReceitaAdapter;
import com.wadyjorge.receitix.database.ReceitasDatabase;
import com.wadyjorge.receitix.model.Receita;
import com.wadyjorge.receitix.utils.DialogUtils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ListaReceitasActivity extends AppCompatActivity {

    // Constante para preferências
    public static final String ARQUIVO_PREFERENCIAS = "com.wadyjorge.receitix.PREFERENCIAS";
    public static final String KEY_ORDENACAO_CRESCENTE = "ORDENACAO_CRESCENTE";
    public static final String KEY_MODO_NOTURNO = "MODO_NOTURNO";
    public static final Boolean PADRAO_INICIAL_ORDENACAO_CRESCENTE = true;
    public static final Boolean PADRAO_INICIAL_MODO_NOTURNO = false;

    // Componentes da interface
    private ListView listViewReceitas;
    private List<Receita> listaReceitas;
    private ReceitaAdapter receitaAdapter;
    private MenuItem menuItemOrdenar;
    private MenuItem menuItemModoNoturno;

    // Variáveis de estado
    private boolean ordenacaoCrescente = PADRAO_INICIAL_ORDENACAO_CRESCENTE;
    private boolean modoNoturno = PADRAO_INICIAL_MODO_NOTURNO;
    private int posicaoSelecionada = -1;
    private ActionMode actionMode;
    private View itemSelecionado;
    private Drawable backgroundItem;

    // Executor (tarefas assíncronas)
    private Executor executor;

    // Ciclo de vida da Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_receitas);
        setTitle(R.string.lista_de_receitas);

        executor = Executors.newSingleThreadExecutor();
        inicializarComponentes();
        lerPreferencias();
        aplicarTema();
        configurarListeners();
    }

    // Inicializa os componentes da interface e configura o adaptador da lista
    private void inicializarComponentes() {

        listViewReceitas = findViewById(R.id.listViewReceitas);

        executor.execute(() -> {
            ReceitasDatabase database = ReceitasDatabase.getInstance(this);

            if (ordenacaoCrescente) {
                listaReceitas = database.receitaDao().queryAllAscending();
            } else {
                listaReceitas = database.receitaDao().queryAllDownward();
            }

            runOnUiThread(() -> {
                receitaAdapter = new ReceitaAdapter(this, listaReceitas);
                listViewReceitas.setAdapter(receitaAdapter);
            });
        });
    }

    // Lê as preferências armazenadas
    private void lerPreferencias() {
        SharedPreferences shared = getSharedPreferences(ListaReceitasActivity.ARQUIVO_PREFERENCIAS, Context.MODE_PRIVATE);
        ordenacaoCrescente = shared.getBoolean(KEY_ORDENACAO_CRESCENTE, ordenacaoCrescente);
        modoNoturno = shared.getBoolean(KEY_MODO_NOTURNO, modoNoturno);
    }

    // Aplica o tema com base nas preferências
    private void aplicarTema() {

        if (modoNoturno) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Define os eventos de clique e clique longo para a lista
    private void configurarListeners() {
        listViewReceitas.setOnItemClickListener((parent, view, position, id) -> {
            posicaoSelecionada = position;
            editarReceita();
        });

        listViewReceitas.setOnItemLongClickListener((parent, view, position, id) -> {

            if (actionMode != null) {
                return false;
            }

            selecionarItem(view, position);
            actionMode = startSupportActionMode(actionCallback);
            return true;
        });
    }

    // Destaca visualmente um item selecionado e desativa interações na lista
    private void selecionarItem(View view, int position) {
        posicaoSelecionada = position;
        itemSelecionado = view;
        backgroundItem = view.getBackground();
        view.setBackgroundColor(getColor(R.color.corSelecao));
        listViewReceitas.setEnabled(false);
    }

    // Cria o menu de opções da ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lista_receitas_opcoes, menu);
        menuItemOrdenar = menu.findItem(R.id.menuItemOrdenar);
        menuItemModoNoturno = menu.findItem(R.id.menuItemModoNoturno);
        atualizarTextoModoNoturno();
        return true;
    }

    // Prepara o menu de opções para a exibição
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        atualizarIconeOrdenacao();
        return true;
    }

    // Lida com a seleção dos itens do menu de opções
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuItemAdicionar) {
            abrirCadastroReceita();
            return true;
        } else if (item.getItemId() == R.id.menuItemSobre) {
            abrirSobre();
            return true;
        } else if (item.getItemId() == R.id.menuItemOrdenar) {
            salvarConfigOrdenacaoCrescente(!ordenacaoCrescente);
            atualizarIconeOrdenacao();
            ordenarLista();
            return true;
        } else if (item.getItemId() == R.id.menuItemRestaurar) {
            confirmarRestaurarPadroes();
            return true;
        } else if (item.getItemId() == R.id.menuItemModoNoturno) {
            alternarModoNoturno();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Abre a tela de "Cadastro" de receita
    private void abrirCadastroReceita() {
        Intent intent = new Intent(this, CadastroReceitasActivity.class);
        intent.putExtra(CadastroReceitasActivity.KEY_MODO, CadastroReceitasActivity.MODO_CADASTRAR);
        launcherCadastroReceita.launch(intent);
    }

    // Ordena a lista de receitas com base na configuração de ordenação
    private void ordenarLista() {

        if (ordenacaoCrescente) {
            listaReceitas.sort(Receita.ordenacaoCrescente);
        } else {
            listaReceitas.sort(Receita.ordenacaoDecrescente);
        }
        receitaAdapter.notifyDataSetChanged();
    }

    // Atualiza o ícone do item de ordenação no menu
    private void atualizarIconeOrdenacao() {

        if (ordenacaoCrescente) {
            menuItemOrdenar.setIcon(R.drawable.ic_sort_alpha_asc);
        } else {
            menuItemOrdenar.setIcon(R.drawable.ic_sort_alpha_desc);
        }
    }

    // Alterna para o modo noturno
    private void alternarModoNoturno() {
        modoNoturno = !modoNoturno;
        salvarConfigModoNoturno(modoNoturno);
        aplicarTema();
        atualizarTextoModoNoturno();
    }

    // Atualiza o texto do item de modo noturno no menu
    private void atualizarTextoModoNoturno() {

        if (modoNoturno) {
            menuItemModoNoturno.setTitle(R.string.modo_claro);
        } else {
            menuItemModoNoturno.setTitle(R.string.modo_noturno);
        }
    }

    // Abre a tela "Sobre"
    private void abrirSobre() {
        startActivity(new Intent(this, SobreActivity.class));
    }

    // Abre a tela de "Edição" da receita selecionada
    private void editarReceita() {
        if (isPosicaoValida()) {
            Receita receita = listaReceitas.get(posicaoSelecionada);
            Intent intent = new Intent(this, CadastroReceitasActivity.class);
            intent.putExtra(CadastroReceitasActivity.KEY_MODO, CadastroReceitasActivity.MODO_EDITAR);
            intent.putExtra(CadastroReceitasActivity.KEY_ID, receita.getId());
            launcherEditarReceita.launch(intent);
        } else {
            exibirMensagemErro();
        }
    }

    // Exclui a receita selecionada
    private void excluirReceita() {
        if (isPosicaoValida()) {
            Receita receita = listaReceitas.get(posicaoSelecionada);
            String mensagem = getString(R.string.confirmar_excluir, receita.getNome());

            DialogInterface.OnClickListener listenerSim = (dialog, which) -> {

                executor.execute(() -> {
                    ReceitasDatabase database = ReceitasDatabase.getInstance(this);

                    int quantidadeAlterada = database.receitaDao().delete(receita);

                    if (quantidadeAlterada != 1) {
                        runOnUiThread(() -> DialogUtils.mostrarAviso(this, R.string.erro_ao_excluir));
                        return;
                    }

                    runOnUiThread(() -> {
                        listaReceitas.remove(posicaoSelecionada);
                        receitaAdapter.notifyDataSetChanged();
                        actionMode.finish();

                        ConstraintLayout constraintLayout = findViewById(R.id.main);
                        Snackbar.make(constraintLayout, R.string.receita_excluida, Snackbar.LENGTH_LONG).show();
                    });
                });
            };

            DialogUtils.confirmarAcao(this, mensagem, listenerSim, null);
        } else {
            exibirMensagemErro();
        }
    }

    // Verifica se a posição selecionada é válida
    private boolean isPosicaoValida() {
        return posicaoSelecionada >= 0 && posicaoSelecionada < listaReceitas.size();
    }

    // Exibe um alerta caso a posição selecionada seja inválida
    private void exibirMensagemErro() {
        Toast.makeText(this, R.string.erro_posicao_invalida, Toast.LENGTH_SHORT).show();
    }

    // Callback para gerenciar ações do ActionMode (menu contextual)
    private final ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.lista_receitas_item_selecionado, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            if (item.getItemId() == R.id.menuItemEditar) {
                editarReceita();
                return true;
            } else if (item.getItemId() == R.id.menuItemExcluir) {
                excluirReceita();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (itemSelecionado != null) {
                itemSelecionado.setBackground(backgroundItem);
            }

            actionMode = null;
            itemSelecionado = null;
            listViewReceitas.setEnabled(true);
        }
    };

    // Launchers para capturar o retorno das activities de cadastro e edição
    private final ActivityResultLauncher<Intent> launcherCadastroReceita = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::tratarResultadoCadastro);

    private final ActivityResultLauncher<Intent> launcherEditarReceita = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::tratarResultadoEdicao);

    // Processa o resultado do cadastro de receita
    private void tratarResultadoCadastro(ActivityResult result) {

        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Bundle bundle = result.getData().getExtras();

            if (bundle != null) {
                long id = bundle.getLong(CadastroReceitasActivity.KEY_ID);

                executor.execute(() -> {
                    ReceitasDatabase database = ReceitasDatabase.getInstance(this);
                    Receita receita = database.receitaDao().queryForId(id);

                    runOnUiThread(() -> {
                        listaReceitas.add(receita);
                        ordenarLista();

                        ConstraintLayout constraintLayout = findViewById(R.id.main);
                        Snackbar.make(constraintLayout, R.string.receita_cadastrada, Snackbar.LENGTH_LONG).show();
                    });
                });
            }
        }
    }

    // Processa o resultado da edição de receita
    private void tratarResultadoEdicao(ActivityResult result) {

        int resultCode = result.getResultCode();

        if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
            if (actionMode != null) {
                actionMode.finish();
            }
        }

        if (resultCode == RESULT_OK && result.getData() != null) {
            Bundle bundle = result.getData().getExtras();

            if (bundle == null || !isPosicaoValida()) {
                exibirMensagemErro();
                return;
            }

            final Receita receitaOriginal = listaReceitas.get(posicaoSelecionada);
            long id = bundle.getLong(CadastroReceitasActivity.KEY_ID);

            executor.execute(() -> {
                ReceitasDatabase database = ReceitasDatabase.getInstance(this);
                final Receita receitaEditada = database.receitaDao().queryForId(id);

                runOnUiThread(() -> {
                    listaReceitas.set(posicaoSelecionada, receitaEditada);
                    ordenarLista();

                    ConstraintLayout constraintLayout = findViewById(R.id.main);
                    Snackbar snackBar = Snackbar.make(constraintLayout, R.string.receita_editada, Snackbar.LENGTH_LONG);

                    snackBar.setAction(R.string.desfazer, v -> {
                        executor.execute(() -> {
                            int quantidadeAlterada = database.receitaDao().update(receitaOriginal);

                            runOnUiThread(() -> {
                                if (quantidadeAlterada != 1) {
                                    DialogUtils.mostrarAviso(this, R.string.erro_ao_editar);
                                    return;
                                }

                                listaReceitas.remove(receitaEditada);
                                listaReceitas.add(receitaOriginal);
                                ordenarLista();
                                posicaoSelecionada = -1;
                            });
                        });
                    });

                    snackBar.show();
                });
            });
        }
    }

    // Salva a configuração de ordenação crescente nas preferências
    private void salvarConfigOrdenacaoCrescente(boolean novoValor) {
        SharedPreferences shared = getSharedPreferences(ListaReceitasActivity.ARQUIVO_PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(KEY_ORDENACAO_CRESCENTE, novoValor);
        editor.apply();
        ordenacaoCrescente = novoValor;
    }

    // Salva a configuração de modo noturno nas preferências
    private void salvarConfigModoNoturno(boolean novoValor) {
        SharedPreferences shared = getSharedPreferences(ARQUIVO_PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(KEY_MODO_NOTURNO, novoValor);
        editor.apply();
    }

    // Exibe uma confirmação para restaurar as preferências padrões
    private void confirmarRestaurarPadroes() {
        DialogInterface.OnClickListener listenerSim = (dialog, which) -> {
            restaurarPadroes();
            atualizarIconeOrdenacao();
            ordenarLista();

            ConstraintLayout constraintLayout = findViewById(R.id.main);
            Snackbar.make(constraintLayout, R.string.padroes_restaurados, Snackbar.LENGTH_SHORT).show();
        };

        DialogUtils.confirmarAcao(this, R.string.confirmar_restaurar_padroes, listenerSim, null);
    }

    // Restaura as preferências para os valores padrão
    private void restaurarPadroes() {
        SharedPreferences shared = getSharedPreferences(ARQUIVO_PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.clear();
        editor.apply();

        ordenacaoCrescente = PADRAO_INICIAL_ORDENACAO_CRESCENTE;
        modoNoturno = PADRAO_INICIAL_MODO_NOTURNO;

        aplicarTema();
        atualizarTextoModoNoturno();
    }
}
