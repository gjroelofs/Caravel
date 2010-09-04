package nl.lumenon.games.caravel;

import nl.lumenon.games.caravel.ardor3d.Ardor3DBase;
import nl.lumenon.games.caravel.ardor3d.BaseScene;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Caravel extends Activity{
	
	
	public static final int LOADING_DONE=-419872010,SWITCH_NOW=-419872011, SET_TOTAL=-419872012, NEXT_TOTAL=-419872013;
	private static final int SPLASH_SCREEN_WAIT=2000, LOAD_SCREEN_WAIT=2000, LOAD_TOTAL=5000;
	/** Handlers */
	private Handler viewHandler_ = new Handler(){
		
		public void handleMessage(Message message){			
			int view = message.arg1;
			setContentView(view);
			
			if(view == R.layout.load){
				ProgressBar pb = (ProgressBar)findViewById(R.id.loadprogress);
				pb.setMax(LOAD_TOTAL);
			}
		}
		
	};
	private Handler loadHandler_ = new Handler(){
		public void handleMessage(Message message){
			if(message.arg2!= SWITCH_NOW){
				if(message.arg2==SET_TOTAL){
					currentCategTot = message.arg1;					
				} else if(message.arg2==NEXT_TOTAL){
					currentCateg++;

					int value = (int)((LOAD_TOTAL/(double)loadingCategs)*currentCateg);
					String text = (String) message.obj;
		
					ProgressBar pb = (ProgressBar)findViewById(R.id.loadprogress);
					TextView tv = (TextView)findViewById(R.id.loadtext);
					
					pb.setProgress(value);
					tv.setText(text);
					
				} else {
					int value = message.arg1;
					value = (int)((LOAD_TOTAL/(double)loadingCategs)*currentCateg + ((LOAD_TOTAL/(double)loadingCategs)/currentCategTot)*value);
					String text = (String) message.obj;
		
					ProgressBar pb = (ProgressBar)findViewById(R.id.loadprogress);
					TextView tv = (TextView)findViewById(R.id.loadtext);
					
					pb.setProgress(value);
					tv.setText(text);
	
					if(message.arg2==LOADING_DONE){
		
						pb.setMax(1);
						pb.setProgress(1);
						
						Message msg = loadHandler_.obtainMessage();
						msg.arg2=SWITCH_NOW;
						loadHandler_.sendMessageDelayed(msg, LOAD_SCREEN_WAIT);				
					}
				}
			} else {
				setContentView(ardor3d_._canvas);	
				ardor3d_.onResume();
			}
		}
	};

	/** Private Variables */
	private BaseScene gameScene_;
    private Ardor3DBase ardor3d_;
    private int loadingCategs=3,currentCateg=0, currentCategTot=0;

	public Caravel() {
	}	

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.splash);	
		
		Thread loader = new Thread(){
			public void run(){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Message change = new Message();
				change.arg1=R.layout.load;
				
				viewHandler_.sendMessage(change);			
								
				startGame();
			}
		};
		
		loader.start();
	}
	
	/**
	 * Overriding Methods
	 */	

    @Override
    protected void onResume() {
    	if(ardor3d_ != null)
    		ardor3d_.onResume();
    	
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	if(ardor3d_ != null)
    		ardor3d_.onPause();
    	
    	super.onPause();
    }
	
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
    	if(ardor3d_!=null)
    		ardor3d_.onTouchEvent(event);
    	
        return true;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {        	
            return super.onKeyDown(keyCode, event);
        }
    	if(ardor3d_!=null)
    		ardor3d_.onKeyDown(keyCode,event);
    	
        return true;
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
       if (keyCode == KeyEvent.KEYCODE_BACK) {
    	   ardor3d_.onPause();
    	   gameScene_.destroy();
    	   gameScene_=null;
    	   ardor3d_.destroy();
    	   ardor3d_=null;
           this.finish();
       }
    	if(ardor3d_!=null)
    		ardor3d_.onKeyUp(keyCode, event);
    	
        return true;
    }
	
	/**
	 * Private Methods
	 */
    
	private void startGame(){
		gameScene_ = new BaseScene(this);
		ardor3d_ = new Ardor3DBase(this, gameScene_);
		gameScene_.startSetup(ardor3d_, loadHandler_);
	
	}
	
}