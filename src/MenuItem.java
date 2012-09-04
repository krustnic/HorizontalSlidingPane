import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** @author Krustnic */
public class MenuItem extends Actor {

    private static int N = 0;
    private int currentN;

    private Texture texMenuItem;
    private TextureRegion tex;

    private BitmapFont font;

    public MenuItem( float _width, float _height ) {
        texMenuItem = new Texture(Gdx.files.internal("assets/item.png"));
        tex = new TextureRegion(texMenuItem, 0, 0, 128, 128);

        this.width = _width;
        this.height = _height;

        font = new BitmapFont();

        // Глобальная нумерация кнопок выбора уровня
        N += 1;
        currentN = N;
    }

    @Override
    public void draw(SpriteBatch batcher, float v) {

        batcher.draw(tex, x, y, originX, originY, width, height, scaleX, scaleY, rotation);

        font.setColor(1, 1, 1, 1);
        font.setScale(2);
        font.draw(batcher, String.valueOf(currentN), x + width/2, y + height/2 );
    }

    @Override
    public Actor hit(float x, float y) {
        // Если нажали на данный элемент, то возвращаем его выше
        return x > 0 && x < width && y > 0 && y < height?this:null;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer) {
        width += 10;
        height += 10;
        return true;
    }

    @Override
    public void touchUp(float x, float y, int pointer) {
        width  -= 10;
        height -= 10;
    }
}
