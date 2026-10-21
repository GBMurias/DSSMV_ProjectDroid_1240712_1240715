package com.example.projectdroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectdroid.R;

import service.ServiceTest;

public class MainActivity extends AppCompatActivity {

    private Button meuBotao;
    private TextView txtResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        meuBotao = findViewById(R.id.meuBotao);
        txtResultado = findViewById(R.id.txtResultado);


        meuBotao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        ServiceTest service = new ServiceTest();
                        final String dadosJson = service.fazerPedido();


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtResultado.setText(dadosJson);
                            }
                        });
                    }
                }).start();

            }
        });
    }
}
