package com.androidgames.othelloid;

import java.util.ArrayList;

/**
 * Created by ambs on 30/09/14.
 */
public class Tabuleiro {

    public static final int COLS = 8;
    public static final int LINS = 8;

    public static final int VAZIA  = 0;
    public static final int BRANCA = 1;
    public static final int PRETA  = 2;

    private static ArrayList<Posicao> cantos;
    private static ArrayList<Posicao> quaseCantos;

    static {
        cantos = new ArrayList<Posicao>();
        quaseCantos = new ArrayList<Posicao>();

        cantos.add(new Posicao(0, 0));
        cantos.add(new Posicao(0, COLS - 1));
        cantos.add(new Posicao(LINS - 1, 0));
        cantos.add(new Posicao(LINS - 1, COLS - 1));

        quaseCantos.add(new Posicao(0, 1));
        quaseCantos.add(new Posicao(0, COLS - 2));

        quaseCantos.add(new Posicao(1, 0));
        quaseCantos.add(new Posicao(1, 1));
        quaseCantos.add(new Posicao(1, COLS - 1));
        quaseCantos.add(new Posicao(1, COLS - 2));

        quaseCantos.add(new Posicao(LINS - 2, 0));
        quaseCantos.add(new Posicao(LINS - 2, 1));
        quaseCantos.add(new Posicao(LINS - 2, COLS - 1));
        quaseCantos.add(new Posicao(LINS - 2, COLS - 2));

        quaseCantos.add(new Posicao(LINS - 1, 1));
        quaseCantos.add(new Posicao(LINS - 1, COLS - 2));
    }

    private int[][] tabuleiro;
    private int jogador;
    private int nrPosLivres;

    public Tabuleiro() {
        tabuleiro = new int[LINS][COLS];
        for (int linha = 0; linha < LINS; linha++) {
            for (int coluna = 0; coluna < COLS; coluna++) {
                tabuleiro[linha][coluna] = VAZIA;
            }
        }

        colocarPedra(3, 3, BRANCA);
        colocarPedra(4, 4, BRANCA);
        colocarPedra(3, 4, PRETA);
        colocarPedra(4, 3, PRETA);

        jogador = BRANCA;
        nrPosLivres = LINS * COLS - 4;
    }

    /**
     * Places a stone in a specific coordinate (row/column)
     *
     * @param row row where to place stone
     * @param col column where to place stone
     * @param player color of the stone
     */
    public void colocarPedra(int row, int col, int player) {
        tabuleiro[row][col] = player;
    }

    /**
     * Returns the stone color in a specific coordinate (or VAZIA)
     *
     * @param row row where to look for a stone
     * @param col column where to look for a stone
     * @return the color of the stone (or VAZIA)
     */
    public int obterPedra(int row, int col) {
        return tabuleiro[row][col];
    }

    /**
     * Checks if the game status is game over (filled with stones or a jogador without stones.
     *
     * @return boolean value stating if game is on game over.
     */
    public boolean FimDeJogo() {
        return nrPosLivres == 0 || (jogadas(BRANCA).size() == 0 && jogadas(PRETA).size() == 0);
    }

    public int jogadorAtual() {
        return this.jogador;
    }

    public boolean Vazia(int row, int col) {
        return tabuleiro[row][col] == VAZIA;
    }

    public boolean Branca(int row, int col) {
        return tabuleiro[row][col] == BRANCA;
    }

    public void alternaJogador() {
        this.jogador = this.jogador == 1 ? 2 : 1;
    }




    /***************************************************/

