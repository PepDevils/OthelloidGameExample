package com.androidgames.othelloid;

/**
 * Created by ambs on 30/09/14.
 */
public class Posicao {
    public int x;
    public int y;

    public Posicao(int x, int y) {
        this.x= x;
        this.y= y;
    }

    public Posicao(Posicao p) {
        this.x = p.x;
        this.y = p.y;
    }

    public void Incrementa(Posicao p) {
        this.x += p.x;
        this.y += p.y;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Posicao))return false;
        Posicao o = (Posicao)other;
        return o.x == this.x && o.y == this.y;
    }

}