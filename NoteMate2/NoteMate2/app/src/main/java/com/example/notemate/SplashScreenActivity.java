package com.example.notemate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    // Waktu tunggu splash screen (3 detik agar logo terlihat jelas)
    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Menggunakan Looper.getMainLooper() untuk menjalankan intent setelah delay
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // PERUBAHAN DI SINI: Langsung pindah ke MainActivity karena LocationActivity sudah dihapus
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);

                // Menutup activity ini agar user tidak bisa kembali ke Splash Screen saat menekan tombol back
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}