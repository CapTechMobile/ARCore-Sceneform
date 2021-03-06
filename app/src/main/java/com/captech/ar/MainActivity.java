/*
 * Copyright 2018 Google Inc. All Rights Reserved.
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

package com.captech.ar;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

/**
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ArFragment mFragment;
    private GestureDetector mGestureDetector;
    private ImageView postImageView;
    private ConstraintLayout editTextConstraintLayout;
    private EditText editTextField;
    private Button saveTextButton;
    private FloatingActionButton fab;
    private int selectedId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fab = findViewById(R.id.fab);
        postImageView = findViewById(R.id.postIcon);
        editTextConstraintLayout = findViewById(R.id.changePostItTextConstraintLayout);
        saveTextButton = findViewById(R.id.saveTextButton);
        editTextField = findViewById(R.id.editTextField);
        mFragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);


        //on tapping of the scene, we want to interact with the world
        mFragment.getArSceneView().getScene().setOnTouchListener((hitTestResult, motionEvent) -> mGestureDetector.onTouchEvent(motionEvent));

        mGestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                tapAddObject(e);
                                return true;
                            }

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }
                        });


        //take a photo on clicking of the fab
        fab.setOnClickListener(view -> PhotoUtils.takePhoto(mFragment));

        //click listener for selecting that you want to post a note.
        postImageView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.postIcon:
                if (selectedId == R.id.postIcon) {
                    //remove selection
                    selectedId = -1;
                    postImageView.setBackground(null);
                } else {
                    //selecting a post it note
                    selectedId = R.id.postIcon;
                    postImageView.setBackground(getDrawable(R.drawable.icon_outline));
                }
                break;
        }

    }

    /**
     * Method that takes the user's tap event and creates an anchor from it
     * to attach a renderable post it note.
     *
     * @param motionEvent
     */
    private void tapAddObject(MotionEvent motionEvent) {
        Frame frame = mFragment.getArSceneView().getArFrame();

        if (selectedId == -1 || motionEvent == null || frame == null ||
                frame.getCamera().getTrackingState() != TrackingState.TRACKING)
            return;


        for (HitResult hit : frame.hitTest(motionEvent)) {
            Trackable trackable = hit.getTrackable();
            if ((trackable instanceof Plane &&
                    ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))) {
                //set the 3d model to the anchor
                buildRenderable(mFragment, hit.createAnchor());

                //remove selected item after a successful set.
                selectedId = -1;
                postImageView.setBackground(null);
                break;

            }
        }
    }

    /**
     * Method to build the renderable post it note.
     *
     * @param fragment
     * @param anchor
     */
    private void buildRenderable(ArFragment fragment, Anchor anchor) {
        ModelRenderable.builder()
                .setSource(fragment.getContext(), Uri.parse("post_it.sfb"))
                .build()
                .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                .exceptionally((throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Codelab error!");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return null;
                }));
    }


    /**
     * Method to take a renderable and attach it to the anchor point the user selected.
     *
     * @param fragment
     * @param anchor
     * @param renderable
     */
    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode postitNode = new TransformableNode(fragment.getTransformationSystem());
        postitNode.setRenderable(renderable);
        postitNode.setParent(anchorNode);

        //rotate the post it to stick to the flat surface.
        postitNode.setLocalRotation(new Quaternion(.65f, 0f, 0f, -.5f));

        //add text view node
        ViewRenderable.builder().setView(this, R.layout.post_it_text).build()
                .thenAccept(viewRenderable -> {
                    Node noteText = new Node();
                    noteText.setParent(fragment.getArSceneView().getScene());
                    noteText.setParent(postitNode);
                    noteText.setRenderable(viewRenderable);
                    noteText.setLocalPosition(new Vector3(0.0f, -0.05f, 0f));
                });

        //adding a tap listener to change the text of a note
        postitNode.setOnTapListener((hitTestResult, motionEvent) -> {
            //select it on touching so we can rotate it and position it as needed
            postitNode.select();

            //toggle the edit text view.
            if (editTextConstraintLayout.getVisibility() == View.GONE) {
                editTextConstraintLayout.setVisibility(View.VISIBLE);

                //save the text when the user wants to
                saveTextButton.setOnClickListener(view -> {
                    TextView tv;
                    for (Node nodeInstance : postitNode.getChildren()) {
                        if (nodeInstance.getRenderable() instanceof ViewRenderable) {
                            tv = ((ViewRenderable) nodeInstance.getRenderable()).getView().findViewById(R.id.postItNoteTextView);
                            tv.setText(editTextField.getText());
                            editTextConstraintLayout.setVisibility(View.GONE);
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            break;
                        }
                    }
                });
            } else {
                editTextConstraintLayout.setVisibility(View.GONE);
            }
        });


        fragment.getArSceneView().getScene().addChild(anchorNode);
        postitNode.select();
    }

}
