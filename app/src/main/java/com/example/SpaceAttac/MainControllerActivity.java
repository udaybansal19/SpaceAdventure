/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.SpaceAttac;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Random;
import java.util.*;


public class MainControllerActivity extends AppCompatActivity {
  private static final String TAG = MainControllerActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  public ArFragment arFragment;
  private ModelRenderable andyRenderable;
  private ModelRenderable playerRenderable;
  ArrayList<ModelRenderable> spaceRenderable = new ArrayList<ModelRenderable>();
  private ObjectAnimator objectAnimation;

    private int oneTimeFlag = 0;
    private int position = 2;
    private AnchorNode startNode;
    private AnchorNode endNode;
    private Queue<AnchorNode> planetsSetQueue = new LinkedList<>();
    public Queue<ObjectAnimator> planetsAnimationQueue = new LinkedList<>();
    public Node playerNode = new Node();
    private Node playerPositionNode = new Node();
    private Node rightNode = new Node();
    private Node leftNode = new Node();
    private Node centerNode = new Node();
    private Node andy;
    private Context con;
    private int counterUpdatePlanets = 0;
    private TimeAnimator planetsMove;
    private TextView scoreView;
    public TextView scoreViewGameEnd;
    public LinearLayout gameEndLayout;
    private ImageButton restartButton;
    public int score = 0;
    public int highScore = 0;
    private int setNumber = 1;
    private int planetsMoveTime = 2100;
    private int nextPlanetSetIn = 620;
    public boolean isColliding = false;

    private Vector3 scale = new Vector3(0.5f,0.5f,0.5f);
    private Vector3 startNodePosition = new Vector3(0f,0f,-1.7f);
    private Vector3 rightNodePosition = new Vector3(0.6f,0f,0f);
    private Vector3 leftNodePosition = new Vector3(-0.6f,0f,0f);
    private Vector3 centerNodePosition = new Vector3(0f,0f,0f);

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    setContentView(R.layout.activity_ux);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

