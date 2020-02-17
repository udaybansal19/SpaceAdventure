package com.example.SpaceAttac;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.view.View;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;

import java.util.ArrayList;

public class CollisionDetect extends MainControllerActivity {

//    public boolean onUpdate(FrameTime frameTime) {
//            if(arFragment.getArSceneView().getScene().overlapTestAll(playerNode).size()!=0){
//                isColliding = true;
//                for(Node node1 : arFragment.getArSceneView().getScene().overlapTestAll(playerNode)){
//                    if(node1.getName().equalsIgnoreCase("node"))
//                        isColliding = false;
//                    else
//                    {
//                        isColliding = true;
//                    }
//                }
//                if(isColliding) {
//                    planetsMove().end();
//                    gameEndLayout.setVisibility(View.VISIBLE);
//                    //arFragment.getArSceneView().clearAnimation();
//                    for(ObjectAnimator objectAnimator : planetsAnimationQueue){
//                        objectAnimator.pause();
//                    }
//
//                    if(score > highScore){
//                        highScore = score;
//                        SharedPreferences mPref = getSharedPreferences("highScore",0);
//                        SharedPreferences.Editor mEditor = mPref.edit();
//                        mEditor.putInt("highScore",score).commit();
//                        scoreViewGameEnd.setText("Score: " + score + "\nBest: " + highScore);
//                    }
//                    else
//                    {
//                        scoreViewGameEnd.setText("Score: " + score + "\nBest: " + highScore);
//                    }
//
//                }
//            }
//            return isColliding;
//    }


}