    // Explicar este código é que vai ser do catano.
    public ArrayList<Posicao> jogadas(int player) {
        ArrayList<Posicao> possiveisJogadas = new ArrayList<Posicao>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (!Vazia(x, y)) continue;

                boolean paraSair = false;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;

                        Posicao limit = limita(new Posicao(x, y), new Posicao(i, j), player);

                        if (limit != null && tabuleiro[x + i][y + j] != player) {
                            possiveisJogadas.add(new Posicao(x, y));
                            paraSair = true;
                        }

                        if (paraSair) break;
                    }
                    if (paraSair) break;
                }
            }
        }
        return possiveisJogadas;
    }


    // retorna limite de x,y na direcao ix,iy
    private Posicao limita(Posicao origin, Posicao direction, int player) {
        Posicao pos = new Posicao(origin);
        pos.Incrementa(direction);

        while (pos.x >= 0 && pos.y >= 0 && pos.x < 8 && pos.y < 8 && !this.Vazia(pos.x, pos.y)) {
            if (this.tabuleiro[pos.x][pos.y] == player)
                return pos;
            pos.Incrementa(direction);
        }
        return null;
    }

    /**
     * Checks if a move is valid
     * @param row row where to move
     * @param col columns where to move
     * @param player color of the stone to place
     * @return a boolean value, stating if the move if valid
     */
    public boolean jogadaValida(int row, int col, int player) {
        ArrayList<Posicao> moves = this.jogadas(player);

        return moves.contains(new Posicao(row, col));
    }


    /**
     * A homemade clone method.
     *
     * @return a new tabuleiro
     */
    private Tabuleiro duplica() {
        Tabuleiro novoTabuleiro = new Tabuleiro();

        novoTabuleiro.jogador = jogador;
        novoTabuleiro.nrPosLivres = nrPosLivres;
        novoTabuleiro.tabuleiro = new int[LINS][COLS];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                novoTabuleiro.tabuleiro[i][j] = tabuleiro[i][j];
            }
        }
        return novoTabuleiro;
    }






    private void atualizaDirecao(Posicao origem, Posicao destino, Posicao incremento, int jogador) {
        Posicao pos = new Posicao(origem);

        while (pos.x != destino.x || pos.y != destino.y) {
            this.tabuleiro[pos.x][pos.y] = jogador;
            pos.Incrementa(incremento);
        }
    }

    public Tabuleiro realizaJogada(int x, int y, int jogador) {
        Tabuleiro novoTabuleiro = duplica();

        novoTabuleiro.nrPosLivres--;
        novoTabuleiro.colocarPedra(x, y, jogador);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                Posicao limit = novoTabuleiro.limita(new Posicao(x, y), new Posicao(i, j), jogador);
                if (limit != null)
                    novoTabuleiro.atualizaDirecao(new Posicao(x, y), limit, new Posicao(i, j), jogador);
            }
        }

        novoTabuleiro.alternaJogador();
        return novoTabuleiro;
    }


    /*****************************************/

    public int contaPedras(int jogador) {
        int total = 0;
        for (int i = 0; i < LINS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (tabuleiro[i][j] == jogador) total++;
            }
        }
        return total;
    }

    public int contaCantos(int jogador) {
        int total = 0;
        for (Posicao p : Tabuleiro.cantos) {
            if (tabuleiro[p.x][p.y] == jogador) total++;
        }
        return total;
    }

    public int contaQuaseCantos(int jogador) {
        int total = 0;
        for (Posicao p : Tabuleiro.quaseCantos) {
            if (tabuleiro[p.x][p.y] == jogador) total++;
        }
        return total;
    }



    public int avalia(int jogador) {
        int outro = (jogador == BRANCA) ? PRETA : BRANCA;

        int contagemJog = contaPedras(jogador);
        int contagemAdv = contaPedras(outro);

        // Empate final, e ganho/perda de jogo
        if (FimDeJogo()) {
            if (contagemJog > contagemAdv)
                return 1000;
            else if (contagemAdv > contagemJog)
                return -1000;
            else
                return 0;
        }

        int cantosJog   = this.contaCantos(jogador);
        int cantosAdv = this.contaCantos(outro);

        int quaseCantosJog = contaQuaseCantos(jogador);
        int quaseCantosAdv = contaQuaseCantos(outro);

        // Cantos: valor entre -500 e 500
        int cantos = (cantosJog - cantosAdv) * 125;

        // Totalpeças: entre -100 e 100
        int totalPecas = 100 * (contagemJog - contagemAdv) / 64;

        // QuaseCantos: varia entre -100 e 100
        int quaseCantos = 100 * (quaseCantosJog - quaseCantosAdv) / 12;

        // Total varia entre -700 e 700
        return cantos + totalPecas + quaseCantos;
    }





}
