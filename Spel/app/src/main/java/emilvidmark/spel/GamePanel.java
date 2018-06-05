package emilvidmark.spel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by emilv on 2017-07-15.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -20;
    private long missileStartTime;
    private long coinStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private Random rand = new Random();
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topBorder;
    private ArrayList<BotBorder> botBorder;
    private ArrayList<Coin> coins;
    private int maxBorderHeight;
    private int minBorderHeight;
    private int progressDenom = 20;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;
    private int coinPoints;

    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best = 0;


    public GamePanel(Context context)
    {
        super(context);

        //Add the callback to the surfaceholder to interupt events
        getHolder().addCallback(this);

        //Initialize thread
        thread = new MainThread(getHolder(), this);

        //Make gamepanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Stop the thread
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000){
            counter++;
            try{
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            }catch (InterruptedException e){e.printStackTrace();}

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.background));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.fagel), 71, 50, 8 );
        missiles = new ArrayList<Missile>();
        missileStartTime = System.nanoTime();
        coinStartTime = System.nanoTime(); //           <-----
        topBorder = new ArrayList<TopBorder>();
        botBorder = new ArrayList<BotBorder>();
        coins = new ArrayList<Coin>();
        // We can safely start the gameloop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(!player.getPlaying() && newGameCreated && reset){
                player.setPlaying(true);
                player.setUp(true);
            }
            if(player.getPlaying()){
                if(!started)started = true;
                reset = false;
                player.setUp(true);
            }
            else{
                player.setUp(true);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP){
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update(){
        if(player.getPlaying()){

            if(botBorder.isEmpty()){
                player.setPlaying(false);
                return;
            }
            if(topBorder.isEmpty()){
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

            //Check collision topborder

            for(int i = 0; i < topBorder.size();i++){
                if(collision(topBorder.get(i), player)){
                    player.setPlaying(false);
                }

            }

            //Collision bottom border
            for(int i = 0; i < botBorder.size();i++){
                if(collision(botBorder.get(i), player)){
                    player.setPlaying(false);
                }

            }

            //Update top border
           // this.updateTopBorder();

            //Update bottom border
           // this.updateBottomBorder();

            //add coins on timer




            //add  missiles on timer
            long missileElapsed = (System.nanoTime()-missileStartTime)/1000000;
            if(missileElapsed >(1000-player.getScore()/3)){
                if(missiles.size()==0){
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.misil), -10, HEIGHT/2, 29, 16, player.getScore(), 1));
                }else{
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.misil), -10, (int)((rand.nextDouble()*(HEIGHT))), 29, 16, player.getScore(), 1));
                }
                player.setScore(10);
                //reset timer.

                missileStartTime = System.nanoTime();

                //Loop through every missile

                }



            for (int i = 0; i < missiles.size();i++){
                missiles.get(i).update();

                //Check if collision with player.
                if(collision(missiles.get(i),player)){
                    missiles.remove(i);
                    player.setPlaying(false);
                    newGame();
                    break;
                }
                //Remove missile if it gets off the screen.
                if (missiles.get(i).getX()<-100){
                    missiles.remove(i);
                    break;
                }
            }


        }

        else{
            if(!reset){
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
            }
            long resetElapsed = (System.nanoTime()-startReset)/1000000;

            if(resetElapsed > 2500 && !newGameCreated){
                newGame();
            }
            if(!newGameCreated) {
                newGame();
            }
        }
    }
    public boolean collision(GameObject a, GameObject b){
        if(Rect.intersects(a.getRectangle(),b.getRectangle())){
            return true;
        }
        return false;

    }

    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas != null){
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);

            for(Missile m:missiles){
                m.draw(canvas);
            }

            //Draw top border
            for(TopBorder tb: topBorder){
                tb.draw(canvas);
            }
            //Draw bottom border
            for(BotBorder bb: botBorder){
                bb.draw(canvas);
            }
            for(Coin c:coins){
                c.draw(canvas);
            }
            drawText(canvas);

            canvas.restoreToCount(savedState);
        }

    }

    public void updateTopBorder(){
        topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), topBorder.get(topBorder.size()).getX()+20, 0, 2));

        for(int i = 0; i<topBorder.size();i++){
            topBorder.get(i).update();
            if(topBorder.get(i).getX()<-20){
                topBorder.remove(i);
                if(topBorder.get(topBorder.size()-1).getHeight()>=maxBorderHeight){
                    topDown = false;
                }
                if(topBorder.get(topBorder.size()-1).getHeight()<=minBorderHeight){
                    topDown = true;
                }
                if(topDown){
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), topBorder.get(topBorder.size()-1).getX()+20, 0, 0));
                }else{
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), topBorder.get(topBorder.size()-1).getX()+20, 0, 0));
                }
            }
        }
    }
    public void updateBottomBorder(){
        botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), botBorder.get(botBorder.size()-1).getX()+20, 0));
        for(int i = 0; i<botBorder.size();i++){
            botBorder.get(i).update();
            if(botBorder.get(i).getX()<-20){
                botBorder.remove(i);
                if(botBorder.get(botBorder.size()-1).getHeight()>=maxBorderHeight){
                    botDown = false;
                }
                if(botBorder.get(botBorder.size()-1).getHeight()<=minBorderHeight){
                    botDown = true;
                }
                if(botDown){
                    botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), botBorder.get(botBorder.size()-1).getX()+20, 0));
                }else{
                    botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), botBorder.get(botBorder.size()-1).getX()+20, 0));
                }
            }
        }
    }
    public void newGame(){

        dissapear = false;

        botBorder.clear();
        topBorder.clear();
        missiles.clear();
        coins.clear();

        minBorderHeight =5;
        maxBorderHeight = 30;

        player.setY(HEIGHT/2);

        if(player.getScore()>best){
            best = player.getScore();
        }
        player.resetScore();

        //Create initiall borders

        for(int i = 0; i*20<WIDTH+40;i++) {
            if (i == 0) {
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), i * 20, 0, 1));
            } else {
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), i * 20, 0, topBorder.get(i-1).getHeight()));
            }
        }

        for(int i = 0; i*20<WIDTH+40; i++){
            if(i==0){
                botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), i * 20, HEIGHT-minBorderHeight));

            }
            else{
                botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border), i * 20, botBorder.get(i-1).getY()));
            }
        }

        newGameCreated = true;
    }

    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("SCORE: " + player.getScore(), 10, HEIGHT-10, paint);
        canvas.drawText("BEST: " + best, WIDTH -215, HEIGHT - 10, paint);

        if(!player.getPlaying()&&newGameCreated&&reset){
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2-70, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-70, HEIGHT/2+20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-70, HEIGHT/2+40, paint1);
        }
    }
}
