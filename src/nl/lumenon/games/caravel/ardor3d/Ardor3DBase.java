/**
 * Copyright (c) 2009-2010 Ardor Labs, Inc. (http://ardorlabs.com/)
 *   
 * This file is part of Ardor3D-Android (http://ardor3d.com/).
 *   
 * Ardor3D-Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *   
 * Ardor3D-Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *   
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ardor3D-Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.lumenon.games.caravel.ardor3d;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.ardor3d.extension.android.AndroidImageLoader;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.android.AndroidCanvas;
import com.ardor3d.framework.android.AndroidCanvasRenderer;
import com.ardor3d.image.Image;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.android.AndroidKeyWrapper;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scene.state.android.AndroidTextureStateUtil;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.Timer;
import com.ardor3d.util.resource.URLResourceSource;

public class Ardor3DBase implements Runnable {
    public static AndroidCanvas _canvas;
    public AndroidCanvasRenderer _canvasRenderer;
    public boolean _endThread = false;

    public final Timer _timer = new Timer();

    public OrbitCamControl _control;
    public AndroidKeyWrapper _keyWrapper;
    public AndroidGestureMouseWrapper _mouseWrapper;
    public LogicalLayer _logicalLayer = new LogicalLayer();
    public PhysicalLayer _physicalLayer;
    public Node _rootNode;
    
    private AndroidImageLoader _loader = null;

    public Ardor3DBase(Activity activity, Scene scene) {
       // TextureState.DEFAULT_TEXTURE_SOURCE = new URLResourceSource(AndroidTextureStateUtil.class
        //        .getResource("notloaded.png"));
        ImageLoaderUtil.registerDefaultHandler(new AndroidImageLoader());

        Looper.prepare();
        
        // Create our renderer and canvas
        _canvasRenderer = new AndroidCanvasRenderer(scene);
        _canvas = new AndroidCanvas(getSettings(), _canvasRenderer, activity);
        // input
        _keyWrapper = new AndroidKeyWrapper();
        _mouseWrapper = new AndroidGestureMouseWrapper(_canvas);
        _physicalLayer = new PhysicalLayer(_keyWrapper, _mouseWrapper);
        _logicalLayer.registerInput(_canvas, _physicalLayer);
        
        // Orbit Input
		_control = new OrbitCamControl(_canvasRenderer.getCamera(), new Vector3(0,0,0));
		_control.setBaseDistance(15);
		_control.setXSpeed(0.002);
		_control.setYSpeed(0.005);
		_control.setZoomSpeed(0.002);
		_control.setMinAscent(25 * MathUtils.DEG_TO_RAD);
		_control.setMinZoomDistance(5);
		_control.setMaxZoomDistance(30);
		_control.setupMouseTriggers(_logicalLayer, true);
    }

    public DisplaySettings getSettings() {
        return new DisplaySettings(480, 800, 16, 0, 0, 16, 0, 0, true, false);
    }

    //Override
    public void onResume() {
        _canvas.onResume();

        // reset some vars
        _timer.reset();
        _endThread = false;

        // Kick off thread.
        new Thread(this).start();
    }

    //Override
    public void onPause() {
        _canvas.onPause();

        // End thread.
        _endThread = true;
    }

    public void run() {
        Log.i(AndroidCanvas.TAG, "Ardor3DActivity.run - starting game loop");
        while (!_endThread) {
            // update timer
            _timer.update();
            doUpdate(_timer.getTimePerFrame());

            final CountDownLatch latch = new CountDownLatch(1);
            _canvas.draw(latch);
            
            try {
                latch.await(50, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                ; // ignore
            }
            Thread.yield();
        }
        Log.i(AndroidCanvas.TAG, "Ardor3DActivity.run - ending game loop");        
    }
    
    public void doUpdate(final double tpf) {
        _logicalLayer.checkTriggers(tpf);
        if(_rootNode != null){
        	_rootNode.updateGeometricState(tpf);
        }
        
        // execute queue
        GameTaskQueueManager.getManager(_canvas).getQueue(GameTaskQueue.UPDATE).execute();
    }

    //Override
    public boolean onTouchEvent(final MotionEvent event) {
        _mouseWrapper.onTouchEvent(event);
        return true;
    }

    //Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        //if (keyCode == KeyEvent.KEYCODE_BACK) {
        //    return super.onKeyDown(keyCode, event);
        //}
        _keyWrapper.keyPressed(event);
        return true;
    }

    //Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        //if (keyCode == KeyEvent.KEYCODE_BACK) {
        //    return super.onKeyUp(keyCode, event);
        //}
        _keyWrapper.keyReleased(event);
        return true;
    }

    public void destroy(){
    	_canvas=null;
    	_canvasRenderer.releaseCurrentContext();
    	_canvasRenderer=null;
    	_keyWrapper=null;
    	_mouseWrapper=null;
    	_logicalLayer=null;
    	_physicalLayer=null;
    	_rootNode.detachAllChildren();
    	_rootNode=null;
    }
    
}
