package com.androidgames.othelloid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;



public class MainActivity extends Activity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final int REQUEST_PLAY = 1;
    private static final int REQUEST_LEADERBOARD = 2;
    private static final int REQUEST_ACHIEVEMENTS = 3;


    private GoogleApiClient myGoogleApiClient;
    private static int RC_SIGN_IN = 9001;

    SharedPreferences sharedPref;
    int nivel;

    private boolean myResolvingConnectionFailure = false;
    private boolean myAutoStartSignInFlow = true;
    private boolean myExplicitSignOut = false;
    private boolean mySignInClicked = false;
    private boolean myScore = false;
    private boolean myInSignInFlow = false; // set to true when you're in the middle of the
    // sign in flow, to know you should not attempt
    // to connect in onStart()

    private int score = 0;
    private boolean fullBoard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtém uma instância do ficheiro de preferências
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Obtém o nível de dificuldade
        nivel = Integer.parseInt(sharedPref.getString("nivel_dificuldade","1"));

        // Cria o Google Api Client com acesso aos serviços Plus e Games
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        // Associa um layout à atividade
        setContentView(R.layout.activity_main);

        //Adiciona listeners para lidar com os cliques nos botões sign-in e sign-out
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // Clique no item de menu Jogar
        ImageView imgJogar = (ImageView) findViewById(R.id.imgJogar);
        imgJogar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(MainActivity.this, Jogo.class), REQUEST_PLAY);
            }
        });

        // Clique no item de menu Opções
        ImageView imgOpcoes = (ImageView) findViewById(R.id.imgOpcoes);
        imgOpcoes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(
                        new Intent(MainActivity.this, Opcoes.class));
            }
        });

        // Clique no item de menu Rankings
        ImageView imgRanking = (ImageView) findViewById(R.id.imgRanking);
        imgRanking.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (myGoogleApiClient != null && myGoogleApiClient.isConnected()) {
                    try {
                        startActivityForResult(
                                Games.Leaderboards.getLeaderboardIntent(myGoogleApiClient, getString(R.string.leaderboard_ranking)),
                                REQUEST_LEADERBOARD);
                    } catch (SecurityException se) {
                        showMessage(se.getMessage());
                    }
                } else {
                    showMessage("Para aceder ao ranking autentique-se no Google+");
                }
            }
        });

        // Clique no item de menu Conquistas
        ImageView imgConquistas = (ImageView) findViewById(R.id.imgConquistas);
        imgConquistas.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (myGoogleApiClient != null && myGoogleApiClient.isConnected()) {
                    try {
                        startActivityForResult(
                                Games.Achievements.getAchievementsIntent(myGoogleApiClient), REQUEST_ACHIEVEMENTS);
                    } catch (SecurityException se) {
                        showMessage(se.getMessage());
                    }
                } else {
                    showMessage("Para aceder à lista de conquistas autentique-se no Google+");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!myInSignInFlow && !myExplicitSignOut) {
            // auto sign in
            myGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (myGoogleApiClient.isConnected()) {
            myGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Está conectado! Esconde o botão de sign-in e mostra o botão de sign-out
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

        // Se há resultados para submeter
        if(myScore) {
            // Submete score usando a LeaderBoard API
            Games.Leaderboards.submitScore(myGoogleApiClient, getString(R.string.leaderboard_ranking), score);

            // Submete conquista baseada no nível caso tenha ganho (pontuação superior a 0)
            if(score>0) {
                switch (nivel) {
                    case 1: Games.Achievements.unlock(myGoogleApiClient, getString(R.string.achievement_1)); break;
                    case 2: Games.Achievements.unlock(myGoogleApiClient, getString(R.string.achievement_2)); break;
                    case 3: Games.Achievements.unlock(myGoogleApiClient, getString(R.string.achievement_3)); break;
                    case 4: Games.Achievements.unlock(myGoogleApiClient, getString(R.string.achievement_4)); break;
                }
                // Submete conquista caso tenho ganho e ocupado o tabuleiro com todas as suas peças (full board)
                if(fullBoard) {
                    Games.Achievements.unlock(myGoogleApiClient, getString(R.string.achievement_5));
                    fullBoard = false;
                }
            }
            myScore = false;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (myResolvingConnectionFailure) {
            // already resolving
            return;
        }
        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mySignInClicked || myAutoStartSignInFlow) {
            myAutoStartSignInFlow = false;
            mySignInClicked = false;
            myResolvingConnectionFailure = true;
            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    myGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                myResolvingConnectionFailure = false;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Reconectar
        myGoogleApiClient.connect();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            // Inicia assíncronamente o fluxo do sign-in
            mySignInClicked = true;
            myGoogleApiClient.connect();
        }
        else if (view.getId() == R.id.sign_out_button) {
            // user explicitly signed out, so turn off auto sign in
            myExplicitSignOut = true;
            if (myGoogleApiClient != null && myGoogleApiClient.isConnected()) {
                Games.signOut(myGoogleApiClient);
                myGoogleApiClient.disconnect();
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK && requestCode == REQUEST_PLAY) {
            if(data.hasExtra("score")) {
                Bundle b = data.getExtras();
                score = b.getInt("score");
                fullBoard = b.getBoolean("fullboard");
                myScore = true;
                myGoogleApiClient.connect();
            }
        }
        if (requestCode == RC_SIGN_IN) {
            mySignInClicked = false;
            myResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                myGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }
    }

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Erro").setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
