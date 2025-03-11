package com.wadyjorge.receitix.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wadyjorge.receitix.R;

public class SobreActivity extends AppCompatActivity {

    // Constantes para URLs e emails
    private static final String URL_UTFPR = "https://www.utfpr.edu.br";
    private static final String URL_AUTORIA = "https://github.com/WadyJorge";
    private static final String URL_CURSO = "https://pos-graduacao-ead.cp.utfpr.edu.br/java/";
    private static final String EMAIL_AUTOR = "wbeliche@live.com";

    // Ciclo de vida da Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);
        setTitle(R.string.sobre);
    }

    // Métodos de ação - Abrir sites
    public void abrirSiteUtfpr(View view) {
        abrirSite(URL_UTFPR);
    }

    public void abrirSiteAutoria(View view) {
        abrirSite(URL_AUTORIA);
    }

    public void abrirSiteCurso(View view) {
        abrirSite(URL_CURSO);
    }

    // Métodos de ação - Enviar email ao autor
    public void enviarEmailAutor(View view) {
        enviarEmail(new String[]{EMAIL_AUTOR}, getString(R.string.contato_aplicativo));
    }

    // Métodos auxiliares - Abrir um site
    private void abrirSite(String endereco) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(endereco));
        iniciarIntent(intent, R.string.nenhum_aplicativo_paginas_web);
    }

    // Métodos auxiliares - Enviar um email
    private void enviarEmail(String[] enderecos, String assunto) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, enderecos);
        intent.putExtra(Intent.EXTRA_SUBJECT, assunto);
        iniciarIntent(intent, R.string.nenhum_aplicativo_email);
    }

    // Métodos auxiliares - Iniciar uma intent com tratamento de erro
    private void iniciarIntent(Intent intent, int mensagemErro) {
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG).show();
        }
    }
}