   arFragment.getArSceneView().getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
       @Override
       public void onUpdate(FrameTime frameTime) {
           if(arFragment.getArSceneView().getScene().overlapTestAll(playerNode).size()!=0){
               isColliding = true;
               for(Node node1 : arFragment.getArSceneView().getScene().overlapTestAll(playerNode)){
                   if(node1.getName().equalsIgnoreCase("node"))
                       isColliding = false;
                   else
                   {
                       isColliding = true;
                   }
               }
               if(isColliding) {
                   planetsMove().end();
                   gameEndLayout.setVisibility(View.VISIBLE);
                   //arFragment.getArSceneView().clearAnimation();
                   for(ObjectAnimator objectAnimator : planetsAnimationQueue){
                       objectAnimator.pause();
                   }

                   if(score > highScore){
                       highScore = score;
                       SharedPreferences mPref = getSharedPreferences("highScore",0);
                       SharedPreferences.Editor mEditor = mPref.edit();
                       mEditor.putInt("highScore",score).commit();
                       scoreViewGameEnd.setText("Score: " + score + "\nBest: " + highScore);
                   }
                   else
                   {
                       scoreViewGameEnd.setText("Score: " + score + "\nBest: " + highScore);
                   }

               }
           }
       }
   });

      SharedPreferences mPref = getSharedPreferences("highScore",MODE_PRIVATE);
      highScore = mPref.getInt("highScore",0);

    // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
      //For Arrow
      String arg[] = {"Astronaut.sfb",
      "jupiter.sfb",
      "mars.sfb",
              "mercury.sfb","neptune.sfb","pluto.sfb","saturn.sfb","Spacestation.sfb","ufo.sfb","uranus.sfb","venus.sfb","Earth.sfb"
      };
      for(int i=0;i<12;i++)
      modelRender(arg[i],i);
    ModelRenderable.builder()
        .setSource(this, Uri.parse("spaceship.sfb"))
        .build()
        .thenAccept(renderable -> playerRenderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "Unable to load playerRenderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });
      ModelRenderable.builder()
              .setSource(this, R.raw.andy)
              .build()
              .thenAccept(renderable -> andyRenderable = renderable)
              .exceptionally(
                      throwable -> {
                          Toast toast =
                                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                          toast.setGravity(Gravity.CENTER, 0, 0);
                          toast.show();
                          return null;
                      });

      TextView initialInstuc = (TextView) findViewById(R.id.initialInstruc);
      Button playButton = findViewById(R.id.playButton);
      restartButton = findViewById(R.id.restartButton);
      gameEndLayout = findViewById(R.id.gameEndLayout);
      scoreView = (TextView) findViewById(R.id.score);
      scoreViewGameEnd = findViewById(R.id.scoreViewGameEnd);

          arFragment.setOnTapArPlaneListener(
                  (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                      if (andyRenderable == null || oneTimeFlag!=0) {
                          return;
                      }
                      arFragment.getArSceneView().getPlaneRenderer().setVisible(false);
                      initialInstuc.setVisibility(View.INVISIBLE);
                      playButton.setVisibility(View.VISIBLE);

                      oneTimeFlag++;
                      Anchor anchor = hitResult.createAnchor();
                      endNode = new AnchorNode(anchor);
                      endNode.setParent(arFragment.getArSceneView().getScene());

                      startNode = new AnchorNode();
                      startNode.setParent(endNode);
                      startNode.setLocalPosition(startNodePosition);

                      playerPositionNode.setParent(endNode);

                      centerNode.setParent(playerPositionNode);
                      rightNode.setParent(playerPositionNode);
                      leftNode.setParent(playerPositionNode);

                      playerPositionNode.setLocalScale(scale);
//                      playerPositionNode.setLocalPosition(new Vector3(0f,0f,-0.1f));

                      rightNode.setLocalPosition(rightNodePosition);
                      leftNode.setLocalPosition(leftNodePosition);

                      playerNode.setParent(endNode);
                      playerNode.setLocalScale(scale);
                      playerNode.setCollisionShape(playerRenderable.getCollisionShape());
                      playerNode.setRenderable(playerRenderable);
//                      testModel(playerNode);

                      //Move player ship
                      View.OnTouchListener onTouchListener = new OnSwipeTouchListener(MainControllerActivity.this) {



                          public void onSwipeRight() {
                              if(position == 1)
                              {
                                  position = 2;
                                  playerMove(leftNode,centerNode);
                              }
                              else if(position == 2) {
                                  position = 3;
                                  playerMove(centerNode, rightNode);
                              }
                          }
                          public void onSwipeLeft() {
                              if(position == 3)
                              {
                                  position = 2;
                                  playerMove(rightNode,centerNode);
                              }
                              else if(position == 2){
                                  position = 1;
                                  playerMove(centerNode,leftNode);
                              }
                          }

//                          public void onSwipeTop() {
//                              Toast.makeText(MainControllerActivity.this, "top", Toast.LENGTH_SHORT).show();
//                          }
//                          public void onSwipeBottom() {
//                              Toast.makeText(MainControllerActivity.this, "bottom", Toast.LENGTH_SHORT).show();
//                          }

                      };

                      arFragment.getArSceneView().setOnTouchListener(onTouchListener);

                      con = this;

                     planetsMove = allPlanetsMove();

                      playButton.setOnClickListener(new View.OnClickListener (){
                          public void onClick(View v) {
                              // Do something in response to button click
                              planetsMove.start();
                              playButton.setVisibility(View.GONE);
                          }
                      });

                      restartButton.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {

                              gameEndLayout.setVisibility(View.INVISIBLE);
                              score = 0;
                              scoreView.setText("" + score);
                              planetsMove = allPlanetsMove();
                              planetsMove.start();
                              counterUpdatePlanets = 0;
                              for(AnchorNode anchorNode : planetsSetQueue){
                                  anchorNode.setParent(null);
                              }
                              planetsSetQueue.clear();
                              planetsAnimationQueue.clear();

                          }
                      });


                      //allPlanetsMove();
                  });

  }

  private void testModel(Node node){
      MaterialFactory.makeTransparentWithColor(getApplicationContext(), new Color(244, 244, 244))
              .thenAccept(
                      material -> {

                          Vector3 vector3 = new Vector3(0.05f, 0.05f,0.05f);
                          ModelRenderable model = ShapeFactory.makeCube(scale,
                                  Vector3.zero(), material);
                          model.setShadowCaster(false);
                          model.setShadowReceiver(false);
                          //model.setCollisionShape(sp);

                          TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                          transformableNode.setParent(node);
                          transformableNode.setRenderable(model);
                          //transformableNode.setCollisionShape(sp);
                          transformableNode.select();
                      });
  }


