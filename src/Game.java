import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

/** @author Krustnic */
public class Game implements ApplicationListener {

    private Stage stage;
    private SpriteBatch batcher;

    HorizontalSlidingPane slidingMenuPure;

    private Texture bg, naviPassive, naviActive;

    private int LINE_MENU_ITEM_COUNT = 6;

    @Override
    public void create() {

        batcher = new SpriteBatch();
        stage = new Stage(Gdx.app.getGraphics().getWidth(), Gdx.app.getGraphics().getHeight(), true, batcher);

        // Инициализируем наш контейнер
        slidingMenuPure = new HorizontalSlidingPane();

        // Считаем ширину иконки уровня исходя из желаемого количества иконок по ширине экрана
        // Ширина = Ширина экрана / желаемое количество - (отступ слева + отступ справа)
        float itemWidth = Gdx.app.getGraphics().getWidth() / LINE_MENU_ITEM_COUNT - 40;

        // Создаем 4 секции с иконками выбора уровня
        // В каждой секции будет 2 строки иконок по 6 в каждой
        // Расставляем иконки по сетке с помощью виджета Table
        for(int section=0; section<4; section++) {
            Table table = new Table();
            for(int i=0; i<2; i++) {
                table.row();
                for(int j = 0; j < 6; j++ ) {
                    // (20,20,60,20) - отступы сверху, слева, снизу, справа
                    table.add( new MenuItem( itemWidth, itemWidth ) ).pad(20,20,60,20);
                }
            }
            // Добавляем секцию в наш контейнер
            slidingMenuPure.addWidget(table);
        }

        stage.addActor( slidingMenuPure );
        Gdx.input.setInputProcessor(stage);

        // Инициализируем необходимые текстуры
        bg          = new Texture(Gdx.files.internal("assets/bg.png"));
        naviPassive = new Texture(Gdx.files.internal("assets/naviPassive.png"));
        naviActive  = new Texture(Gdx.files.internal("assets/naviActive.png"));
    }

    @Override
    public void resize(int w, int h) {

    }

    @Override
    public void render() {
        GL10 gl = Gdx.graphics.getGL10();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        batcher.begin();
        // Рисуем фон
        batcher.draw(bg, 0, 0, Gdx.app.getGraphics().getWidth(), Gdx.app.getGraphics().getHeight());

        // Рисуем указатель текущей секции
        for(int i=1; i<= slidingMenuPure.getSectionsCount(); i++) {
            if ( i == slidingMenuPure.calculateCurrentSection() ) {
                batcher.draw( naviActive, Gdx.app.getGraphics().getWidth()/2 - slidingMenuPure.getSectionsCount()*20/2 + i*20 , 50);
            }
            else {
                batcher.draw( naviPassive, Gdx.app.getGraphics().getWidth()/2 - slidingMenuPure.getSectionsCount()*20/2 + i*20 , 50);
            }
        }

        batcher.end();

        // Просчитываем и отрисовываем анимацию
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
