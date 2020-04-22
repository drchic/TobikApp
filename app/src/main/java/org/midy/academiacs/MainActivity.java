package org.midy.academiacs;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

   /** duration of zoom animation */
   public static final int ZOOM_ANIMATION_TIME = 750;

   /** duration of rotate animation */
   public static final int ROTATE_ANIMATION_TIME = 2000;

   /** duration of fade animations */
   public static final int FADE_ANIMATION_TIME = 1000;

   /** image view */
   private ImageView imgV;

   /** initial image view x position */
   private float initImgXPosition;

   /** initial image view y position */
   private float initImgYPosition;

   /** is image view on original position */
   private boolean isOriginalViewPosition = true;

   /** wide and height of the display */
   private final Point displaySize = new Point();

   @Override
   protected void onCreate(final Bundle savedInstanceState) {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      getWindow().setFlags(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL, WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
      super.onCreate(savedInstanceState);

      this.setContentView(R.layout.activity_main);

      this.imgV = this.findViewById(R.id.imageView);

      this.imgV.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(final View v) {
            animateRotation();
            MainActivity.this.isOriginalViewPosition = true;
         }
      });
   }

   @Override
   public void onWindowFocusChanged(final boolean hasFocus) {
      super.onWindowFocusChanged(hasFocus);

      if (hasFocus) {
         final View decorView = getWindow().getDecorView();
         /*decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                 | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                 | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);*/
         //| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
         /*decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                 | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                 | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                 | View.SYSTEM_UI_FLAG_FULLSCREEN
                 | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
         );*/

         this.displaySize.y = decorView.getHeight();
         this.displaySize.x = decorView.getWidth();

         this.initImgXPosition = this.imgV.getX();
         this.initImgYPosition = this.imgV.getY();
      }
   }

   @Override
   public boolean onTouchEvent(final MotionEvent event) {
      final boolean validation = validateTouch(event);
      if (validation)
         this.isOriginalViewPosition = false;
      return validation;
   }

   /**
    * Validates touch event
    * @param event motion event
    * @return true if tablet was touched, else false
    */
   private boolean validateTouch(final MotionEvent event) {
      if (MotionEvent.ACTION_DOWN == event.getAction()) {
         final Point pointToMove = getPointToMove(event);

         animateMotion(pointToMove.x, pointToMove.y).start();

         return true;
      }

      return false;
   }

   /**
    * Gets point to which the image view is about to be moved
    * @param event touch event
    * @return image view point
    */
   private Point getPointToMove(final MotionEvent event) {
      int moveToX = (int) event.getRawX() - (this.imgV.getWidth() / 2);
      if (moveToX + this.imgV.getWidth() > this.displaySize.x) {
         moveToX = this.displaySize.x - this.imgV.getWidth();
      } else if (moveToX < 0) {
         moveToX = 0;
      }

      int moveToY = (int) event.getRawY() - (this.imgV.getHeight() / 2);
      if (moveToY + this.imgV.getHeight() > this.displaySize.y) {
         moveToY = this.displaySize.y - this.imgV.getHeight();
      } else if (moveToY < 0) {
         moveToY = 0;
      }

      return new Point(moveToX, moveToY);
   }

   /**
    * Animates the image view to be disappearing, moving and appearing
    * @param moveToX x position where the image view is about to move
    * @param moveToY y position where the image view is about to move
    * @return animator set which includes disappearing, moving and appearing of the image view
    */
   private AnimatorSet animateMotion(final float moveToX, final float moveToY) {
      final AnimatorSet set = new AnimatorSet();
      final List<Animator> animations = new ArrayList<>();

      final AnimatorSet fadeOutSet = new AnimatorSet();
      fadeOutSet.play(ObjectAnimator.ofFloat(MainActivity.this.imgV, View.ALPHA, 1f, 0f).setDuration(FADE_ANIMATION_TIME));
      animations.add(fadeOutSet);

      final AnimatorSet motionSet = new AnimatorSet();
      motionSet.play(ObjectAnimator.ofFloat(MainActivity.this.imgV, View.X, moveToX))
              .with(ObjectAnimator.ofFloat(MainActivity.this.imgV, View.Y, moveToY));
      motionSet.setDuration(0);
      animations.add(motionSet);

      if (moveToX != this.initImgXPosition && moveToY != this.initImgYPosition) {
         final AnimatorSet fadeInSet = new AnimatorSet();
         fadeInSet.play(ObjectAnimator.ofFloat(MainActivity.this.imgV, View.ALPHA, 0f, 1f).setDuration(FADE_ANIMATION_TIME));
         animations.add(fadeInSet);
      }

      set.playSequentially(animations);

      return set;
   }

   /**
    * Animates the image view to be disappearing, appearing in the centre position, zooming in, rotating and zooming out
    */
   private void animateRotation() {
      final AnimatorSet set = new AnimatorSet();
      final List<Animator> animations = new ArrayList<>();

      if (!this.isOriginalViewPosition)
         animations.add(animateMotion(this.initImgXPosition, this.initImgYPosition));


      final AnimatorSet zoomInSet = createZoomInAnimation();
      animations.add(zoomInSet);

      final AnimatorSet rotateSet = createRotateAnimation();
      animations.add(rotateSet);

      final AnimatorSet zoomOutSet = createZoomOutAnimation();
      animations.add(zoomOutSet);

      set.playSequentially(animations);
      set.start();
   }

   /**
    * Creates zoom in animation for image view
    * @return animator set
    */
   private AnimatorSet createZoomInAnimation() {
      final AnimatorSet zoomInSet = new AnimatorSet();

      if (!this.isOriginalViewPosition) {
         zoomInSet.play(ObjectAnimator.ofFloat(this.imgV, View.ALPHA, 0f, 1f))
                 .with(ObjectAnimator.ofFloat(this.imgV, View.SCALE_X, 0f, 2f))
                 .with(ObjectAnimator.ofFloat(this.imgV, View.SCALE_Y, 0f, 2f));
      } else {
         zoomInSet.play(ObjectAnimator.ofFloat(this.imgV, View.SCALE_X, 1f, 2f))
                 .with(ObjectAnimator.ofFloat(this.imgV, View.SCALE_Y, 1f, 2f));
      }

      zoomInSet.setDuration(ZOOM_ANIMATION_TIME);

      return zoomInSet;
   }

   /**
    * Creates rotate animation for image view
    * @return animator set
    */
   private AnimatorSet createRotateAnimation() {
      final AnimatorSet rotateSet = new AnimatorSet();
      rotateSet.playSequentially(ObjectAnimator.ofFloat(this.imgV, View.ROTATION, 0, 360),
              ObjectAnimator.ofFloat(this.imgV, View.ROTATION, 360, 0));
      rotateSet.setDuration(ROTATE_ANIMATION_TIME);

      return rotateSet;
   }

   /**
    * Creates zoom out animation for image view
    * @return animator set
    */
   private AnimatorSet createZoomOutAnimation() {
      final AnimatorSet zoomOutSet = new AnimatorSet();
      zoomOutSet.play(ObjectAnimator.ofFloat(this.imgV, View.SCALE_X, 2f, 1f))
              .with(ObjectAnimator.ofFloat(this.imgV, View.SCALE_Y, 2f, 1f));
      zoomOutSet.setDuration(ZOOM_ANIMATION_TIME);

      return zoomOutSet;
   }
}
