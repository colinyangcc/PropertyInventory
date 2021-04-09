package com.example.agc_inventory.tools;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import com.example.agc_inventory.R;

public class Sound {
    private static final int SOUND_COUNT = 2; // 預計要載入的聲音數量
    private SoundPool sp = null; // 宣告SoundPool物件
    private int[] nSoundId = new int[SOUND_COUNT];  // 存放音效ID的陣列
    private int CurrentSound = 0;
    private int Volume = 0;

    public Sound(Context context) {
        // 建立SoundPool物件
        sp = new SoundPool(SOUND_COUNT,               // 要用SoundPool來管理多少個音效
                AudioManager.STREAM_MUSIC, // 聲音類型, 一般為STREAM_MUSIC
                0);                        // 聲音品質, 0 = 預設品質

        // 初始化音效檔
        nSoundId[0] = sp.load(context,         // 要使用SoundPool的Context, 也就是目前的Activity
                R.raw.barcodebeep, // 音效資源
                1);           // 優先權, 目前無作用, 設定1來保持未來的相容性
    }

    public int GetVolume() { return Volume;}

    public void Play() {
        if (CurrentSound == 0)
            CurrentSound = sp.play(nSoundId[0],Int2Float(Volume),Int2Float(Volume),0,-1,Int2Float(Volume) + 1.0f);
    }

    public void Play(int volume) {
        if(volume > 100)
            Volume = 100;
        else if (volume < 0)
            Volume = 0;
        else
            Volume = volume;

        if (CurrentSound == 0)
            CurrentSound = sp.play(nSoundId[0],Int2Float(Volume),Int2Float(Volume),0,-1,Int2Float(Volume) + 1.0f);
        else {
            sp.setVolume(CurrentSound,Int2Float(Volume),Int2Float(Volume));
            sp.setRate(CurrentSound,Int2Float(Volume) + 1.0f);
        }
    }

    public void Stop() {
        if (CurrentSound != 0) {
            sp.stop(CurrentSound);
            CurrentSound = 0;
        }
    }

    public int VolumeUp() {
        if (Volume < 100) {
            Volume += 10;
            sp.setVolume(CurrentSound,Int2Float(Volume),Int2Float(Volume));
        }
        return Volume;
    }

    public int VolumeDown() {
        if (Volume > 0) {
            Volume -= 10;
            sp.setVolume(CurrentSound,Int2Float(Volume),Int2Float(Volume));
        }
        return Volume;
    }

    private float Int2Float(int source) {
        float result = (float)source / 100;
        return result;
    }
}
