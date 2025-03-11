package com.wadyjorge.receitix.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.wadyjorge.receitix.R;

public final class DialogUtils {

    private static final int ICONE_AVISO = android.R.drawable.ic_dialog_info;
    private static final int ICONE_CONFIRMACAO = android.R.drawable.ic_dialog_alert;

    private static final int COR_AVISO = R.color.amarelo;
    private static final int COR_CONFIRMACAO = R.color.vermelho;

    private DialogUtils() {
    }

    public static void mostrarAviso(@NonNull Context context, int mensagemId) {
        mostrarAviso(context, context.getString(mensagemId), null);
    }

    public static void mostrarAviso(@NonNull Context context, @NonNull String mensagem,
                                    @Nullable DialogInterface.OnClickListener listener) {
        criarDialogo(context, R.string.aviso, ICONE_AVISO, mensagem, R.string.ok, listener, null, null).show();
    }

    public static void confirmarAcao(@NonNull Context context, int mensagemId,
                                     @Nullable DialogInterface.OnClickListener listenerSim,
                                     @Nullable DialogInterface.OnClickListener listenerNao) {
        confirmarAcao(context, context.getString(mensagemId), listenerSim, listenerNao);
    }

    public static void confirmarAcao(@NonNull Context context, @NonNull String mensagem,
                                     @Nullable DialogInterface.OnClickListener listenerSim,
                                     @Nullable DialogInterface.OnClickListener listenerNao) {
        criarDialogo(context, R.string.confirmacao, ICONE_CONFIRMACAO, mensagem, R.string.sim, listenerSim, R.string.nao, listenerNao).show();
    }

    private static AlertDialog criarDialogo(@NonNull Context context, int tituloId, int iconeId,
                                            @NonNull String mensagem, int textoBotaoSim,
                                            @Nullable DialogInterface.OnClickListener listenerSim,
                                            @Nullable Integer textoBotaoNao,
                                            @Nullable DialogInterface.OnClickListener listenerNao) {

        int corIcone = (iconeId == ICONE_AVISO) ? COR_AVISO : COR_CONFIRMACAO;
        Drawable icone = aplicarCorAoIcone(context, iconeId, corIcone);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(tituloId)
                .setIcon(icone)
                .setMessage(mensagem)
                .setPositiveButton(textoBotaoSim, listenerSim);

        if (textoBotaoNao != null) {
            builder.setNegativeButton(textoBotaoNao, listenerNao);
        }

        return builder.create();
    }

    private static Drawable aplicarCorAoIcone(Context context, int iconeId, int cor) {
        Drawable icone = ContextCompat.getDrawable(context, iconeId);
        if (icone != null) {
            icone.mutate().setTint(ContextCompat.getColor(context, cor));
        }
        return icone;
    }
}
