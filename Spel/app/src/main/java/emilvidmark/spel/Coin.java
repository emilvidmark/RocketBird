package emilvidmark.spel;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by emilv on 2017-07-18.
 */

public class Coin extends GameObject {
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spriteSheet;

    public Coin(Bitmap res, int x, int y, int w, int h, int numFrames){
        super.x = x;
        super.y = y;

        width = w;
        height = h;
        speed = 10;

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
