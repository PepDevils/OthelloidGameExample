package com.androidgames.othelloid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class Jogo extends Activity {

    private TabuleiroJogo tabuleiroJogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabuleiroJogo = new TabuleiroJogo(this);
        setContentView(tabuleiroJogo);
    }

    @Override
    public void finish() {
        Intent responseIntent = new Intent();
        int pontuacao = tabuleiroJogo.getPontuacao();
        boolean fullboard = tabuleiroJogo.isFullBoard();
        responseIntent.putExtra("score", pontuacao);
        responseIntent.putExtra("fullboard", fullboard);
        setResult(RESULT_OK, responseIntent);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Terminar")
                .setMessage("Tem a certeza?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("Não", null).show();
    }


    public void onStop() {
        super.onStop();
        AlertDialog dialog = tabuleiroJogo.getDialog();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
