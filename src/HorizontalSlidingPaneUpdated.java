import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;

/** @author Krustnic */
public class HorizontalSlidingPane extends Group {

    private float sectionWidth, sectionHeight;
    // Контейнер для секций
    private Group sections;
    // Смещение контейнера sections
    private float amountX = 0;

    // Направление движения секций
    private int transmission   = 0;
    private float stopSection  = 0;
    private float speed        = 1500;
    private int currentSection = 1;
    // Скорость пиксель/секунда после которой считаем, что пользователь хочет перейти к следующей секции
    private float flingSpeed   = 1000;

    private float overscrollDistance = 500;

    private Rectangle cullingArea = new Rectangle();
    private Actor touchFocusedChild;
    private ActorGestureListener actorGestureListener;

    public HorizontalSlidingPaneUpdated() {

        sections = new Group();
        this.addActor( sections );

        sectionWidth  = Gdx.app.getGraphics().getWidth();
        sectionHeight = Gdx.app.getGraphics().getHeight();

        actorGestureListener = new ActorGestureListener() {

            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {

            }

            @Override
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {

                if ( amountX < -overscrollDistance ) return;
                if ( amountX > (sections.getChildren().size - 1) * sectionWidth + overscrollDistance) return;

                amountX -= deltaX;


                cancelTouchFocusedChild();
            }

            @Override
            public void fling (InputEvent event, float velocityX, float velocityY, int button) {

                if ( Math.abs(velocityX) > flingSpeed ) {

                    if ( velocityX > 0 ) setStopSection(currentSection - 2);
                    else setStopSection(currentSection);

                }

                cancelTouchFocusedChild();
            }

            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {

                if ( event.getTarget().getClass() == LevelIcon.class ) {
                    touchFocusedChild = event.getTarget();
                }

            }

        };

        this.addListener(actorGestureListener);

    }

    public void addWidget(Actor widget) {
        widget.setX( this.sections.getChildren().size * sectionWidth );
        widget.setY( 0 );
        widget.setWidth( sectionWidth );
        widget.setHeight( sectionHeight );

        sections.addActor( widget );

    }

    // Вычисление текущей секции на основании смещения контейнера sections
    public int calculateCurrentSection() {
        // Текущая секция = (Текущее смещение / длинну секции) + 1, т.к наши секции нумеруются с 1
        int section = Math.round( amountX / sectionWidth ) + 1;
        //Проверяем адекватность полученного значения, вхождение в интервал [1, количество секций]
        if ( section > sections.getChildren().size ) return sections.getChildren().size;
        if ( section < 1 ) return 1;
        return section;
    }

    public int getSectionsCount() {
        return sections.getChildren().size;
    }

    public void setStopSection(int stoplineSection) {

        if ( stoplineSection < 0 ) stoplineSection = 0;
        if ( stoplineSection > this.getSectionsCount() - 1 ) stoplineSection = this.getSectionsCount() - 1;

        stopSection = stoplineSection * sectionWidth;

        // Определяем направление движения
        // transmission ==  1 - вправо
        // transmission == -1 - влево
        if ( amountX < stopSection) {
            transmission = 1;
        }
        else {
            transmission = -1;
        }
    }

    private void move(float delta) {

        // Определяем направление смещения
        if ( amountX < stopSection) {
            // Двигаемся вправо
            // Если попали сюда, а при этом должны были двигаться влево
            // значит пора остановиться
            if ( transmission == -1 ) {
                amountX = stopSection;
                // Фиксируем номер текущей секции
                currentSection = calculateCurrentSection();
                return;
            }
            // Смещаем
            amountX += speed * delta;

        }
        else if( amountX > stopSection) {
            if ( transmission == 1 ) {
                amountX = stopSection;
                currentSection = calculateCurrentSection();
                return;
            }
            amountX -= speed * delta;
        }
    }

    @Override
    public void act (float delta) {

        // Смещаем контейнер с секциями
        sections.setX( -amountX );

        cullingArea.set( -sections.getX() + 50, sections.getY(), sectionWidth - 100, sectionHeight );
        sections.setCullingArea(cullingArea);

        // Если водим пальцем по экрану
        if ( actorGestureListener.getGestureDetector().isPanning() ) {
            // Устанавливаем границу, к которой будем анимировать движение
            // граница = номер предыдущей секции
            setStopSection(calculateCurrentSection() - 1);
        }
        else {
            // Если палец далеко от экрана - анимируем движение в заданную точку
            move( delta );
        }
    }

    void cancelTouchFocusedChild () {

        if (touchFocusedChild == null) return;

        try {
            this.getStage().cancelTouchFocus(this.actorGestureListener, this);
        } catch (Exception e) {

        }

        touchFocusedChild = null;
    }

    public void setFlingSpeed( float _flingSpeed ) {
        flingSpeed = _flingSpeed;
    }

    public void setSpeed( float _speed ) {
        speed = _speed;
    }

    public void setOverscrollDistance( float _overscrollDistance ) {
        overscrollDistance = _overscrollDistance;
    }

}
