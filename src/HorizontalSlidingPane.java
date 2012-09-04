import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

/** @author Krustnic */
public class HorizontalSlidingPane extends Group {

    private float sectionWidth, sectionHeight;
    // Контейнер для секций
    private Group sections;
    // Смещение контейнера sections
    private float amountX = 0;

    // Направление движения секций
    private int transmission   = 0;
    private float stopOffset = 0;
    private float speed        = 1500;
    private int currentSection = 1;
    // Скорость пиксель/секунда после которой считаем, что пользователь хочет перейти к следующей секции
    private float flingSpeed   = 1000;

    private float overscrollDistance = 500;

    private Actor touchFocusedChild;
    private GestureDetector gestureDetector;

    public HorizontalSlidingPane() {
        sections = new Group();
        this.addActor( sections );

        sectionWidth  = Gdx.app.getGraphics().getWidth();
        sectionHeight = Gdx.app.getGraphics().getHeight();

        // Иницилизируем обработчик событий мыши
        gestureDetector = new GestureDetector(new GestureDetector.GestureListener() {
            // "простое" движение пальцем по экрану
            public boolean pan (int x, int y, int deltaX, int deltaY) {

                if ( amountX < -overscrollDistance ) return false;
                if ( amountX > (sections.getActors().size() - 1) * sectionWidth + overscrollDistance) return false;

                amountX -= deltaX;

                cancelTouchFocusedChild();
                return true;
            }

            // Быстрое скольжение пальцем по экрану
            // ( соответсвует желанию пользователя сильно прокрутить экран в определенную сторону )
            public boolean fling (float velocityX, float velocityY) {
                if ( Math.abs(velocityX) > flingSpeed ) {

                    if ( velocityX > 0 ) setStopSection(currentSection - 1);
                    else setStopSection(currentSection + 1);

                }

                cancelTouchFocusedChild();
                return true;
            }

            public boolean touchDown (int x, int y, int pointer) {
                return true;
            }

            public boolean tap (int x, int y, int count) {
                return false;
            }

            public boolean zoom (float originalDistance, float currentDistance) {
                return false;
            }

            public boolean pinch(Vector2 vector2, Vector2 vector21, Vector2 vector22, Vector2 vector23) {
                return false;
            }

            public boolean longPress (int x, int y) {
                return false;
            }
        });
    }

    // Добавление секции с порцией уровней в наш контейнер
    public void addWidget(Actor widget) {
        widget.x = this.sections.getActors().size() * sectionWidth;
        widget.y = 0;
        widget.width  = sectionWidth;
        widget.height = sectionHeight;

        sections.addActor( widget );
    }

    // Вычисление текущей секции на основании смещения контейнера sections
    public int calculateCurrentSection() {
        // Текущая секция = (Текущее смещение / длинну секции) + 1, т.к наши секции нумеруются с 1
        int section = Math.round( amountX / sectionWidth ) + 1;
        //Проверяем адекватность полученного значения, вхождение в интервал [1, количество секций]
        if ( section > sections.getActors().size() ) return sections.getActors().size();
        if ( section < 1 ) return 1;
        return section;
    }

    public int getSectionsCount() {
        return sections.getActors().size();
    }

    // Устанавлием номер секции, к которой нам нужно сместить экран
    public void setStopSection(int stoplineSection) {

        // Проверяем вхождение секции в интервал [1, количество секций]
        if ( stoplineSection < 1 ) stoplineSection = 1;
        if ( stoplineSection > sections.getActors().size() ) stoplineSection = sections.getActors().size();

        // Высчитываем смещение контейнера sections к которому нужно стремиться
        stopOffset = (stoplineSection-1) * sectionWidth;

        // Определяем направление движения
        // transmission ==  1 - вправо
        // transmission == -1 - влево
        if ( amountX < stopOffset) {
            transmission = 1;
        }
        else {
            transmission = -1;
        }
    }

    private void move(float delta) {

        // Определяем направление смещения
        if ( amountX < stopOffset) {
            // Двигаемся вправо
            // Если попали сюда, а при этом должны были двигаться влево
            // значит пора остановиться
            if ( transmission == -1 ) {
                amountX = stopOffset;
                // Фиксируем номер текущей секции
                currentSection = calculateCurrentSection();
                return;
            }
            // Смещаем
            amountX += speed * delta;

        }
        else if( amountX > stopOffset) {
            if ( transmission == 1 ) {
                amountX = stopOffset;
                currentSection = calculateCurrentSection();
                return;
            }
            amountX -= speed * delta;
        }
    }

    @Override
    public void act (float delta) {

        // Смещаем контейнер с секциями
        sections.x = -amountX;

        // Если водим пальцем по экрану
        if ( gestureDetector.isPanning() ) {
            // Устанавливаем секцию, к которой будем анимировать движение
            // граница = номер предыдущей секции
            setStopSection( calculateCurrentSection() );
        }
        else {
            // Если палец далеко от экрана - анимируем движение в заданную точку
            move( delta );
        }
    }

    // Нас вполне устроит функция отрисовки родительского класса, которая поочереди отрисовывает все дочерние элементы
    /*
    @Override
    public void draw (SpriteBatch batch, float parentAlpha) {
    }
    */

    @Override
    public boolean touchDown (float x, float y, int pointer) {

        // Избегаем параллельного выбора двух уровней двумя пальцами
        if (pointer != 0) return false;

        super.touchDown(x, y, pointer);

        // Запоминаем элемент, который оказался в фокусе при клике
        touchFocusedChild = stage.getTouchFocus(0);
        gestureDetector.touchDown((int)x, (int)y, pointer, 0);

        // Избегаем передачи фокуса на элементы меню
        stage.setTouchFocus(this, 0);
        return true;
    }

    @Override
    public void touchUp (float x, float y, int pointer) {

        // Передаем информацию об окончинии касания в выделенный элемент
        if ( touchFocusedChild != null ) {
            point.x = x;
            point.y = y;
            toLocalCoordinates(touchFocusedChild, point);
            touchFocusedChild.touchUp(point.x, point.y, 0);
            touchFocusedChild = null;
        }

        gestureDetector.touchUp((int)x, (int)y, pointer, 0);
    }

    @Override
    public void touchDragged (float x, float y, int pointer) {
        gestureDetector.touchDragged((int)x, (int)y, pointer);
    }

    void cancelTouchFocusedChild () {
        // Снимаем выделение с элемента
        if (touchFocusedChild == null) return;
        // Integer.MIN_VALUE передаем для того, чтобы элемент мог отличить "окончание касания"
        // от "принудительной отмены фокуса"
        touchFocusedChild.touchUp(Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
        touchFocusedChild = null;
    }

    // Установка значения скорости пиксель/секунда после которой считаем
    // движение по экрану за fling. По умолчанию значение 1000 Px/sec.
    public void setFlingSpeed( float _flingSpeed ) {
        flingSpeed = _flingSpeed;
    }

    // Установка скорости анимации движения
    public void setSpeed( float _speed ) {
        speed = _speed;
    }

    // Установка размера области на которую возможно смещение
    // крайних (левой и правой) секций.
    public void setOverscrollDistance( float _overscrollDistance ) {
        overscrollDistance = _overscrollDistance;
    }

}
