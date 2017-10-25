package com.rrafols.packt.epg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.rrafols.packt.epg.data.Channel;
import com.rrafols.packt.epg.data.Program;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EPG epg = (EPG) findViewById(R.id.epg_view);
        epg.setCallback(new EPG.EPGCallback() {
            @Override
            public void programClicked(Channel channel, Program program) {
                Log.d("EPG", "program clicked: " + program.getName() + " channel: " + channel.getName());
            }
        });

        populateDummyChannelList(epg);
    }

    private static void populateDummyChannelList(EPG epg) {
        Channel[] channelList = new Channel[20];
        for (int i = 0; i < channelList.length; i++) {
            String iconURL = "https://raw.githubusercontent.com/googlei18n/noto-emoji/master/png/128/emoji_u1f3";

            if (i < 10) {
                iconURL += "0" + i;
            } else {
                iconURL += i;
            }
            iconURL += ".png";
            channelList[i] = new Channel("channel " + i, iconURL);

            Random rnd = new Random();
//            long time = System.currentTimeMillis(); // - 2 * 60 * 60 * 1000;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.HOUR, -2);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long time = calendar.getTimeInMillis();

            for (int j = 0; j < 100; j++ ) {
                long duration = (rnd.nextInt(7) + 1) * 15 * 60 * 1000;
                Program program = new Program("Program " + j,
                        "This will be the description of program " + j,
                        time,time + duration);

                channelList[i].addProgram(program);
                time += duration;
            }
        }

        epg.setChannelList(channelList);
    }
}
