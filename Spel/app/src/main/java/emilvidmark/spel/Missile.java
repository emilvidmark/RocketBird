package emilvidmark.spel;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by emilv on 2017-07-16.
 */

public class Missile extends GameObject{
    private int score;
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spriteSheet;


    public Missile(Bitmap res, int x, int y, int w, int h, int s, int numFrames){
        super.x = x;
        super.y = y;

        width = w;
        height = h;
        score = s;

        speed = -10 - ((int)(rand.nextDouble()*score/30));

        //cap missile speed

        if(speed <-40-score*0.3)speed = (int) (-40-score*0.3);
        Bitmap[] image = new Bitmap[numFrames];

        spriteSheet = res;

        for(int i = 0; i <(image.length);i++) {
            image[i] = Bitmap.createBitmap(spriteSheet,0,i*height, width, height);

        }

        animation.setFrames(image);
        animation.setDelay(100+speed);
    }
    public void update(){
       x-=speed;
        animation.update();
    }
    public void draw(Canvas canvas){
        try{
            canvas.drawBitmap(animation.getImage(),x,y,null);

        }catch (Exception e){}

    }
    @Override
    public int getWidth()
    {
        return width-10;
    }
}
