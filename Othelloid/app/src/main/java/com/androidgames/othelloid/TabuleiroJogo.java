package com.androidgames.othelloid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ricardo on 10/09/2014.
 */
class TabuleiroJogo extends View {

    private Tabuleiro tabuleiro;

    private enum Estado {
        JOGADOR_A_JOGAR, IA_A_JOGAR, IA_A_PENSAR,
        JOGADOR_PASSA, IA_PASSA, FIM_DE_JOGO
    };
    private Estado estado;

    private Paint paint;
    private int dimensaoQuadrado;
    private int nrQuadrados = 8;
    private boolean modoVertical;
    private Posicao posBranco, posPreto;
    private Posicao ultimaJogada = null;

    private int profundidade;
    private Activity actividade = null;

    private Toast toastAtual = null;
    private ProgressDialog progresso = null;

    private AlertDialog.Builder builder = null;
    private AlertDialog myDialog = null;


    SharedPreferences sharedPref;

    /**
     *
     * @param widthMeasuredSpec
     * @param heightMeasuredSpec
     */
    @Override
    protected void onMeasure(int widthMeasuredSpec, int heightMeasuredSpec) {
        int width  = MeasureSpec.getSize(widthMeasuredSpec);
        int height = MeasureSpec.getSize(heightMeasuredSpec);
        int d = (width == 0) ? height : (height == 0) ? width
                : (width < height) ? width : height;
        dimensaoQuadrado = d / nrQuadrados;
        modoVertical = height > width;

        if (modoVertical) {
            posBranco = new Posicao((int)(dimensaoQuadrado *0.5),
                                   ((int)(dimensaoQuadrado *8.5)));
            posPreto  = new Posicao((int)(dimensaoQuadrado *0.5),
                                   ((int)(dimensaoQuadrado *9.5)));
        } else {
            posBranco = new Posicao((int)(dimensaoQuadrado *8.5),
                                   ((int)(dimensaoQuadrado *0.5)));
            posPreto =  new Posicao((int)(dimensaoQuadrado *8.5),
                                   ((int)(dimensaoQuadrado *1.5)));
        }

        setMeasuredDimension(width, height);
    }

    // FIXME: CHANGED THE SIGNATURE, as we need the activitity, it seems
    // ---------------------------------
    // FIXME: All dialog builders now use atividade instead of context!!!!
    public TabuleiroJogo(Activity atividade) {
        super(atividade);  // <-------- FIXME: changed

        this.actividade = atividade;   // <-------- FIXME: changed

        builder = new AlertDialog.Builder(this.actividade)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Othelloid")
                .setPositiveButton("OK", null);  // FIXME: assim executa o onDismiss

        // Obter instância do ficheiro de preferências
        sharedPref = PreferenceManager.getDefaultSharedPreferences(atividade);

        // Obter o nível de dificuldade
        profundidade = Integer.parseInt(sharedPref.getString("nivel_dificuldade","1"));

        // Obter a ordem de jogo
        if (sharedPref.getBoolean("ordem_jogar", true))
            estado = Estado.JOGADOR_A_JOGAR;
        else
            estado = Estado.IA_A_JOGAR;

        tabuleiro = new Tabuleiro();
        paint = new Paint();

        // Prepare a progresso indicator, so we do not create it every time
        progresso = new ProgressDialog(atividade);
        progresso.setMessage("A jogar...");
        progresso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progresso.getWindow().setGravity(Gravity.BOTTOM);
        progresso.setIndeterminate(true);
    }