//    private boolean collisionDetect(){
//      boolean fla = false;
//        if(arFragment.getArSceneView().getScene().overlapTestAll(playerNode).size()!=0){
//            fla=true;
//            for(Node node1 : arFragment.getArSceneView().getScene().overlapTestAll(playerNode)){
//                if(node1.getName().equalsIgnoreCase("node"))
//                    fla=false;
//                else
//                {
//                    fla=true;
//                }
//            }
//            if(fla) {
//                planetsMove.end();
//                gameEndLayout.setVisibility(View.VISIBLE);
//                //arFragment.getArSceneView().clearAnimation();
//                for(ObjectAnimator objectAnimator : planetsAnimationQueue){
//                    objectAnimator.pause();
//                }
//
//                if(score > highScore){
//                    highScore = score;
//                    SharedPreferences mPref = getSharedPreferences("highScore",0);
//                    SharedPreferences.Editor mEditor = mPref.edit();
//                    mEditor.putInt("highScore",score).commit();
//                    scoreViewGameEnd.setText("Score: " + score + "\nBest: " + highScore);
//                }
//                else
//                {
//                    scoreViewGameEnd.setText("Score: " + score + "\nBest: " + highScore);
//                }
//
//            }
//        }
//        return fla;
//    }

  private TimeAnimator allPlanetsMove(){

      TimeAnimator animator = new TimeAnimator();
      animator.setTimeListener(new TimeAnimator.TimeListener() {
          @Override
          public void onTimeUpdate(TimeAnimator a, long total, long dt){
              // total = millis since animation started
              // dt = millis since last update
              if(total/nextPlanetSetIn==counterUpdatePlanets)
              {
                  updatePlanets();
                  counterUpdatePlanets++;
                  //collisionDetect();
              }

          }
      });

      return animator;

  }

  private Node planetsSet(){
      AnchorNode planetsSetNode = new AnchorNode();
      planetsSetNode.setParent(endNode);
      planetsSetNode.setWorldPosition(startNodePosition);


      planetsSetNode.setLocalScale(scale);

      Random rand = new Random();
      int r = rand.nextInt(1000);


      ModelRenderable m1 = randomObj();
      ModelRenderable m2 = randomObj();
      while(m1.equals(m2))
          m2 = randomObj();

          if (r % 3 != 0)
          {
              Node st1 = new Node();
              st1.setParent(planetsSetNode);
              st1.setLocalPosition(rightNodePosition);
              st1.setRenderable(m1);
              st1.setCollisionShape(m1.getCollisionShape());
              st1.setName("planetSet: " + setNumber + "PlanetNumber: " + 1 );
              m1=m2;
          }
          if (r % 3 != 1)
          {
              Node st2 = new Node();
              st2.setParent(planetsSetNode);
              st2.setLocalPosition(centerNodePosition);
              st2.setRenderable(m1);
              st2.setCollisionShape(m1.getCollisionShape());
              st2.setName("planetSet: " + setNumber + "PlanetNumber: " + 2 );
              if( r % 3 == 0)
                  m1=m2;
          }
          if (r % 3 != 2)
          {
              Node st3 = new Node();
              st3.setParent(planetsSetNode);
              st3.setLocalPosition(leftNodePosition);
              st3.setRenderable(m1);
              st3.setCollisionShape(m1.getCollisionShape());
              st3.setName("planetSet: " + setNumber + "PlanetNumber: " + 3 );
          }

          setNumber++;
          planetsSetQueue.add(planetsSetNode);
      return planetsSetNode;
  }

  private AnimatorSet updatePlanets() {
      AnimatorSet s = new AnimatorSet();
      if(!isColliding){
          s.play(planetsMove());
          s.addListener(new Animator.AnimatorListener() {
              @Override
              public void onAnimationStart(Animator animation) {
                  //endNode.setParent(null);
              }

              @Override
              public void onAnimationEnd(Animator animation) {
                  //planetsSetNode.setParent(null);
                  score++;
                  scoreView.setText("" + score);
                  planetsSetQueue.peek().setParent(null);
                  planetsSetQueue.remove();
                  planetsAnimationQueue.remove();
              }

              @Override
              public void onAnimationCancel(Animator animation) {

              }

              @Override
              public void onAnimationRepeat(Animator animation) {
              }
          });
          s.start();
      }
      else
      {

      }
      return s;
  }

  public ObjectAnimator planetsMove(){

      Node n = planetsSet();
      n.setParent(endNode);
      objectAnimation = new ObjectAnimator();
      objectAnimation.setTarget(n);

      // All the positions should be world positions
      // The first position is the start, and the second is the end.
      objectAnimation.setObjectValues(startNode.getWorldPosition(), endNode.getWorldPosition());

      // Use setWorldPosition to position andy.
      objectAnimation.setPropertyName("worldPosition");

      // The Vector3Evaluator is used to evaluator 2 vector3 and return the next
      // vector3.  The default is to use lerp.
      objectAnimation.setEvaluator(new Vector3Evaluator());
      // This makes the animation linear (smooth and uniform).
      objectAnimation.setInterpolator(new LinearInterpolator());
      // Duration in ms of the animation.
      objectAnimation.setDuration(planetsMoveTime);
      //objectAnimation.setRepeatCount(Animation.INFINITE);
      planetsAnimationQueue.add(objectAnimation);
      //collisionDetect();
      return objectAnimation;
    }

  private ObjectAnimator playerMove(Node from,Node moveTo){

    objectAnimation = new ObjectAnimator();
    objectAnimation.setTarget(playerNode);

    // All the positions should be world positions
    // The first position is the start, and the second is the end.
    objectAnimation.setObjectValues(from.getWorldPosition(),moveTo.getWorldPosition());

    // Use setWorldPosition to position andy.
    objectAnimation.setPropertyName("worldPosition");

    // The Vector3Evaluator is used to evaluator 2 vector3 and return the next
    // vector3.  The default is to use lerp.
    objectAnimation.setEvaluator(new Vector3Evaluator());
    // This makes the animation linear (smooth and uniform).
    objectAnimation.setInterpolator(new LinearInterpolator());
    // Duration in ms of the animation.
    objectAnimation.setDuration(250);
    //objectAnimation.setRepeatCount(Animation.INFINITE);
      objectAnimation.start();

    return objectAnimation;
  }

  private ModelRenderable randomObj(){
    Random rand = new Random();
    int r = rand.nextInt(1000);
    return spaceRenderable.get(r%spaceRenderable.size());
  }

  private void modelRender(String arg,int i){
      ModelRenderable.builder()
              .setSource(this, Uri.parse(arg))
              .build()
              .thenAccept(renderable -> spaceRenderable.add(renderable))
              .exceptionally(
                      throwable -> {
                          Toast toast =
                                  Toast.makeText(this, "Unable to load " + arg, Toast.LENGTH_LONG);
                          toast.setGravity(Gravity.CENTER, 0, 0);
                          toast.show();
                          return null;
                      });
  }


  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }
}
