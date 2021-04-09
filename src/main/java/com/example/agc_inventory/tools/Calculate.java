package com.example.agc_inventory.tools;

public class Calculate {

    private float a1,a2,b1,b2,c1;

    public Calculate(int max,int min,int Weighted ) {
        init(max,min,Weighted);
    }

    public void init(int max,int min,int Weighted ) {
        a1 = 100;
        a2 = max - min;
        b1 = -100 * min;
        b2 = max - min;
        c1 = Weighted;
    }

    public int GetResult(int input) {
        float output = (float)a1 * input / a2 + (float)b1 / b2 + c1;
        return (int)output;
    }
}