    /**
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        ArrayList<Posicao> jogadasValidas = null;

        if (estado != Estado.IA_A_PENSAR) progresso.hide();

        if (estado == Estado.JOGADOR_A_JOGAR) {
            jogadasValidas = tabuleiro.jogadas(tabuleiro.jogadorAtual());
            if (jogadasValidas.size() == 0)
                estado = Estado.JOGADOR_PASSA;
        }

        paint.setColor(Color.GRAY);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        for (int linha = 0; linha < nrQuadrados; linha++) {
            for (int coluna = 0; coluna < nrQuadrados; coluna++) {
                int a = coluna * dimensaoQuadrado;
                int b = linha * dimensaoQuadrado;
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(3);
                canvas.drawRect(a, b, a + dimensaoQuadrado, b + dimensaoQuadrado, paint);
                paint.setStrokeWidth(0);
                paint.setColor(Color.rgb(123, 167, 123));
                canvas.drawRect(a+3, b+3, a + dimensaoQuadrado -3, b + dimensaoQuadrado -3, paint );

                if (! tabuleiro.Vazia(linha, coluna)){
                    paint.setColor( tabuleiro.Branca(linha, coluna) ? Color.WHITE : Color.BLACK);
                    canvas.drawCircle(a+(dimensaoQuadrado /2), b+(dimensaoQuadrado /2), dimensaoQuadrado * 0.45f, paint);
                }

                if (estado == Estado.JOGADOR_PASSA || estado == Estado.JOGADOR_A_JOGAR) {
                    Posicao p = new Posicao(linha, coluna);

                    if (jogadasValidas != null && jogadasValidas.contains(p)) {
                        paint.setARGB(128, 255, 225, 225);
                        canvas.drawCircle(a + (dimensaoQuadrado / 2), b + (dimensaoQuadrado / 2), dimensaoQuadrado * 0.20f, paint);
                    }

                    if (ultimaJogada != null && p.equals(ultimaJogada)) {
                        paint.setColor(Color.RED);
                        canvas.drawCircle(a + (dimensaoQuadrado / 2), b + (dimensaoQuadrado / 2), dimensaoQuadrado * 0.05f, paint);
                    }
                }
            }
        }

        ShowStats(canvas);

        switch (estado) {
            case JOGADOR_PASSA:
                HumanPass();
                break;
            case IA_PASSA:
                ComputerPass();
                break;
            case IA_A_JOGAR:
                ComputerPlay();
                break;
            case FIM_DE_JOGO:
                FinalDeJogo();
        }
    }

    /**
     *
     * @param canvas
     */
    private void ShowStats(Canvas canvas) {
        paint.setColor(Color.BLACK);
        canvas.drawCircle(posPreto.x, posPreto.y, dimensaoQuadrado * 0.3f, paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(posBranco.x, posBranco.y, dimensaoQuadrado * 0.3f, paint);

        paint.setTextSize(dimensaoQuadrado * 0.5f);

        canvas.drawText("Computador (" + tabuleiro.contaPedras(Tabuleiro.PRETA) + " pedras)",
                posPreto.x + dimensaoQuadrado * 0.5f, posPreto.y + dimensaoQuadrado * 0.15f, paint);
        canvas.drawText(sharedPref.getString("nome_jogador","Jogador 1") + " (" + tabuleiro.contaPedras(Tabuleiro.BRANCA) + " pedras)",
                posBranco.x + dimensaoQuadrado * 0.5f, posBranco.y + dimensaoQuadrado * 0.15f, paint);
    }


    /**
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (estado != Estado.JOGADOR_A_JOGAR)
            return false;

        int action = event.getAction();

        if (toastAtual != null)
            toastAtual.cancel();

        if (action == MotionEvent.ACTION_DOWN)
            return true;

        if (action == MotionEvent.ACTION_UP) {

            if (event.getX() > Tabuleiro.LINS * dimensaoQuadrado ||
                    event.getY() > Tabuleiro.COLS * dimensaoQuadrado)
                return false;

            int col = ((int) event.getX())/ dimensaoQuadrado;
            int row = ((int) event.getY())/ dimensaoQuadrado;

            if (! tabuleiro.jogadaValida(row, col, tabuleiro.jogadorAtual())) {
                toastAtual = Toast.makeText(getContext(), "Jogada Inválida!", Toast.LENGTH_SHORT);
                toastAtual.show();
            }
            else {
                ultimaJogada = null;
                tabuleiro = tabuleiro.realizaJogada(row, col, tabuleiro.jogadorAtual());
                if (tabuleiro.FimDeJogo())
                    estado = Estado.FIM_DE_JOGO;
                else
                    estado = Estado.IA_A_JOGAR;
                invalidate();
            }
            return true;
        }

        return false;
    }



    /**
     *
     */
    private void HumanPass() {
        tabuleiro.alternaJogador();
        AlertDialog d = builder.create();
        d.setMessage("Não tem jogadas disponíveis.");
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
               public void onDismiss(DialogInterface dialog) {
                    estado = Estado.IA_A_JOGAR;
                    postInvalidate();
                }
            });
        d.show();
    }

    /**
     *
     */
    private void ComputerPass() {
        AlertDialog d = builder.create();
        d.setMessage("Não posso jogar! Passo!");
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                estado = Estado.JOGADOR_A_JOGAR;
                postInvalidate();
            }
        });
        d.show();
    }


    /**
     *
     */
    private void ComputerPlay() {

        estado = Estado.IA_A_PENSAR;

        progresso.show();
        invalidate();

        new Thread(new Runnable() {
            public void run() {
                Posicao aiMove = Minimax.obterMelhorJogada(tabuleiro, tabuleiro.jogadorAtual(), profundidade);
                if (aiMove == null) {
                    tabuleiro.alternaJogador();
                    estado = Estado.IA_PASSA;

                } else {
                    ultimaJogada = new Posicao(aiMove);
                    tabuleiro = tabuleiro.realizaJogada(aiMove.x, aiMove.y, tabuleiro.jogadorAtual());
                    if (tabuleiro.FimDeJogo())
                        estado = Estado.FIM_DE_JOGO;
                    else
                        estado = Estado.JOGADOR_A_JOGAR;

                    postInvalidate();
                }
            }
        }).start();
    }


    // 8888888888888888888888888888888888888888888888888888888888888888888

    public Activity getActivity() {
        return this.actividade;
    }

    public AlertDialog getDialog() {
        return myDialog;
    }

    /**
     *
     */
    private void FinalDeJogo() {
        progresso.hide();

        int white = tabuleiro.contaPedras(Tabuleiro.BRANCA);
        int black = tabuleiro.contaPedras(Tabuleiro.PRETA);
        String message;
        if (white > black) {
            message = "Ganhaste! Parabéns!!";
        } else if (white == black) {
            message = "Empate! Foi um bom jogo!";
        } else {
            message = "Ganhei! Quando jogamos de novo?";
        }
        int pts = getPontuacao();
        message += "\nPontuação: " + pts;


        myDialog = builder.create();
        myDialog.setMessage(message);
        myDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                getActivity().finish();
            }
        });
        myDialog.show();

    }

     public int getPontuacao() {
        int pontos_jogador = tabuleiro.contaPedras(Tabuleiro.BRANCA);
        int pontos_ia      = tabuleiro.contaPedras(Tabuleiro.PRETA);
        int dificuldade    = profundidade;

        return dificuldade * (pontos_jogador - pontos_ia);
    }

    public boolean isFullBoard() {
        return tabuleiro.contaPedras(Tabuleiro.PRETA)== 0 ? true : false;
    }
}
