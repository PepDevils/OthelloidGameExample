package com.androidgames.othelloid;

import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by ambs on 01/10/14.
 */
public final class Minimax {


    public static Posicao obterMelhorJogada(Tabuleiro tabuleiro, int jogador, int profundidadeMax) {
        Pair<Posicao, Integer> melhorJogadaPontuacao = minimax(tabuleiro, jogador, profundidadeMax, 0);
        return melhorJogadaPontuacao.first;
    }

    private static Pair<Posicao, Integer> minimax(Tabuleiro tabuleiro, int jogador, int profMax, int profAct) {
        if (tabuleiro.FimDeJogo() || profAct == profMax)
            return new Pair<Posicao, Integer>(null, tabuleiro.avalia(jogador));

        Posicao melhorJogada = null;
        int melhorPontuacao = 200000; // exaggerate

        if (tabuleiro.jogadorAtual() == jogador) melhorPontuacao = -melhorPontuacao;

        ArrayList<Posicao> jogadas = tabuleiro.jogadas(tabuleiro.jogadorAtual());

        if (jogadas.size() == 0)
            return new Pair<Posicao, Integer>(null, tabuleiro.avalia(jogador));

        Pair<Posicao, Integer> atual = null;
        for (Posicao p : jogadas) {
            Tabuleiro novoTab = tabuleiro.realizaJogada(p.x, p.y, tabuleiro.jogadorAtual());

            atual = Minimax.minimax(novoTab, jogador, profMax, profAct + 1);

            if (tabuleiro.jogadorAtual() == jogador) {
                if (atual.second > melhorPontuacao) {
                    melhorPontuacao = atual.second;
                    melhorJogada = p;
                }
            } else {
                if (atual.second < melhorPontuacao) {
                    melhorPontuacao = atual.second;
                    melhorJogada = p;
                }
            }
        }

        return new Pair<Posicao, Integer>(melhorJogada, melhorPontuacao);
    }
}
